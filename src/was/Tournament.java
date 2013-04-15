package was;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
 * The Tournament class describes and manages the game tournament. This is the
 * main (entry) class.
 *
 * @author dr
 */
public class Tournament {

    protected ArrayList<Class> disqualifiedPlayers = new ArrayList<Class>();
    protected GameBoard eboard;
    protected ArrayList<Class> players = new ArrayList<Class>();
    protected static Random random = new Random();
    Scenario scenario = null;
    int initNumSheep = 4;
    int initNumWolves = 1;
    int numSheep, numWolves;
    protected static Map<Class, String> teams = new HashMap();
    static int minNumSheepRequiredToRun = 0;  // run won't start a game otherwise
    static int minNumWolvesRequiredToRun = 0;
    static boolean exitRequested = false;
    static boolean pauseInitially = false;
    static boolean quiet = false;

    static Class name2class(String name, String[] postfix) {
        // we'll try different variants

        String[] n = new String[4];
        n[0] = name;
        n[1] = name.toLowerCase();
        n[2] = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        n[3] = name.toUpperCase();

        if (postfix == null) {
            postfix = new String[0];
        }

        Class c = null;
        for (String s : n) {
            try {
                for (String p : postfix) {
                    try {
                        c = Class.forName(s + p);
                    } catch (ClassNotFoundException ex) {
                    }
                }
            } catch (SecurityException ex) {
                Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (c != null) {
                break;
            }
        }

        if (c == null) {
            System.err.print("No such class: " + n[1] + postfix);
//            for (String s : n) {
//                System.err.print(s + postfix + ", ");
//            }
            System.err.println("");
        }

        return c;
    }
    static HighScore crashLog = new HighScore().setTitle("crashes");

    static void logPlayerCrash(Class pl, Exception ex) {
        crashLog.inc(pl.getName() + ".Crash");
        crashLog.inc(pl.getName() + ".Crash\\" + ex);
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
            } catch (RuntimeException ex) {

                if (Player.catchExceptions) {
                    if (!Player.logToFile) {
                        Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    logPlayerCrash(cl, ex);
                } else {
                    throw ex;
                }
            } catch (InvocationTargetException ex) {
                if (!Player.logToFile) {
                    Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
                }
                logPlayerCrash(cl, ex);
                if (!Player.catchExceptions) {

                    throw new RuntimeException("Exception in Player constructor!");


                }
            }
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
        }



        return null;
    }
    HighScore timing = new HighScore();
    HighScore highscore = new HighScore();
    HighScore scenarioScore = new HighScore();

