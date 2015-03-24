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
       String[] sheepteams = new String[]{
            // Section 1
            "001-Sheep1:lara,reed,vliet,weiland",
            "001-Sheep2:daouphars,hatzell,ohliger,vella",
            "001-Sheep3:dodds,stackpole,warner,williams",
            "001-Sheep4:bardusch,mcdaniel,page,rath",
            "001-Sheep5:agraviador,lee,myers,swatsworth",
            // Section 2
            "002-Sheep1:guzman,fabrizio,hanson,vining",
            "002-Sheep2:butler,geroski,houstian,jordan",
            "002-Sheep3:callahan,kahlbaugh,miao,signorino",
            "002-Sheep4:hohman,kelly,stachniewicz,steitz",
            "002-Sheep5:lin,moore,kavya,walton",
            "002-Sheep6:akins,chiu,hersh,rickley"};
        // team scores are averages, not sums - so team size doesn't matter
        String[] wolves = new String[]{
            // Section 1
            "001-Wolf1:allan,fritz,mak,schroeter",
            "001-Wolf2:hanahan,kramer,mardis,preston",
            "001-Wolf3:atkins,dehnert,hong,sullivan",
            "001-Wolf4:gillin,kim,patel",
            // Section 2
            "002-Wolf1:anderson,barbone,ngwira,pham",
            "002-Wolf2:alvarado,carney,nolasco,salonick"
        };
        
        run(wolves, sheepteams, repeats, minutes);
    }

    static void run(String[] wolves, String[] sheepteams, int repeats, int minutes) {

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
            Scenario.repeatCount++;
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
                        for (int sp : Scenario.getParameterValues()) {
                            if (avgtimeperrun > 0) {
                                System.out.printf("running (%s out of %s).  %s mins. left\n", runcount, totalRuns, (int) (((totalRuns - runcount) * avgtimeperrun) / 1000 / 60));
                            }
                            runcount++;
                            //Scenario sc = Scenario.makeScenario(sp);
                            //for (int sc = 1; sc < Scenario.NUMSCENARIOS && exitRequested==false; sc++) {
                            Tournament t = run(p, repeats, false, sp, false, false);
                            totalHighscore.addHighScore(t.highscore);
                            totalTiming.addHighScore(t.timing);
                            totalEatingScore.addHighScore(t.eatingScore);
                            if (scenarioHighScore.get(t.scenario.toString()) == null) {
                                scenarioHighScore.put(t.scenario.toString(), new HighScore().setTitle(t.scenario.toString()));
                                scenarioTiming.put(t.scenario.toString(), new HighScore().setTitle(t.scenario.toString()));
                            }
                            scenarioHighScore.get(t.scenario.toString()).addHighScore(t.highscore);
                            scenarioTiming.get(t.scenario.toString()).addHighScore(t.timing);

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
        for (String s : sheepteams) {
            outln(s);
        }
        outln("Wolf teams:");
        for (String s : wolves) {
            outln(s);
        }
        outln("\n");
        totalHighscore.printByClass(scenarioHighScore.values());
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
            System.err.println("       -q        ==> do not print progress info ");
        }
        teamStructureInit(r, duration);


    }
}
