package was;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static was.Tournament.dividerLine;
import static was.Tournament.quiet;

/**
 *
 * @author dr
 */
public class IST240Tournament extends Tournament {

    public static void ist240(int repeats, int minutes) {
       
        String[] sheepteams = new String[]{"TABZ:flanigan,lipshutz,senat,zomok",
            "Team B:hancharik,hilliard,lutsenko,rump",
            "Team C:cao,charles,hoffman,zwierzynski",
            "Old Mutton (Classic):derhammer,dori,vickery,dori,tailor"};
        // team scores are averages, not sums - so team size doesn't matter
        String[] wolves = new String[]{"Wolfenstein:bellisario,petcu,redman",
            "Wolfram Alphas:fung,robb,zhao",
            "Meat Eater (Classic):greene,zielenski,gehr,wilkinson"};
        
        HighScore totalHighscore = new HighScore().setTitle("total");
        HighScore totalTiming = new HighScore().setTitle("timing");
        Map<String, HighScore> scenarioHighScore = new TreeMap();
        Map<String, HighScore> scenarioTiming = new TreeMap();
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
            for (String s : sheepteams) {
                // each sheep team
                String sheepteam = prefix(s);
                for (String w : wolves) {
                    // each wolf team
                    String wolfteam = prefix(w);
                    ArrayList<Class> wolves2 = PlayerFactory.string2classlist(w, ".Wolf");
                    System.out.print("Wolf team:" + wolfteam + ": ");
                    for (Class w2 : wolves2) // for each wolf within a group
                    {
                        ArrayList<Class> p = PlayerFactory.string2classlist(s, ".Sheep"); // all sheep
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
                                System.out.printf("running (%s out of %s).  %s mins. left\n", runcount, totalRuns, (int) (((totalRuns - runcount) * avgtimeperrun) / 1000 / 60));
                            }
                            runcount++;
                            //Scenario sc = Scenario.makeScenario(sp);
                            //for (int sc = 1; sc < Scenario.NUMSCENARIOS && exitRequested==false; sc++) {
                            Tournament t = run(p, repeats, false, sp, false, false);
                            totalHighscore.addHighScore(t.highscore);
                            totalTiming.addHighScore(t.timing);
                            if (scenarioHighScore.get(t.scenario.toString()) == null) {
                                scenarioHighScore.put(t.scenario.toString(), new HighScore().setTitle(t.scenario.toString()));
                                scenarioTiming.put(t.scenario.toString(), new HighScore().setTitle(t.scenario.toString()));
                            }
                            scenarioHighScore.get(t.scenario.toString()).addHighScore(t.highscore);
                            scenarioTiming.get(t.scenario.toString()).addHighScore(t.timing);
                            
                               
                            System.out.println("Timing (ms.):");
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
        System.out.println("###########################"); // marker for processing script
        System.out.print(dividerLine);
        System.out.println("IST240 Tournament results:");
        for (String s : sheepteams) {
            System.out.println(s);
        }
        for (String s : wolves) {
            System.out.println(s);
        }
        System.out.println("\n");
        totalHighscore.printByClass(scenarioHighScore.values());
        System.out.print(dividerLine);
        System.out.println("Timing (ms.):");
        totalTiming.printByClass(scenarioTiming.values());
        System.out.println(dividerLine);
        System.out.println("Player Crashes:");
        crashLog.printByCategory(null);
        System.out.println(dividerLine);
       
    }

    public IST240Tournament(List<Class> playerClasses, int r, boolean ui) {
        super(playerClasses, r, ui);
        throw new RuntimeException("not implemented.");
                
    }

    public static void main(String args[]) {

        Player.catchExceptions = true;
        Player.logToFile = true;

        ArrayList<Class> players = new ArrayList<Class>();
//        int m = -1;
//        int n = -;
//        int k = 4;
        int r = 1;
        int sc = 0; // random scenario

        boolean tourn = false;
        int duration = 0;

        // parse the command line
        int i = 0;
        while (i < args.length) {
            String s = args[i++];
            if (s.equals("-q")) {
                quiet = true;
            } else if (s.equals("-r")) {

                r = Integer.parseInt(args[i++]);

            } else if (s.equals("-d")) {

                duration = Integer.parseInt(args[i++]);

            } else if (s.equals("--secret")) {

                Scenario.useSecretScenarioClass = true;

            }
        }

        System.err.println("Usage: java -jar WolvesAndSheep.jar -r R -s S -t -e -p -c -q CLASS1 CLASS2 CLASS3 CLASS4 CLASS5 (...)");
        //System.err.println("       -t M,N,K  ==> play a M*N board with K sheep.");
        System.err.println("       -r R      ==> play R repeats of each game.");
        System.err.println("       -q        ==> do not print progress info ");

        ist240(r, duration);


    }
}
