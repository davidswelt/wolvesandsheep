package was;

import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Tournament class describes and manages the game tournament.
 *
 * This is the main (entry) class. See main() function for usage.
 *
 *
 * @author dr
 */
public class Tournament implements GameBoard.WolfSheepDelegate {

    protected ArrayList<Class> players = new ArrayList<Class>();
    protected static Random random = new Random();
    // Scenario scenario = null;
    int initNumSheep = 4;
    int initNumWolves = 1;
    int numSheep, numWolves;
    protected static Map<Object, String> teams = new HashMap();
    static int minNumSheepRequiredToRun = 0;  // run won't start a game otherwise
    static int minNumWolvesRequiredToRun = 0;
    static boolean exitRequested = false;
    static boolean resetRequested = false;
    static boolean pauseInitially = false;
    static boolean quiet = false;

    static void logPlayerMoveAttempt(Class pl, GameBoard g) {
        moveLog.inc(pl.getName() + ".Crash");
        if (g != null && g.scenario != null) {
            String ss = "Scenario" + g.scenario.toString();

            moveLog.inc(pl.getName() + ".Crash." + ss);
        }

    }

    static void logPlayerCrash(Class pl, Throwable ex, GameBoard g) {

        crashLog.inc(pl.getName() + ".Crash");
//        crashLog.inc(pl.getName() + ".Crash\\" + ex);

        if (g != null && g.scenario != null) {
            String ss = "Scenario" + g.scenario.toString();
            crashLog.inc(pl.getName() + ".Crash." + ss);
            crashLog.inc(pl.getName() + ".Crash." + ss + "\\" + ex);
        }

    }
    volatile static HighScore crashLog = new HighScore().setTitle("crashes", "class");
    volatile static HighScore moveLog = new HighScore().setTitle("total calls to move()", "class");

    volatile HighScore timing;
    volatile HighScore scenarioTiming;
    volatile HighScore highscore;
    volatile HighScore scenarioScore;
    volatile HighScore eatingScore;

    void initHighScores() {
        timing = new HighScore();
        scenarioTiming = new HighScore();
        highscore = new HighScore();
        scenarioScore = new HighScore();
        eatingScore = new HighScore();

    }

    static String prefix(String s) {
        StringTokenizer st = new StringTokenizer(s, ":");
        if (st.hasMoreElements()) {
            return st.nextToken();
        }
        return "";
    }

    /**
     * given a list of objects, return combinations of N.
     */
    // To Do
    /**
     * Create a new tournament and run it.
     *
     * @param listofPlayerClassNames String of comma-separated, fully qualified
     * classnames of players, e.g. "smith.Sheep,smith.Sheep,smith.Sheep,
     * reitter.SheepPlayer,reitter.WolfPlayer". Needs a minimum number of
     * players. Repeat player class names if necessary.
     *
     * @param repeats number of repetitions
     */
    static public void run(String listofPlayerClassNames, int repeats) {

        run(PlayerFactory.string2classlist(listofPlayerClassNames, ""), repeats);

    }

    /**
     * creates and runs a tournament, printing the results.
     *
     * @param playerClasses Array of classes of players. Each player has to
     * implement the was.Player interface.
     * @param r number of repetitions to run
     */
    static public void run(List<Class> playerClasses, int r) {

        run(playerClasses, r, false, 0, true, true, 1);
    }

    /**
     * creates and runs a tournament, printing the results.
     *
     * @param playerClasses Array of classes of players. Each player has to
     * implement the was.Player interface.
     * @param r number of repetitions to run
     * @param ui true if UI is to be shown
     * @param scenario number of the scenario to be used
     * @param comb true if the tournament should test all combinations of the
     * players. Otherwise, all given players will be added to the game board at
     * once.
     * @param printHighScore print highscore if true.
     * @param threads number of threads to run in parallel
     * @return resulting Tournament object.
     */
    static public Tournament run(List<Class> playerClasses, int r, boolean ui, int scenario, boolean comb, boolean printHighScore, int threads) {

        Tournament t;

        t = new Tournament(playerClasses, r, ui);

        int totalgames = r * playerClasses.size() * Math.max(1, playerClasses.size() - 1) * Math.max(1, playerClasses.size() - 2) * Math.max(1, playerClasses.size() - 3);

//        logerr("Total trials: " + totalgames);
        try {
            t.start(totalgames > 100000 && !quiet, scenario, r, comb, threads);
        } finally {
            if (printHighScore) {
                t.highscore.print();
                System.out.print(dividerLine);
                t.scenarioScore.printByCategory(null);
                System.out.print(dividerLine);
                System.out.println("Player Crashes:");
                crashLog.printByCategory(Arrays.asList(moveLog));

                System.out.println(dividerLine);
                System.out.println("Timing (Timeout: " + Player.getTimeout() + "ms per move):");
                t.timing.print();

            }
//        t.timing.print();
        }

        return t;
    }

