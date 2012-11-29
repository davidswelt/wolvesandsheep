package was;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Tournament class describes and manages the game tournament. This is the
 * main (entry) class.
 *
 * @author dr
 */
public class Tournament {

    protected long TIMEOUT = 35;
    protected ArrayList<Class> disqualifiedPlayers = new ArrayList<Class>();
    protected GameBoard eboard;
    protected ArrayList<Class> players = new ArrayList<Class>();
    protected Random random = new Random();
    int numSheep = 4;

    static Class name2class(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    Player playerFactory(Class cl, String symbol) {
        try {
            Constructor c = cl.getConstructor();
            try {
                return (Player) c.newInstance();
            } catch (InstantiationException ex) {
                Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    class AverageScore extends TreeMap<String, ArrayList<Double>> {

        void add(String s, double by) {
            ArrayList<Double> f = super.get(s);
            if (f == null) {
                f = new ArrayList<Double>();
                put(s, f);
            }
            f.add((Double) by);
            put(s, f);
        }

        public double get(String s) {
            ArrayList<Double> f = super.get(s);
            if (f == null) {
                return 0.0;
            } else {
                double sum = 0.0;
                for (Double d : f) {
                    sum += d.floatValue();
                }
                return sum / f.size();
            }
        }

        public ArrayList<String> getEntriesGreaterThan(double limit) {
            ArrayList<String> ret = new ArrayList<String>();
            for (Map.Entry<String, ?> k : this.entrySet()) {
                if (get(k.getKey()) > limit) {
                    ret.add(k.getKey());
                }
            }

            return ret;
        }

        public void print() {
            System.out.println("Average runtimes:");
            for (Map.Entry<String, ?> k : this.entrySet()) {
                System.out.println(k.getKey() + ":\t" + String.format("%2.4f", (double) get(k.getKey())) + "ms");
            }
        }
    }

    public class HighScore extends TreeMap<String, Double> {

        void inc(String s) {
            inc(s, 1.0);
        }

        void inc(String s, double by) {
            put(s, new Double(get(s) + by));
        }

        public double get(String s) {
            Double f = super.get(s);
            if (f == null) {
                return 0.0;
            } else {
                return f.floatValue();
            }
        }

        public void print() {
            System.out.println("Highscore:");
            for (Map.Entry<String, Double> k : this.entrySet()) {
                System.out.println(k.getKey() + ":\t" + ((Double) k.getValue()));
            }
        }
    }
    AverageScore timing = new AverageScore();
    HighScore highscore = new HighScore();

    static ArrayList<Class> string2classlist(String listofPlayerClassNames) {
        ArrayList<Class> players = new ArrayList<Class>();
        StringTokenizer st = new StringTokenizer(listofPlayerClassNames, ", ");

        while (st.hasMoreElements()) {
            players.add(name2class(st.nextToken()));
        }
        return players;
    }

    /**
     * Create a new tournament and run it.
     *
     * @param listofPlayerClassNames: String of comma-separated, fully qualified
     * classnames of players, e.g. "smith.Sheep,smith.Sheep,smith.Sheep,
     * reitter.SheepPlayer,reitter.WolfPlayer". Needs a minimum number of
     * players. Repeat player class names if necessary.
     *
     * @param repeats: number of repetitions
     */
    static public void run(String listofPlayerClassNames, int repeats) {


        run(string2classlist(listofPlayerClassNames), repeats);

    }

    /**
     * creates and runs a tournament, printing the results.
     *
     * @param playerClasses: Array of classes of players. Each player has to
     * implement the was.Player interface.
     * @param r number of repetitions to run
     */
    static public void run(List<Class> playerClasses, int r) {

        run(playerClasses, 40, 40, r, false, 0);
    }

    /**
     * creates and runs a tournament, printing the results.
     *
     * @param playerClasses: Array of classes of players. Each player has to
     * implement the was.Player interface.
     * @param m height of board
     * @param n width of board
     * @param k number of pieces in a row required to win
     * @param r number of repetitions to run
     * @param ui true if UI is to be shown
     * @param scenario number of the scenario to be used
     */
    static public void run(List<Class> playerClasses, int m, int n, int r, boolean ui, int scenario) {

        Tournament t;

        t = new Tournament(playerClasses, m, n, r, ui);

        int totalgames = r * playerClasses.size() * (playerClasses.size() - 1) * (playerClasses.size() - 2) * (playerClasses.size() - 3);

        System.err.println("Total trials: " + totalgames);

        t.start(totalgames > 100000, scenario);

        t.highscore.print();
        t.timing.print();


    }

    /**
     * starts the tournament.
     *
     */
    TreeMap<String, Double> start(boolean printHighscores, int scenario) {

// check players

        int wolves = 0;
        int sheep = 0;
        for (Class p : players) {
            if (isWolf(p)) {
                wolves++;
            } else {
                sheep++;
            }
        }
        if (sheep < numSheep) {
            System.err.println("Must specify at least " + numSheep + " sheep classes.  Have " + sheep + " sheep and " + wolves + " wolves.");
            return null;

        }
        if (wolves < 1) {
            System.err.println("Must specify at least one wolf class.");
            return null;

        }


        startT(printHighscores, new ArrayList<Integer>(), scenario);

        return highscore;

    }

    boolean isWolf(Class c) {
        return WolfPlayer.class.isAssignableFrom(c);
    }

    void startT(boolean printHighscores, ArrayList<Integer> selectedPlayers, int scenario) {


        try {
            // reached number of sheep (plus wolf), or have selected all available players
            if (selectedPlayers.size() > numSheep) // termination condition
            {
                GameBoard board = new GameBoard(boardWidth, boardHeight, boardUI);

                
                for (Integer i : selectedPlayers) {
                    GameLocation initialLocation = board.randomEmptyLocation();

                    board.addPlayer(playerFactory(players.get(i), (isWolf(players.get(i)) ? "w" : "s")), initialLocation);

                }
                addScenario(scenario, board);

                Map<Player, int[]> s = board.playGame();

                for (Map.Entry<Player, int[]> score : s.entrySet()) {
                    highscore.inc(score.getKey().getClass().getName(), score.getValue()[0]);
                }
                if (printHighscores) {
                    //final String ESC = "\033[";
                    //System.out.println(ESC + "2J"); 
                    highscore.print();
                }


            } else {
                for (int i = 0; i < players.size(); i++) {
                    Class p1 = players.get(i);

                    // do not add a player twice
                    if (!selectedPlayers.contains(i)) {
                        if (isWolf(p1) && (selectedPlayers.size() == numSheep)
                                || !isWolf(p1) && selectedPlayers.size() < numSheep) {
                            selectedPlayers.add(i);

                            startT(printHighscores, selectedPlayers, scenario);
                            selectedPlayers.remove(selectedPlayers.size() - 1); // so we don't have to make a copy
                        }
                    }
                }


            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    int boardWidth = 30;
    int boardHeight = 30;
    boolean boardUI = false;

    Tournament(List<Class> playerClasses, int m, int n, int r, boolean ui) {
        //(Class[] playerClasses, int m, int n, int r) {

        boardWidth = m;
        boardHeight = n;
        boardUI = ui;

        // Security Policy
        //System.setProperty("java.security.policy", "file:sandbox.policy");
        //System.setSecurityManager(new SecurityManager());

        players = new ArrayList();

        for (Class p : playerClasses) {

            if (PlayerTest.runTest(p)) {

                if (!SheepPlayer.class.isAssignableFrom(p) && !WolfPlayer.class.isAssignableFrom(p)) {
                    System.err.println("Error: " + p.getName() + " is not a subtype of was.SheepPlayer or was.WolfPlayer.");
                } else {
                    players.add(p);
                }
            }
        }
        // adjust timeout
        //TIMEOUT = (long) ((float) TIMEOUT * (float) Benchmark.runBenchmark());
    }

    final void addScenario(int scenario, GameBoard board) {
        
                board.addPlayer(new Pasture(),null);
                board.addPlayer(new Pasture(),null);
                board.addPlayer(new Pasture(),null);
                board.addPlayer(new Obstacle(),null);
    }

    public static void ist240() {
        String[] sheepteams = new String[]{
            "CHHITH,GEISER,HAFAIRI,HOFBAUER",
            "CONTINO,GARRITY,HOFFMAN,TAILOR",
            "BROADWATER,DERHAMMER,CHAN,TUBERGEN",
            "BONCHONSKY,HE,SUON,USCAMAYTA"
        };

        String[] wolves = new String[]{
            "MONDELL,MULLEN,MUNOZ",
            "CHEETHAM,KIDNEY,LAFFERTY",
            "REIZNER,SICINSKI,ZIELINSKY",
            "NORANTE,RAUGH,ULIANA",
            "GREENE,WILKINSON,YOSUA"
        };

        for (String s : sheepteams) { // each sheep team
            for (String w : wolves) { // each wolf team

                ArrayList<Class> wolves2 = string2classlist(w);

                for (Class w2 : wolves2) // for each wolf within a group
                {

                    ArrayList p = string2classlist(s); // all sheep
                    p.add(w2); // one wolf

                    // randomize order of sheep
                    Collections.shuffle(p);
                    
                    run(p, 20, 20, 5, false, 0);
                }
            }
        }
    }

    public static void main(String args[]) {

        ArrayList<Class> players = new ArrayList<Class>();
        int m = 30;
        int n = 30;
        int k = 4;
        int r = 100;
        boolean ui = true;

        // parse the command line
        int i = 0;
        while (i < args.length) {
            String s = args[i++];
            if (s.equals("-t")) {

                StringTokenizer st = new StringTokenizer(args[i++], ",");
                m = Integer.parseInt(st.nextToken());
                n = Integer.parseInt(st.nextToken());
                k = Integer.parseInt(st.nextToken());

            } else if (s.equals("-u")) {
                ui = true;
            } else if (s.equals("-c")) {
                ui = false;
            } else if (s.equals("-r")) {

                r = Integer.parseInt(args[i++]);

            } else {
                players.add(name2class(s));
            }
        }


        if (players.size() > 1) {
            was.Tournament.run(players, m, n, r, ui, 0); // m, n, k,
        } else {
            System.err.println("Usage: java -jar WolvesAndSheep.jar -t M,N,K -r R CLASS1 CLASS2 CLASS3 CLASS4 CLASS5 (...)");
            System.err.println("       -t M,N,K  ==> play a M*N board with K sheep.");
            System.err.println("       -r R      ==> play R repeats of each game.");
            System.err.println("       -c        ==> do not show the graphical user interface ");
            System.err.println("Example: java -jar WolvesAndSheep.jar -t 20,20,4 -r 400 players.BasicSheep players.BasicWolf players.BasicSheep players.BasicSheep players.BasicSheep");
            System.err.println("Example for NetBeans (Run Configuration, Program arguments): -t 20,20,4 -r 400 players.BasicSheep players.BasicWolf players.BasicSheep players.BasicSheep players.BasicSheep");
            // do not run a default case to make sure it doesn't cause confusion.
            //            was.Tournament.run("reitter.SheepPlayer,reitter.WolfPlayer,reitter.SheepPlayer,reitter.SheepPlayer, reitter.SheepPlayer", 100);
        }
    }
}
