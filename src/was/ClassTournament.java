package was;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static was.Tournament.crashLog;
import static was.Tournament.dividerLine;
import static was.Tournament.quiet;

/**
 *
 * @author dr
 */
public class ClassTournament extends Tournament {

    static void out(String s) {
        System.out.print(s);
    }

    static void outln(String s) {
        System.out.println(s);
    }

    public static void teamStructureInit(int repeats, int minutes) {
       String[] sheepteams = new String[]{"Lambchop:aaa,bbb,ccc",  // hegarty probably dropped
            "Splendiferous Sheep:ddd,eee,fff",
            "Team C:ggg,hhh,iii",  // pipkin probably dropped
            "Team D:jjj,kkk,lll", // li probably dropped
            "Baaad Boys:mmm,nnn,ooo", // owen (dropped),   curtis, blickie
            "Team F:ppp,qqq,rrr",
            "Old Mutton (Classic):sss,ttt,uuu,vvv,xxx"};
        // team scores are averages, not sums - so team size doesn't matter
        String[] wolves = new String[]{
            "Wolfram Alphas:aa,bb,cc",  // mittal probably dropped
            "Team Wolfenstein:dd,ee,ff",
            "Team C:gg,hh,ii",
            "Meat Eater (Classic):jj,kk,ll,mm"};
        
        run(wolves, sheepteams, repeats, minutes);

        run(wolves, sheepteams, repeats, minutes, numThreads);
    }

    static void run(String[] wolves, String[] sheepteams, int repeats, int minutes, int numThreads) {

        HighScore totalHighscore = new HighScore().setTitle("total");
        HighScore totalTiming = new HighScore().setTitle("timing");
        Map<String, HighScore> scenarioHighScore = new TreeMap();
        Map<String, HighScore> scenarioTiming = new TreeMap();
        HighScore totalEatingScore = new HighScore().setTitle("eating");
        for (String player : sheepteams) {
            // each sheep team
            String sheepteam = prefix(player);
            // all sheep
            for (Class sh : PlayerFactory.string2classlist(player, ".Sheep")) {
                teams.put(sh.getName(), sheepteam);
                teams.put(sh, sheepteam);
            }
            for (String sh : PlayerFactory.string2missingClasslist(player, ".Sheep")) {
                teams.put(sh, sheepteam);
            }
        }
        for (String player : wolves) {
            // each wolf team
            String wolfteam = prefix(player);
            // all wolves
            for (Class sh : PlayerFactory.string2classlist(player, ".Wolf")) {
                teams.put(sh.getName(), wolfteam);
                teams.put(sh, wolfteam);
            }
            for (String sh : PlayerFactory.string2missingClasslist(player, ".Wolf")) {
                teams.put(sh, wolfteam);
            }
        }
        minNumSheepRequiredToRun = 1;
        minNumWolvesRequiredToRun = 1;
        int totalRuns = 0;
        for (String wteam : wolves) {
            totalRuns += PlayerFactory.string2classlist(wteam, ".Wolf").size();
        }
        totalRuns = totalRuns * sheepteams.length * Scenario.getParameterValues().size();
        int runcount = 1;
        long targetTimeSecs = Math.max(1, minutes * 60); // 10 minutes
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < targetTimeSecs * 1000) {
            Scenario.repeatCount+=numThreads;
            for (String s : sheepteams) {
                // each sheep team
                for (String w : wolves) {
                    // each wolf team
                    String wolfteam = prefix(w);
                    List<Class> wolves2 = PlayerFactory.string2classlist(w, ".Wolf");
                    out("Wolf team:" + wolfteam + ": ");
                    for (Class w2 : wolves2) // for each wolf within a group
                    {
                        List<Class> p = PlayerFactory.string2classlist(s, ".Sheep"); // all sheep
                        p.add(w2); // one wolf
                        // randomize order of sheep
                        Collections.shuffle(p);
                        double avgtimeperrun = 0;
                        if (runcount > Scenario.getParameterValues().size()) {
                            avgtimeperrun = (System.currentTimeMillis() - startTime) / runcount;
                        }
                        // all scenarios
                        //           List x = Scenario.getParameterValues();
                        // we're running one scenario at a time
                        for (int sp : Scenario.getParameterValues()) {
                            if (avgtimeperrun > 0) {
                                System.out.printf("running (%s out of %s).  %s mins. left\n", runcount, totalRuns, (int) (((totalRuns - runcount) * avgtimeperrun) / 1000 / 60));
                            }
                            runcount++;
                            //Scenario sc = Scenario.makeScenario(sp);
                            //for (int sc = 1; sc < Scenario.NUMSCENARIOS && exitRequested==false; sc++) {
                            Tournament t = run(p, repeats, false, sp, false, false, numThreads);
                            totalHighscore.addHighScore(t.highscore);
                            totalTiming.addHighScore(t.timing);
                            totalEatingScore.addHighScore(t.eatingScore);
                            
                            String ss = Scenario.toString(sp);
                            if (scenarioHighScore.get(ss) == null) {
                                scenarioHighScore.put(ss, new HighScore().setTitle(ss));
                                scenarioTiming.put(ss, new HighScore().setTitle(ss));
                            }
                            scenarioHighScore.get(ss).addHighScore(t.highscore);
                            scenarioTiming.get(ss).addHighScore(t.timing);

                            outln("Timing (ms.):");
                            t.timing.print();
                            totalTiming.print();
                            if (exitRequested) {
                                break;
                            }
                        }
                        totalHighscore.printByCategory(null);
                    }
                }
            }
        }
        outln("###########################"); // marker for processing script
        out(dividerLine);
        outln("TOURNAMENT");
        outln("Sheep teams:");