    class TournamentRunnable implements Runnable {

        boolean printHighscores;
        List<Integer> selectedPlayers;
        int scenarioNum;
        int repeats;
        int id;

        public TournamentRunnable(int id, boolean printHighscores, List<Integer> selectedPlayers, int scenarioNum, int repeats) {
            this.id = id;
            this.printHighscores = printHighscores;
            this.selectedPlayers = new ArrayList(selectedPlayers);
            this.scenarioNum = scenarioNum;
            this.repeats = repeats;
        }

        public void run() {
            startT(printHighscores, selectedPlayers, scenarioNum, repeats, id);
        }
    }

    /**
     * starts the tournament.
     *
     */
    TreeMap<String, Double> start(boolean printHighscores, int scenario, int repeats, boolean combinations, int threads) {

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
//            logerr("Must specify at least " + numSheep + " sheep classes.  Have " + sheep + " sheep and " + wolves + " wolves.");
//            return null;
//
//        }
//        if (wolves < numWolves) {
//            logerr("Must specify at least one wolf class.");
//            return null;
//
//        }
        // if fewer sheep or wolves are specified, just don't add them.

        numSheep = Math.min(initNumSheep, sheep);
        numWolves = Math.min(initNumWolves, wolves);

        List<Integer> sp = new ArrayList<Integer>();
        if (!combinations) { // just add all given players to the selection
            int i = 0;
            for (Class p : players) {
                sp.add(i++);
            }
        }

        PrintStream prevErrStream = System.err;
        PrintStream prevOutStream = System.out;

        if (threads == 1) {
            startT(printHighscores, sp, scenario, repeats, 0);
        } else {
            System.out.println("Starting " + threads + " threads.");
            Thread[] ts = new Thread[threads];
            for (int t = 0; t < threads; t++) {
                Runnable runner = new TournamentRunnable(t + 1, printHighscores, sp, scenario, Math.max(1, (int) (repeats / threads)));
                ts[t] = new Thread(runner);
                ts[t].start(); // thread start

            }
            // are we finished yet?
            for (Thread tr : ts) {
                try {
                    tr.join();

                } catch (InterruptedException ex) {
                    // we'll ignore.  Main thread won't be interrupted.

                }
            }
            System.out.println("All threads synchronized.");

        }
        // with multi-threaded, we can't use logToFile properly and output will
        // not end up where it is supposed to be.

        System.setOut(prevOutStream);
        System.setErr(prevErrStream);

        return highscore;

    }

    boolean isWolf(Class c) {
        return WolfPlayer.class
                .isAssignableFrom(c);
    }

    int countPl(List<Integer> selectedPlayers, boolean wolf) {
        int count = 0;
        for (int i : selectedPlayers) {
            if (isWolf(players.get(i)) == wolf) {
                count++;
            }
        }
        return count;
    }

