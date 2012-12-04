package was;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.CollationKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
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

    protected ArrayList<Class> disqualifiedPlayers = new ArrayList<Class>();
    protected GameBoard eboard;
    protected ArrayList<Class> players = new ArrayList<Class>();
    protected Random random = new Random();
    int numSheep = 4;
    int numWolves = 1;
    protected static Map<String, String> teams = new HashMap();

    
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

        TreeMap<String, Integer> uses = new TreeMap();

        void inc(String s) {
            inc(s, 1.0);
        }

        void inc(String s, double by) {
            put(s, new Double(get(s) + by));
        }

        void noteUse(String s) {
            Integer prev = uses.get(s);

            uses.put(s, new Integer(prev == null ? 1 : prev + 1));

        }

        public double get(String s) {
            Double f = super.get(s);
            if (f == null) {
                return 0.0;
            } else {
                return f.floatValue();
            }
        }

        public double getNormalized(String s) {
            Double f = super.get(s);
            if (f == null) {
                return 0.0;
            } else {
                Integer n = uses.get(s);
                if (n == null || n.equals(0)) {
                    return f.floatValue();
                }
                return f.floatValue() / n;
            }
        }

        public void print() {
            System.out.println("____________________________________\n");
            System.out.println("Highscore:");
            printKeys(new ArrayList(keySet()), false);

        }

        public class TreeValueComparator implements Comparator<String> {

            @Override
            public int compare(String a, String b) {
                return ((Double) HighScore.this.get(a)).compareTo(HighScore.this.get(b));
            }
        };

        public void printKeys(List<String> keys, boolean sorted) {


            // sort it
            Collections.sort(keys, new TreeValueComparator());

            for (String k : keys) {
                System.out.println(k + ":\t" + String.format("%.3g", getNormalized(k)));
            }
        }

        public void printByCategory() {
            System.out.println("____________________________________\n");
            System.out.println("Highscore:");

            Set<String> keys = keySet();
            Map<String, List<String>> cats = new TreeMap(); // categories

            for (String k : keys) {
                StringTokenizer t = new StringTokenizer(k, ".");
                t.nextToken();
                String classname = t.nextToken();
                if (cats.get(classname) == null) {
                    cats.put(classname, new ArrayList());
                }
                cats.get(classname).add(k); // add whole key
            }

            // for each category, print it, sorted

            for (String k : cats.keySet()) {
                System.out.println();
                printKeys(cats.get(k), true);
            }


        }
    }
    AverageScore timing = new AverageScore();
    HighScore highscore = new HighScore();

    static ArrayList<Class> string2classlist(String listofPlayerClassNames) {

        StringTokenizer st = new StringTokenizer(listofPlayerClassNames, ":");
        if (st.hasMoreElements()) {
            st.nextToken();
            if (st.hasMoreElements()) {
                listofPlayerClassNames = st.nextToken();
            }
        }


        ArrayList<Class> players = new ArrayList<Class>();
        st = new StringTokenizer(listofPlayerClassNames, ", ");

        while (st.hasMoreElements()) {
            players.add(name2class(st.nextToken()));
        }
        return players;
    }

    static String prefix(String s) {
        StringTokenizer st = new StringTokenizer(s, ": ");
        if (st.hasMoreElements()) {
            return st.nextToken();
        }
        return "";
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

        t.highscore.printByCategory();
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
//        if (sheep < numSheep) {
//            System.err.println("Must specify at least " + numSheep + " sheep classes.  Have " + sheep + " sheep and " + wolves + " wolves.");
//            return null;
//
//        }
//        if (wolves < numWolves) {
//            System.err.println("Must specify at least one wolf class.");
//            return null;
//
//        }
        // if fewer sheep or wolves are specified, just don't add them.

        numSheep = Math.min(numSheep, sheep);
        numWolves = Math.min(numWolves, wolves);


        startT(printHighscores, new ArrayList<Integer>(), scenario);

        return highscore;

    }

    boolean isWolf(Class c) {
        return WolfPlayer.class.isAssignableFrom(c);
    }

    int countPl(ArrayList<Integer> selectedPlayers, boolean wolf) {
        int count = 0;
        for (int i : selectedPlayers) {
            if (isWolf(players.get(i)) == wolf) {
                count++;
            }
        }
        return count;
    }

    void startT(boolean printHighscores, ArrayList<Integer> selectedPlayers, int scenario) {
// 
        try {
            // reached number of sheep (plus wolf), or have selected all available players
            if (selectedPlayers.size() >= numSheep + numWolves) // termination condition
            {
                GameBoard board = new GameBoard(boardWidth, boardHeight, boardUI);

                Stack<GameLocation> wolfQueue = new Stack();
                Stack<GameLocation> sheepQueue = new Stack();
                
                addScenario(scenario, board, wolfQueue, sheepQueue); // add scenario first to occupy these spaces

                for (Integer i : selectedPlayers) {
                    Stack<GameLocation> queue = isWolf(players.get(i)) ? wolfQueue : sheepQueue;
                    GameLocation initialLocation = queue.empty() ? board.randomEmptyLocation() : queue.pop();
                        
                    board.addPlayer(playerFactory(players.get(i), (isWolf(players.get(i)) ? "w" : "s")), initialLocation);

                    highscore.noteUse(players.get(i).getName());
                }

                Map<Player, int[]> s = board.playGame();

                for (Map.Entry<Player, int[]> score : s.entrySet()) {
                    String clname = score.getKey().getClass().getName();
                    highscore.inc(clname, score.getValue()[0]);
                    if (teams.get(clname) != null) {
                        highscore.inc(teams.get(clname), score.getValue()[0]);
                    }
                }
                if (printHighscores) {
                    //final String ESC = "\033[";
                    //System.out.println(ESC + "2J"); 
                    highscore.printByCategory();
                }


            } else {
                for (int i = 0; i < players.size(); i++) {
                    Class p1 = players.get(i);

                    // do not add a player twice
                    if (!selectedPlayers.contains(i)) {
                        if ((isWolf(p1) && countPl(selectedPlayers, true) < numWolves)
                                || (!isWolf(p1) && countPl(selectedPlayers, false) < numSheep)) {
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

    final void addScenario(int scenario, GameBoard board, Stack<GameLocation> wolfP, Stack<GameLocation> sheepP) {

        // scnario 0 is randomly chosen from several

        if (scenario == 0) {
            scenario = 4; // random.nextInt(4)+1;
        }

        switch (scenario) {
            case 1:

                board.addPlayer(new Pasture(), new GameLocation(1, 1));
                board.addPlayer(new Pasture(), new GameLocation(1, 2));
                board.addPlayer(new Pasture(), new GameLocation(2, 2));
                board.addPlayer(new Obstacle(), new GameLocation(15, 15));
                board.addPlayer(new Obstacle(), new GameLocation(16, 15));
                break;
            case 2:

                board.addPlayer(new Pasture(), new GameLocation(28, 1));
                board.addPlayer(new Pasture(), new GameLocation(27, 1));
                board.addPlayer(new Pasture(), new GameLocation(29, 1));
                board.addPlayer(new Obstacle(), new GameLocation(15, 20));
                board.addPlayer(new Obstacle(), new GameLocation(16, 20));
                board.addPlayer(new Obstacle(), new GameLocation(14, 20));
                break;
            case 3:

                board.addPlayer(new Pasture(), new GameLocation(2, 2));
                board.addPlayer(new Pasture(), new GameLocation(3, 2));
                board.addPlayer(new Pasture(), new GameLocation(28, 1));
                board.addPlayer(new Pasture(), new GameLocation(27, 1));
                board.addPlayer(new Pasture(), new GameLocation(27, 2));
                board.addPlayer(new Pasture(), new GameLocation(28, 2));

                board.addPlayer(new Obstacle(), new GameLocation(15, 20));
                board.addPlayer(new Obstacle(), new GameLocation(16, 20));
                board.addPlayer(new Obstacle(), new GameLocation(14, 20));
                break;
            case 4: // this challenges the sheep
                board.addPlayer(new Pasture(), new GameLocation(1, 1));
                board.addPlayer(new Pasture(), new GameLocation(1, 2));
                board.addPlayer(new Pasture(), new GameLocation(2, 1));
                board.addPlayer(new Pasture(), new GameLocation(2, 2));


                board.addPlayer(new Obstacle(), new GameLocation(0, 4));
                board.addPlayer(new Obstacle(), new GameLocation(1, 4));
                board.addPlayer(new Obstacle(), new GameLocation(2, 4));
                board.addPlayer(new Obstacle(), new GameLocation(3, 4));
                board.addPlayer(new Obstacle(), new GameLocation(4, 3));
                board.addPlayer(new Obstacle(), new GameLocation(4, 4));
                board.addPlayer(new Obstacle(), new GameLocation(5, 2));
                board.addPlayer(new Obstacle(), new GameLocation(5, 3));
                sheepP.add(new GameLocation(5,29));
                sheepP.add(new GameLocation(10,29));
                sheepP.add(new GameLocation(20,29));
                sheepP.add(new GameLocation(25,29));
                
                break;
            case 5: // this challenges the wolf
                board.addPlayer(new Pasture(), new GameLocation(1, 1));
                board.addPlayer(new Pasture(), new GameLocation(1, 2));
                board.addPlayer(new Pasture(), new GameLocation(2, 1));
                board.addPlayer(new Pasture(), new GameLocation(2, 2));


                board.addPlayer(new Obstacle(), new GameLocation(20, 1));
                board.addPlayer(new Obstacle(), new GameLocation(20, 2));
                board.addPlayer(new Obstacle(), new GameLocation(20, 3));
                board.addPlayer(new Obstacle(), new GameLocation(20, 4));
                board.addPlayer(new Obstacle(), new GameLocation(20, 5));
                board.addPlayer(new Obstacle(), new GameLocation(21, 5));
                board.addPlayer(new Obstacle(), new GameLocation(22, 6));
                board.addPlayer(new Obstacle(), new GameLocation(23, 7));
                board.addPlayer(new Obstacle(), new GameLocation(24, 8));
                board.addPlayer(new Obstacle(), new GameLocation(21, 1));
                board.addPlayer(new Obstacle(), new GameLocation(21, 2));
                board.addPlayer(new Obstacle(), new GameLocation(21, 3));
                board.addPlayer(new Obstacle(), new GameLocation(21, 4));
                board.addPlayer(new Obstacle(), new GameLocation(21, 6));
                board.addPlayer(new Obstacle(), new GameLocation(22, 5));
                board.addPlayer(new Obstacle(), new GameLocation(23, 6));
                board.addPlayer(new Obstacle(), new GameLocation(24, 7));
                board.addPlayer(new Obstacle(), new GameLocation(25, 8));

                wolfP.add(new GameLocation(28,3));
                break;
        }
    }

    public static void ist240() {
        String[] sheepteams = new String[]{
            "Black Sheep:CHHITH,GEISER,HAFAIRI,HOFBAUER",
            "Creepy Sheepies:SCONTINO,GARRITY,HOFFMAN,TAILOR",
            "Dolly's Den:BROADWATER,DERHAMMER,CHAN,TUBERGEN",
            "White Sheep:BONCHONSKY,HE,SUON,USCAMAYTA"
        };

        String[] wolves = new String[]{
            "Hungry Beast:MONDELL,MULLEN,MUNOZ",
            "Lone Hunters:CHEETHAM,KIDNEY,LAFFERTY",
            "Furry Fury:REIZNER,SICINSKI,ZIELINSKY",
            "The Gray:NORANTE,RAUGH,ULIANA",
            "Wolf in Sheep's Clothing:GREENE,WILKINSON,YOSUA"
        };

        for (String s : sheepteams) { // each sheep team
            String sheepteam = prefix(s);

            for (String w : wolves) { // each wolf team

                ArrayList<Class> wolves2 = string2classlist(w);
                String wolfteam = prefix(w);

                for (Class w2 : wolves2) // for each wolf within a group
                {
                    teams.put(w2.getName(), wolfteam);

                    ArrayList<Class> p = string2classlist(s); // all sheep

                    for (Class sh : p) {
                        teams.put(sh.getName(), sheepteam);

                    }

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
        int sc = 0; // random scenario
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
            } else if (s.equals("-e")) {
                Player.catchExceptions  = true;
            } else if (s.equals("-r")) {

                r = Integer.parseInt(args[i++]);

            } else if (s.equals("-s")) {

                sc = Integer.parseInt(args[i++]);

            } else {
                players.add(name2class(s));
            }
        }


        if (players.size() > 0) {
            was.Tournament.run(players, m, n, r, ui, sc); // m, n, k,
        } else {
            System.err.println("Usage: java -jar WolvesAndSheep.jar -t M,N,K -r R CLASS1 CLASS2 CLASS3 CLASS4 CLASS5 (...)");
            System.err.println("       -t M,N,K  ==> play a M*N board with K sheep.");
            System.err.println("       -r R      ==> play R repeats of each game.");
            System.err.println("       -S S      ==> set up scenario no. S (0 for random)");
            System.err.println("       -e        ==> ignore player's exceptions");
            System.err.println("       -c        ==> do not show the graphical user interface ");
            System.err.println("Example: java -jar WolvesAndSheep.jar -t 20,20,4 -r 400 players.BasicSheep players.BasicWolf players.BasicSheep players.BasicSheep players.BasicSheep");
            System.err.println("Example for NetBeans (Run Configuration, Program arguments): -t 20,20,4 -r 400 players.BasicSheep players.BasicWolf players.BasicSheep players.BasicSheep players.BasicSheep");
            // do not run a default case to make sure it doesn't cause confusion.
            //            was.Tournament.run("reitter.SheepPlayer,reitter.WolfPlayer,reitter.SheepPlayer,reitter.SheepPlayer, reitter.SheepPlayer", 100);
        }
    }
}
