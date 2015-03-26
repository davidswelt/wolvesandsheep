package was;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
 * This is the main (entry) class.
 * Usage: java -jar WolvesAndSheep.jar -r R -s S -t -e -p -c -q CLASS1 CLASS2 CLASS3 CLASS4 CLASS5 (...)
 *       -r R      == play R repeats of each game.
 *       -s S      == set up scenario no. S (0 or default for random)
 *       -t        == play a tournament of all combinations of players (4 sheep, one wolf)
 *       -e        == ignore player's exceptions
 *       -p        == pause initially if using graphical UI
 *       -c        == do not show the graphical user interface 
 *       -q        == do not print progress info 
 * Example: java -jar WolvesAndSheep.jar -r 10 basic.Wolf basic.Sheep basic.Sheep basic.Sheep basic.Sheep
 * Example for NetBeans (Run, Project Configuration, Arguments): -r 10 basic.Wolf basic.Sheep basic.Sheep basic.Sheep basic.Sheep
 * 
 *
 * @author dr
 */
public class Tournament implements GameBoard.WolfSheepDelegate {

    protected GameBoard eboard;
    protected ArrayList<Class> players = new ArrayList<Class>();
    protected static Random random = new Random();
    Scenario scenario = null;
    int initNumSheep = 4;
    int initNumWolves = 1;
    int numSheep, numWolves;
    protected static Map<Object, String> teams = new HashMap();
    static int minNumSheepRequiredToRun = 0;  // run won't start a game otherwise
    static int minNumWolvesRequiredToRun = 0;
    static boolean exitRequested = false;
    static boolean pauseInitially = false;
    static boolean quiet = false;
    static HighScore crashLog = new HighScore().setTitle("crashes");
    static HighScore moveLog = new HighScore().setTitle("total calls to move()");
    static int loadedScenario = -1; // used for logging crashes

    static void logPlayerMoveAttempt(Class pl) {
        moveLog.inc(pl.getName() + ".Crash");
        if (loadedScenario > -1) {
            String ss = "Scenario" + String.format("%2d", loadedScenario);

            moveLog.inc(pl.getName() + ".Crash." + ss);
        }
    }

    static void logPlayerCrash(Class pl, Throwable ex) {
        logPlayerCrash(pl, ex, loadedScenario);
    }

    static void logPlayerCrash(Class pl, Throwable ex, Integer info) {
        crashLog.inc(pl.getName() + ".Crash");
//        crashLog.inc(pl.getName() + ".Crash\\" + ex);

        if (info > -1) {
            String ss = "Scenario" + String.format("%2s", info.toString());
            crashLog.inc(pl.getName() + ".Crash." + ss);
            crashLog.inc(pl.getName() + ".Crash." + ss + "\\" + ex);
        }
    }
    HighScore timing = new HighScore();
    HighScore scenarioTiming = new HighScore();
    HighScore highscore = new HighScore();
    HighScore scenarioScore = new HighScore();
    HighScore eatingScore = new HighScore();

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

        run(playerClasses, r, false, 0, true, true);
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
     * @return resulting Tournament object.
     */
    static public Tournament run(List<Class> playerClasses, int r, boolean ui, int scenario, boolean comb, boolean printHighScore) {

        Tournament t;


        t = new Tournament(playerClasses, r, ui);

        int totalgames = r * playerClasses.size() * Math.max(1, playerClasses.size() - 1) * Math.max(1, playerClasses.size() - 2) * Math.max(1, playerClasses.size() - 3);

//        logerr("Total trials: " + totalgames);



        try {
            t.start(totalgames > 100000 && !quiet, scenario, r, comb);
        } finally {
            if (printHighScore) {
                t.highscore.print();
                System.out.print(dividerLine);
                t.scenarioScore.printByCategory(null);
                System.out.print(dividerLine);
                System.out.println("Player Crashes:");
                crashLog.printByCategory(Arrays.asList(moveLog));

                System.out.println(dividerLine);
                System.out.println("Timing:");
                t.timing.print();

            }
//        t.timing.print();
        }


        return t;
    }