    void startT(boolean printHighscores, List<Integer> selectedPlayers, int scenarioNum, int repeats, int threadID) {
// 
        try {

            ArrayList selSheep = new ArrayList();
            ArrayList selWolves = new ArrayList();
            ArrayList<ArrayList> sheepComb, wolvesComb;

            if (selectedPlayers.size() > 0) {
                for (Integer i : selectedPlayers) {
                    Class plClass = players.get(i);
                    if (isWolf(plClass)) {
                        selWolves.add(i);
                    } else {
                        selSheep.add(i);
                    }
                }
                sheepComb = new ArrayList();
                sheepComb.add(selSheep);
                wolvesComb = new ArrayList();
                wolvesComb.add(selWolves);

                // Queues for the players come back from scenario in
                // a deterministic order.  So, shuffle wolves/sheep
                // in case they
                Collections.shuffle(sheepComb);
                Collections.shuffle(wolvesComb);

            } else {
                for (Integer i = 0; i < players.size(); i++) {
                    Class plClass = players.get(i);
                    if (isWolf(plClass)) {
                        selWolves.add(i);
                    } else {
                        selSheep.add(i);
                    }
                }

                Combinations c;

                c = new Combinations(selWolves);
                wolvesComb = c.drawNwithoutReplacement(numWolves);
                c = new Combinations(selSheep);
                sheepComb = c.drawNwithoutReplacement(Math.min(numSheep, selSheep.size()));

            }

//            if (!quiet) {
//                logout("Thread " + threadID + ": " + sheepComb.size() + " sheep teams, " + wolvesComb.size() + " wolves, " + repeats + " reps.");
//            }
            for (int r = 0; r < repeats && exitRequested == false; r++) {

                int theSc = scenarioNum;
                int numS = Scenario.getParameterValues().size();
                // randomize, and enough repeats available?
                if (scenarioNum == 0 && repeats > 1) {
                    theSc = Scenario.getParameterValues().get(r % numS);
                }

                Scenario scenario = Scenario.makeScenario(theSc);

                for (ArrayList selWolfComb : wolvesComb) {
                    for (ArrayList selSheepComb : sheepComb) {

                        resetRequested = true;
                        while (resetRequested) {
                            resetRequested = false;

                            selectedPlayers = new ArrayList();

                            /* players play in the order in which they are added:
                             * the last player added plays first.
                             * 
                             * In W&S, the wolf will play last.
                             * The game engine relies on this partially by checking
                             * for a sheep-attacking-wolf scenario just before 
                             * executing the wolf's move.
                             */
                            selectedPlayers.addAll(selWolfComb);
                            selectedPlayers.addAll(selSheepComb);

                            GameBoard board = new GameBoard(scenario, boardUI, 80);
                            board.wolfEatSheepDelegate = this;

                            Stack<GameLocation> wolfQueue = new Stack();
                            Stack<GameLocation> sheepQueue = new Stack();

                            scenario.addToBoard(board, wolfQueue, sheepQueue); // add scenario first to occupy these spaces

                            for (Integer i : selectedPlayers) {

                                Class pclass = players.get(i);
                                Stack<GameLocation> queue = isWolf(pclass) ? wolfQueue : sheepQueue;
                                GameLocation initialLocation = queue.empty() ? board.randomEmptyLocation(wolfQueue, sheepQueue) : queue.pop();

                                Player player = PlayerFactory.makePlayerInstance(pclass);
                                if (player != null) {

                                    board.addPlayer(player, initialLocation);
                                }
                                // note use even if player wasn't added (due to crash!)
                                highscore.noteUse(pclass.getName());
                                scenarioScore.noteUse("Scenario " + scenario.toString() + "\\" + pclass.getName());

                                if (teams.get(pclass) == null) {
                                //logerr(i);
                                    //logerr("WARNING: can't get team for " + players.get(i));
                                } else {
                                    // note use of player so that team score can be normalized later
                                    highscore.noteUse(teams.get(pclass) + (isWolf(pclass) ? ".WolfTeam" : ".SheepTeam"));
                                }
                            }

                            int wolves = 0, sheep = 0;

                            for (Player p : board.players) {
                                if (p instanceof WolfPlayer) {
                                    wolves++;
                                }
                                if (p instanceof SheepPlayer) {
                                    sheep++;
                                }
                            }

                            if (sheep >= minNumSheepRequiredToRun && wolves >= minNumWolvesRequiredToRun) {

                                if (r == 0 && !quiet) {
                                    synchronized (this) {
                                        logout("Thread " + threadID + " Scenario " + theSc + ": ");
                                        logout(board.playerOverviewToString());
                                    }
                                    // board.print();
                                }

                                try {

                                    // PLAY THE GAME
                                    Map<Player, int[]> s = board.playGame(pauseInitially);

                                    // DONE
                                    for (Map.Entry<Player, int[]> score : s.entrySet()) {
                                        Class cl = score.getKey().getClass();
                                        if (score.getKey().isIncludedInHighScore()) {

                                            
                                            double mrt = score.getKey().meanRunTime(); // in milliseconds
                                            int scr = score.getValue()[0];
                                            score.getValue()[0] = 0; // set to 0 to make sure it doesn't get added twice

                                            highscore.inc(cl.getName(), scr);
                                            // score.noteUse happens earlier

                                            timing.inc(cl.getName(), mrt);
                                            timing.noteUse(cl.getName());

                                            final String scenPlayStr = "Scenario " + scenario.toString() + "\\" + cl.getName();
                                            scenarioTiming.inc(scenPlayStr, mrt);
                                            scenarioTiming.noteUse(scenPlayStr);
                                            scenarioScore.inc(scenPlayStr, scr);

                                            // for teams
                                            if (teams.get(cl) != null) { // Pastures etc don't have a team

                                                String tname = teams.get(cl) + (score.getKey() instanceof WolfPlayer ? ".WolfTeam" : ".SheepTeam");
                                                highscore.inc(tname, scr);
                                                timing.inc(tname, mrt);
                                                timing.noteUse(tname);
                                            }

                                        }
                                    }
                                } finally {
                                    if (printHighscores) {
                                        //final String ESC = "\033[";
                                        //System.out.println(ESC + "2J"); 
                                        highscore.printByCategory(null);

                                    }
                                }
                            }
                            // for some reason this helps... GC doesn't run that much otherwise.                        
                            board = null;
                            System.gc();
                        }
                    }
                }
            }

        } catch (Throwable ex) {
            Logger.getLogger(Tournament.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
    boolean boardUI = false;

    Tournament(List<Class> playerClasses, int r, boolean ui) {
        //(Class[] playerClasses, int m, int n, int r) {

        initHighScores();

        boardUI = ui;

        // Security Policy
        //System.setProperty("java.security.policy", "file:sandbox.policy");
        //System.setSecurityManager(new SecurityManager());
        players = new ArrayList();

        for (Class p : playerClasses) {

            if (PlayerTest.runTest(p, crashLog)) {

                if (!SheepPlayer.class
                        .isAssignableFrom(p) && !WolfPlayer.class
                        .isAssignableFrom(p)) {
                    logerr(
                            "Error: " + p.getName() + " is not a subtype of was.SheepPlayer or was.WolfPlayer.");
                } else if (PlayerTest.runUnitTest(p, crashLog)) {
                    highscore.inc(p.getName(), 0.0);
                    players.add(p);
                }
            }
        }

        // adjust timeout
        //TIMEOUT = (long) ((float) TIMEOUT * (float) Benchmark.runBenchmark());
    }
    static String dividerLine = "_______________________________________________________________________________________________________________________________\n\n";
    // static init
    protected static boolean secPolicySet = false;

    {
        if (!secPolicySet) {

            ClassLoader cl = Tournament.class.getClassLoader();
            java.net.URL policyURL = cl.getResource("sandbox2.policy");

            System.out.println(policyURL.toString());

            if (!policyURL.toString().contains("!")) {
                // the following is the correct version
                // it works on windows
                // but it fails when loading from a JAR file...
                // so it can't run on the server
                // see also: http://stackoverflow.com/questions/22605666/java-access-files-in-jar-causes-java-nio-file-filesystemnotfoundexception
                try {
                    System.setProperty("java.security.policy", Paths.get(policyURL.toURI()).toString());
                } catch (URISyntaxException ex) {
                    Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {    // this version will fail on windows
                // but it runs from jar files
                System.setProperty("java.security.policy", policyURL.toString());
            }

            SecurityManager sm = new SecurityManager();
            System.setSecurityManager(sm);
            secPolicySet = true;

        }
    }

    static final java.io.PrintStream standardSystemOut = System.out;
    static final java.io.PrintStream standardSystemErr = System.err;

    synchronized static void logout(String s) {
        PrintStream previousStream = System.out;
        System.setOut(standardSystemOut);
        System.out.println(s);
        System.setOut(previousStream);

    }

    synchronized static void logerr(String s) {
        PrintStream previousStream = System.err;
        System.setErr(standardSystemErr);
        System.err.println(s);
        System.setErr(previousStream);

    }

    public static void main(String args[]) {

        try {
            ArrayList<Class> players = new ArrayList<Class>();
//        int m = -1;
//        int n = -;
//        int k = 4;
            int r = 1;
            int sc = 0; // random scenario
            boolean ui = true;
            boolean tourn = false;
            int thr = 1;

            // parse the command line
            int i = 0;
            while (i < args.length) {
                String s = args[i++];
                if ("-u".equals(s)) {
                    ui = true;
                } else if ("-c".equals(s)) {
                    ui = false;
                } else if ("-q".equals(s)) {
                    quiet = true;
                } else if ("-e".equals(s)) {
                    Player.catchExceptions = true;
                    Player.debuggable = false; // enables time-keeping
                    Player.logToFile = true;
                } else if ("-r".equals(s)) {

                    r = Integer.parseInt(args[i++]);

                } else if ("-j".equals(s)) {

                    thr = Integer.parseInt(args[i++]);

                } else if ("-s".equals(s)) {

                    sc = Integer.parseInt(args[i++]);

                } else if ("-t".equals(s)) {

                    tourn = true;

                } else if ("-m".equals(s)) {
                    long randomSeed = 1L;
                    try {
                        randomSeed = Integer.parseInt(args[i]);
                        i++; // parsing worked
                    } catch (NumberFormatException e) {
                        // no number given
                    }
                    random.setSeed(randomSeed++);
                    Scenario.rand.setSeed(randomSeed++);
                    GameBoard.rand.setSeed(randomSeed++);
                    Move.rand.setSeed(randomSeed++);
                } else if ("-p".equals(s)) {
                    pauseInitially = true;
                } else if ("--secret".equals(s)) {

                    Scenario.useSecretScenarioClass = true;

                } else {
                    players.add(PlayerFactory.getClassForName(s));
                }
            }

            if (players.size() > 0) {

                was.Tournament.run(players, r, ui, sc, tourn, true, thr); // m, n, k,

            } else {
                logerr("Usage: java -jar WolvesAndSheep.jar -r R -s S -t -e -p -c -q CLASS1 CLASS2 CLASS3 CLASS4 CLASS5 (...)");
                //logerr("       -t M,N,K  ==> play a M*N board with K sheep.");
                logerr("       -r R      ==> play R repeats of each game.");
                logerr("       -j N      ==> use N threads simultaneously");
                logerr("       -s S      ==> set up scenario no. S (0 or default for random)");
                logerr("       -t        ==> play a tournament of all combinations of players (4 sheep, one wolf)");
                logerr("       -e        ==> catch and log player's exceptions and timeouts, but keep running.");
                logerr("                     Log player output to file.  For testing against buggy opponents.");
                logerr("       -m        ==> use same randomization of initial player positions etc.");
                logerr("       -m S      ==> use randomization number S for initial player positions etc.");
                logerr("       -p        ==> pause initially if using graphical UI");
                logerr("       -c        ==> do not show the graphical user interface ");
                logerr("       -q        ==> do not print progress info ");
                logerr("Example: java -jar WolvesAndSheep.jar -r 10 basic.Wolf basic.Sheep basic.Sheep basic.Sheep basic.Sheep");
                logerr("Example for NetBeans (Run Configuration, Program arguments): -r 10 basic.Wolf basic.Sheep basic.Sheep basic.Sheep basic.Sheep");
                // do not run a default case to make sure it doesn't cause confusion.
                //            was.Tournament.run("reitter.SheepPlayer,reitter.WolfPlayer,reitter.SheepPlayer,reitter.SheepPlayer, reitter.SheepPlayer", 100);

                // for testing prposes:  greene.Wolf zielenski.Wolf Wilkinson.Wolf derhammer.Sheep chan.Sheep derhammer.Sheep tailor.Sheep
                // (greene is one of the stronger wolves.)
            }
        } finally {
            System.exit(0);
        }
    }

    @Override
    public void wolfEatSheep(Player wolf, Player sheep) {
        // keep track
        eatingScore.inc(wolf.getClass().getName() + "\\" + sheep.getClass().getName());
    }
}
