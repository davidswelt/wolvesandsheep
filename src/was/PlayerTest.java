package was;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import static was.Player.catchExceptions;
import static was.Tournament.logPlayerCrash;

/**
 * Test of the player
 *
 * @author dr
 */
public class PlayerTest {

    static HighScore log;
    static boolean mayNotInheritFrom(Class playerClass, String other)
            throws ClassNotFoundException
    {
        if (Class.forName(other).isAssignableFrom(playerClass) && Class.forName(other) != playerClass) {
                String str = String.format("Class %s inherits from %s.\n", playerClass.getName(), other);
                log.inc(str);
                System.err.println(str);
                return true;
            }
        return false;
    }
    public static boolean runTest(Class playerClass, HighScore log) {
        PlayerTest.log = log;
        try {
            String str;
            boolean isWolf;
            if (Class.forName("was.SheepPlayer").isAssignableFrom(playerClass)) {
                isWolf = false;
            } else if (Class.forName("was.WolfPlayer").isAssignableFrom(playerClass)) {
                isWolf = true;
            } else {
                str = String.format("Class %s is neither derived from was.WolfPlayer nor from was.SheepPlayer.\n", playerClass.getName());
                log.inc(str);
                System.err.println(str);
                return false;
            } 
            
            if ( mayNotInheritFrom(playerClass, "reitter.Wolf") ||
                    mayNotInheritFrom(playerClass, "greene.Wolf") ||
                    mayNotInheritFrom(playerClass, "reitter.Sheep"))
            {
                return false;
            }
                    
            return true;
        } catch (ClassNotFoundException ex) {
            // reitter.* may not be included
            return true;
        }
    }

    public static boolean runTest(String playerClassStr, HighScore log) {

        // wolf or sheep?
        boolean isWolf;

        // we accept package names

        Class playerClass = null;

        String[] names = new String[]{playerClassStr + ".Wolf", playerClassStr + ".Sheep", playerClassStr};

        for (String cn : names) {
            try {
                playerClass = Class.forName(cn);
            } catch (ClassNotFoundException ex) {
            }
        }

        if (playerClass == null) {
            log.inc("Class coud not be found in supplied jar files: " + playerClassStr);
            System.err.println("Class coud not be found in supplied jar files: " + playerClassStr);
            System.err.println("Did you give it in the form of packagename.classname?");
            System.err.println("Did you put your class in the right package?");
            System.err.println("Did you make sure the package is included?");
            return false;
        }
        return runTest(playerClass, log);

    }

    static boolean runUnitTest(Class cl, HighScore log) {

        try {
            Method method = cl.getMethod("test", (Class[]) null);
            if (method == null) {
                logPlayerCrash(cl, new RuntimeException("Player has no valid 'public static boolean test()' method.  Proceeding."));
                return true; // no unit test
            }
            try {


                boolean result = (Boolean) method.invoke(null);

                if (result) {
                    return true;
                }
                logPlayerCrash(cl, new RuntimeException("Player failed unit test."));


            } catch (IllegalAccessException ex) {
                Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RuntimeException ex) {
                if (Player.catchExceptions) {
                    logPlayerCrash(cl, new RuntimeException("Player crashed during unit test.  Proceeding."));
                    return true; // no unit test
                } else {
                    throw ex;
                }
            }

        } catch (NoSuchMethodException ex) {
            logPlayerCrash(cl, new RuntimeException("Player has no unit test.  Proceeding."));
            return true; // no unit test
        } catch (SecurityException ex) {
            Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
            logPlayerCrash(cl, new RuntimeException("Player unit test violated security rule.  Proceeding."));
            return true;
        }

        return false;

    }

    public static void main(String args[]) {

        // parse the command line
        int i = 0;
        boolean pass = true;
        while (i < args.length) {
            String s = args[i++];

            System.out.println("Testing " + s);
            if (runTest(s, new HighScore())) {
                System.out.println(s+" passed.");
            } else {
                System.out.println(s+" failed.");
                pass = false;
            }

        }
        if (args.length == 0) {
            System.err.println("Usage: java -jar W -classpath .:./players/ was.PlayerTest PACKAGENAME.CLASS");
            System.err.println("Put player .jar files into players/");
        }
        System.exit(pass ? 0 : 1);
    }
}