    /**
     * starts the tournament.
     *
     */
    TreeMap<String, Double> start(boolean printHighscores, int scenario, int repeats, boolean combinations) {

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

        startT(printHighscores, sp, scenario, repeats);

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

    void startT(boolean printHighscores, List<Integer> selectedPlayers, int scenarioNum, int repeats) {
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

            if (!quiet) {
                System.out.println("" + sheepComb.size() + " sheep teams, " + wolvesComb.size() + " wolves, " + repeats + " reps.");
            }

            for (int r = 0; r < repeats && exitRequested == false; r++) {

                int theSc = scenarioNum;
                if (scenarioNum == 0 && repeats >= Scenario.getParameterValues().size()) {
                    theSc = 1 + (r % Scenario.getParameterValues().size());
                }

                scenario = Scenario.makeScenario(theSc);
                loadedScenario = theSc;

                for (ArrayList selWolfComb : wolvesComb) {
                    for (ArrayList selSheepComb : sheepComb) {
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

                        GameBoard board = new GameBoard(scenario.boardSize(), scenario.boardSize(), boardUI, 80);
                        board.wolfEatSheepDelegate = this;
                        board.scenario = scenario;

                        Stack<GameLocation> wolfQueue = new Stack();
                        Stack<GameLocation> sheepQueue = new Stack();

                        scenario.addToBoard(board, wolfQueue, sheepQueue); // add scenario first to occupy these spaces

//                      logerr("sel pl len="+selectedPlayers.size());
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
                                board.printPlayerOverview();
                                // board.print();
                            }

                            try {
                                Map<Player, int[]> s = board.playGame(pauseInitially);

                                for (Map.Entry<Player, int[]> score : s.entrySet()) {
                                    Class cl = score.getKey().getClass();
                                    if (score.getKey().isIncludedInHighScore()) {
                                        highscore.inc(cl.getName(), score.getValue()[0]);

                                        if (teams.get(cl) != null) { // Pastures etc don't have a team

                                            highscore.inc(teams.get(cl) + (score.getKey() instanceof WolfPlayer ? ".WolfTeam" : ".SheepTeam"), score.getValue()[0]);
                                        }

                                        timing.inc(cl.getName(), score.getKey().meanRunTime());
                                        final String scenPlayStr = "Scenario " + scenario.toString() + "\\" + cl.getName();
                                        scenarioTiming.inc(scenPlayStr, score.getKey().meanRunTime());
                                        scenarioScore.inc(scenPlayStr, score.getValue()[0]);
                                        timing.noteUse(cl.getName());
                                        scenarioTiming.noteUse(scenPlayStr);
                                        score.getValue()[0] = 0; // set to 0 to make sure it doesn't get added twice

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
                } else {
                    if (PlayerTest.runUnitTest(p, crashLog)) {
                        highscore.inc(p.getName(), 0.0);
                        players.add(p);
                    }
                }
            }
        }

        // adjust timeout
        //TIMEOUT = (long) ((float) TIMEOUT * (float) Benchmark.runBenchmark());
    }
    static String dividerLine = "_______________________________________________________________________________________________________________________________\n\n";
    // static init
    private static boolean secPolicySet = false;

    {
        if (!secPolicySet) {


            ClassLoader cl = Tournament.class.getClassLoader();
            java.net.URL policyURL = cl.getResource("sandbox2.policy");
            System.setProperty("java.security.policy", policyURL.toString());
            SecurityManager sm = new SecurityManager();
            System.setSecurityManager(sm);
            secPolicySet = true;
        }

    }
    
    static void logerr(String s)
    {
//        Logger.getLogger(Tournament.class
//                    .getName()).log(Level.INFO, s);
        System.err.println(s);
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
                    Player.logToFile = true;
                } else if ("-r".equals(s)) {

                    r = Integer.parseInt(args[i++]);

                } else if ("-s".equals(s)) {

                    sc = Integer.parseInt(args[i++]);

                } else if ("-t".equals(s)) {

                    tourn = true;

                } else if ("-p".equals(s)) {

                    pauseInitially = true;

                } else if ("--secret".equals(s)) {

                    Scenario.useSecretScenarioClass = true;

                } else {
                    players.add(PlayerFactory.getClassForName(s));
                }
            }



            if (players.size() > 0) {

                was.Tournament.run(players, r, ui, sc, tourn, true); // m, n, k,

            } else {
                logerr("Usage: java -jar WolvesAndSheep.jar -r R -s S -t -e -p -c -q CLASS1 CLASS2 CLASS3 CLASS4 CLASS5 (...)");
                //logerr("       -t M,N,K  ==> play a M*N board with K sheep.");
                logerr("       -r R      ==> play R repeats of each game.");
                logerr("       -s S      ==> set up scenario no. S (0 or default for random)");
                logerr("       -t        ==> play a tournament of all combinations of players (4 sheep, one wolf)");
                logerr("       -e        ==> ignore player's exceptions");
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