        for (String player : sheepteams) {
            out(prefix(player) + ": ");
            for (String sh : PlayerFactory.string2bracketedClasslist(player, ".Sheep")) {
                out(sh + " ");
            }
            outln("");
        }

        outln("Wolf teams:");

        for (String player : wolves) {
            out(prefix(player) + ": ");
            for (String sh : PlayerFactory.string2bracketedClasslist(player, ".Wolf")) {
                out(sh + " ");
            }
            outln("");
        }

        outln("\n");
        totalHighscore.printByClass(scenarioHighScore.values());
        out(dividerLine);
        outln("" + Scenario.repeatCount + " tournaments with " + Scenario.gameCount + " games in total played.");
        outln("Time: " + (System.currentTimeMillis() - startTime) / 1000.0 + "s elapsed.");

        out(dividerLine);
        outln("Timing (ms.):");
        totalTiming.printByClass(scenarioTiming.values());
        outln(dividerLine);
        outln(dividerLine);
        outln("Wolf attacks:");
        totalEatingScore.printAsTable();
        outln(dividerLine);
        outln("Player Crashes:");

        List li = new ArrayList();
        li.add(moveLog);
        crashLog.printByCategory(li);
        outln(dividerLine);

    }

    public ClassTournament(List<Class> playerClasses, int r, boolean ui) {
        super(playerClasses, r, ui);
        throw new RuntimeException("not implemented.");

    }

    public static void main(String args[]) {

        Player.catchExceptions = true;
        Player.logToFile = true;
        Player.debuggable = false; // enables time-keeping

        int r = 1;
        int numThreads = 1;
        int duration = 0;

        boolean printUsage = args.length == 0;
        // parse the command line
        int i = 0;
        while (i < args.length) {
            String s = args[i++];
            if ("-q".equals(s)) {
                quiet = true;
            } else if ("-r".equals(s)) {

                r = Integer.parseInt(args[i++]);

            } else if ("-d".equals(s)) {

                duration = Integer.parseInt(args[i++]);

            } else if ("-j".equals(s)) {

                numThreads = Integer.parseInt(args[i++]);

            } else if ("--secret".equals(s)) {

                Scenario.useSecretScenarioClass = true;

            } else {
                System.err.println(s + ": unknown parameter.");
                printUsage = true;
            }
        }

        if (printUsage) {
            System.err.println("Usage: java -jar WolvesAndSheep.jar -r R -s S -t -e -p -c -q CLASS1 CLASS2 CLASS3 CLASS4 CLASS5 (...)");
            //System.err.println("       -t M,N,K  ==> play a M*N board with K sheep.");
            System.err.println("       -r R      ==> play R repeats of each game.");
            System.err.println("       -d M      ==> repeat for at least M minutes.");
            System.err.println("       -q        ==> do not print progress info ");
        }
        teamStructureInit(r, duration, numThreads);

    }
}