    static ArrayList<Class> string2classlist(String listofPlayerClassNames, String postfix) {

        StringTokenizer st = new StringTokenizer(listofPlayerClassNames, ":");
        if (st.hasMoreElements()) {
            st.nextToken();
            if (st.hasMoreElements()) {
                listofPlayerClassNames = st.nextToken();
            }
        }

        String[] pfa = new String[]{postfix};



        ArrayList<Class> players = new ArrayList<Class>();
        st = new StringTokenizer(listofPlayerClassNames, ", ");

        while (st.hasMoreElements()) {
            Class cs = name2class(st.nextToken(), pfa);
            if (cs != null) {
                players.add(cs);
            }
        }


        return players;
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


        run(string2classlist(listofPlayerClassNames, ""), repeats);

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
     */
    static public Tournament run(List<Class> playerClasses, int r, boolean ui, int scenario, boolean comb, boolean printHighScore) {

        Tournament t;


        t = new Tournament(playerClasses, r, ui);

        int totalgames = r * playerClasses.size() * Math.max(1, playerClasses.size() - 1) * Math.max(1, playerClasses.size() - 2) * Math.max(1, playerClasses.size() - 3);

//        System.err.println("Total trials: " + totalgames);



        try {
            t.start(totalgames > 100000 && !quiet, scenario, r, comb);
        } finally {
            if (printHighScore) {
                t.highscore.print();
                System.out.print(dividerLine);
                t.scenarioScore.printByCategory(null);
                System.out.print(dividerLine);
                System.out.println("Player Crashes:");
                crashLog.printByCategory(null);

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
                for (Integer i=0; i<players.size(); i++) {
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
            
            if (! quiet)
            {
                System.out.println(""+sheepComb.size()+" sheep teams, "+wolvesComb.size()+" wolves, "+repeats+" reps.");
            }
            
            for (int r = 0; r < repeats && exitRequested == false; r++) {

                int theSc = scenarioNum;
                if (scenarioNum == 0 && repeats >= Scenario.getParameterValues().size()) {
                    theSc = 1 + (r % Scenario.getParameterValues().size());
                }

                scenario = Scenario.makeScenario(theSc);

                for (ArrayList selWolfComb : wolvesComb) {
                    for (ArrayList selSheepComb : sheepComb) {
                        selectedPlayers = new ArrayList();
                        selectedPlayers.addAll(selWolfComb);
                        selectedPlayers.addAll(selSheepComb);


                        GameBoard board = new GameBoard(scenario.boardSize(), scenario.boardSize(), boardUI, 80);


                        Stack<GameLocation> wolfQueue = new Stack();
                        Stack<GameLocation> sheepQueue = new Stack();

                        scenario.addToBoard(board, wolfQueue, sheepQueue); // add scenario first to occupy these spaces

//                      System.err.println("sel pl len="+selectedPlayers.size());
                        for (Integer i : selectedPlayers) {
                            Class plClass = players.get(i);
                            Stack<GameLocation> queue = isWolf(plClass) ? wolfQueue : sheepQueue;
                            GameLocation initialLocation = queue.empty() ? board.randomEmptyLocation(wolfQueue, sheepQueue) : queue.pop();

                            Player player = playerFactory(plClass, (isWolf(plClass) ? "w" : "s"));
                            if (player != null) {
                                board.addPlayer(player, initialLocation);
                            }
                            // note use even if player wasn't added (due to crash!)
                            highscore.noteUse(plClass.getName());
                            scenarioScore.noteUse("Scenario " + scenario.toString() + "\\" + plClass.getName());

                            if (teams.get(plClass) == null) {
                                //System.err.println(i);
                                //System.err.println("WARNING: can't get team for " + players.get(i));
                            } else {
                                // note use of player so that team score can be normalized later
                                highscore.noteUse(teams.get(plClass) + (isWolf(plClass) ? ".WolfTeam" : ".SheepTeam"));
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
                                board.print();
                            }

                            try {
                                Map<Player, int[]> s = board.playGame(pauseInitially);

                                for (Map.Entry<Player, int[]> score : s.entrySet()) {
                                    Class cl = score.getKey().getClass();
                                    highscore.inc(cl.getName(), score.getValue()[0]);

                                    if (teams.get(cl) != null) { // Pastures etc don't have a team

                                        highscore.inc(teams.get(cl) + (score.getKey() instanceof WolfPlayer ? ".WolfTeam" : ".SheepTeam"), score.getValue()[0]);
                                    }
                                    timing.inc(cl.getName(), score.getKey().meanRunTime());


                                    scenarioScore.inc("Scenario " + scenario.toString() + "\\" + cl.getName(), score.getValue()[0]);

                                    timing.noteUse(cl.getName());
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
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Tournament.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
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
                    System.err.println(
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
    static String dividerLine = "__________________________________________________________________________________________\n\n";

    public static void ist240(int repeats) {
        String[] sheepteams = new String[]{
            "Black Sheep:CHHITH,GEISER,HAFAIRI,HOFBAUER",
            "Creepy Sheepies:CONTINO,GARRITY,HOFFMAN,TAILOR", // 
            "Dolly's Den:DERHAMMER,DERHAMMER,CHAN,CHAN", //TUBERGEN  // BROADWATER   MUST REPEAT MISSING STUDENTS (4 sheep guaranteed)
            "White Sheep:BONCHONSKY,HE,SUON,USCAMAYTA",
            "Nervous Wreck:REITTER,REITTER,REITTER,REITTER"
        };

        String[] wolves = new String[]{
            "Hungry Beast:MONDELL,MULLEN,MUNOZ",
            "Lone Hunters:CHEETHAM,KIDNEY,LAFFERTY",
            "Furry Fury:REIZNER,SICINSKI,ZIELENSKI",
            "The Gray:NORANTE,RAUGH,ULIANA",
            "Wolf in Sheep's Clothing:GREENE,WILKINSON,YOSUA",
            "Meat Eater:REITTER"
        };

        HighScore totalHighscore = new HighScore().setTitle("total");
        HighScore totalTiming = new HighScore().setTitle("timing");

        Map<String, HighScore> scenarioHighScore = new TreeMap();

        minNumSheepRequiredToRun = 1;
        minNumWolvesRequiredToRun = 1;

        int totalRuns = 0;
        for (String wteam : wolves) {

            totalRuns += string2classlist(wteam, ".Wolf").size();
        }
        totalRuns = totalRuns * sheepteams.length * Scenario.getParameterValues().size();
        int runcount = 1;
        long startTime = System.currentTimeMillis();

        for (String s : sheepteams) { // each sheep team
            String sheepteam = prefix(s);

            for (String w : wolves) { // each wolf team
                String wolfteam = prefix(w);

                ArrayList<Class> wolves2 = string2classlist(w, ".Wolf");

                System.out.print("Wolf team:" + wolfteam + ": ");

                for (Class w2 : wolves2) // for each wolf within a group
                {
                    teams.put(w2, wolfteam);

                    ArrayList<Class> p = string2classlist(s, ".Sheep"); // all sheep

                    for (Class sh : p) {
                        teams.put(sh, sheepteam);

                    }

                    p.add(w2); // one wolf

                    // randomize order of sheep
                    Collections.shuffle(p);

                    double avgtimeperrun = 0;
                    if (runcount > Scenario.getParameterValues().size()) {
                        avgtimeperrun = (System.currentTimeMillis() - startTime) / runcount;
                    }
                    // all scenarios
                    for (int sp : Scenario.getParameterValues()) {

                        if (avgtimeperrun > 0) {
                            System.out.printf("running (%s out of %s).  %s mins. left\n", runcount, totalRuns,
                                    (int) (((totalRuns - runcount) * avgtimeperrun) / 1000 / 60));
                        }
                        runcount++;

                        //Scenario sc = Scenario.makeScenario(sp);
                        //for (int sc = 1; sc < Scenario.NUMSCENARIOS && exitRequested==false; sc++) {
                        Tournament t = run(p, repeats, false, sp, false, false);
                        totalHighscore.addHighScore(t.highscore);
                        if (scenarioHighScore.get(t.scenario.toString()) == null) {
                            scenarioHighScore.put(t.scenario.toString(), new HighScore().setTitle(t.scenario.toString()));
                        }
                        scenarioHighScore.get(t.scenario.toString()).addHighScore(t.highscore);
                        totalTiming.addHighScore(t.timing);

                        if (exitRequested) {
                            break;
                        }
                    }
                    totalHighscore.printByCategory(null);

                }
            }
        }

        System.out.print(dividerLine);
        System.out.println("IST240 Tournament results:");
        for (String s : sheepteams) {
            System.out.println(s);
        }
        for (String s : wolves) {
            System.out.println(s);
        }
        System.out.println("\n");

        totalHighscore.printByCategory(scenarioHighScore.values());

        System.out.print(dividerLine);
        System.out.println("Player Crashes:");
        crashLog.printByCategory(null);

        System.out.println(dividerLine);
        totalTiming.print();

        System.out.println(dividerLine);


    }
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

    public static void main(String args[]) {


        ArrayList<Class> players = new ArrayList<Class>();
//        int m = -1;
//        int n = -;
//        int k = 4;
        int r = 1;
        int sc = 0; // random scenario
        boolean ui = true;
        boolean run240 = false;
        boolean tourn = false;

        // parse the command line
        int i = 0;
        while (i < args.length) {
            String s = args[i++];
            if (s.equals("-u")) {
                ui = true;
            } else if (s.equals("-c")) {
                ui = false;
            } else if (s.equals("-q")) {

                quiet = true;

            } else if (s.equals("-e")) {
                Player.catchExceptions = true;
                Player.logToFile = true;
            } else if (s.equals("-r")) {

                r = Integer.parseInt(args[i++]);

            } else if (s.equals("-ist240")) {

                run240 = true;

            } else if (s.equals("-s")) {

                sc = Integer.parseInt(args[i++]);

            } else if (s.equals("-t")) {

                tourn = true;

            } else if (s.equals("-p")) {

                pauseInitially = true;

            } else {
                players.add(name2class(s, new String[]{"", ".Wolf", ".Sheep"}));
            }
        }


        if (run240) {
            ist240(r);
        } else {
            if (players.size() > 0) {

                was.Tournament.run(players, r, ui, sc, tourn, true); // m, n, k,

            } else {
                System.err.println("Usage: java -jar WolvesAndSheep.jar -r R -s S -t -e -p -c -q CLASS1 CLASS2 CLASS3 CLASS4 CLASS5 (...)");
                //System.err.println("       -t M,N,K  ==> play a M*N board with K sheep.");
                System.err.println("       -r R      ==> play R repeats of each game.");
                System.err.println("       -s S      ==> set up scenario no. S (0 or default for random)");
                System.err.println("       -t        ==> play a tournament of all combinations of players (4 sheep, one wolf)");
                System.err.println("       -e        ==> ignore player's exceptions");
                System.err.println("       -p        ==> pause initially if using graphical UI");
                System.err.println("       -c        ==> do not show the graphical user interface ");
                System.err.println("       -q        ==> do not print progress info ");
                System.err.println("Example: java -jar WolvesAndSheep.jar -r 10 basic.Wolf basic.Sheep basic.Sheep basic.Sheep basic.Sheep");
                System.err.println("Example for NetBeans (Run Configuration, Program arguments): -r 10 basic.Wolf basic.Sheep basic.Sheep basic.Sheep basic.Sheep");
                // do not run a default case to make sure it doesn't cause confusion.
                //            was.Tournament.run("reitter.SheepPlayer,reitter.WolfPlayer,reitter.SheepPlayer,reitter.SheepPlayer, reitter.SheepPlayer", 100);

                // for testing purposes:  greene.Wolf zielenski.Wolf Wilkinson.Wolf derhammer.Sheep chan.Sheep derhammer.Sheep tailor.Sheep
                // (greene is one of the stronger wolves.)
            }
        }
        System.exit(0);
    }
}
