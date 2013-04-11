package was;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import static was.Tournament.logPlayerCrash;

/**
 * Test of the player
 *
 * @author dr
 */
public class PlayerTest {

    public static boolean runTest(Class playerClass, HighScore log) {
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
            if (Class.forName("reitter.Wolf").isAssignableFrom(playerClass) && Class.forName("reitter.Wolf") != playerClass) {
                str = String.format("Class %s inherits from reitter.Wolf.\n", playerClass.getName());
                log.inc(str);
                System.err.println(str);
                return false;
            } else if (Class.forName("reitter.Sheep").isAssignableFrom(playerClass)&& Class.forName("reitter.Sheep") != playerClass) {
                str = String.format("Class %s inherits from reitter.Sheep.\n", playerClass.getName());
                log.inc(str);
                System.err.println(str);
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

    static boolean runUnitTest( Class cl, HighScore log)
    {
        
            try {
                Method method = cl.getMethod("test", (Class[]) null);
                try {
                    boolean result = (Boolean) method.invoke(null);
                    
                    if (result)
                    {
                        return true;
                    }
                    logPlayerCrash(cl, new RuntimeException("Player failed unit test."));
                     
                
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            } catch (NoSuchMethodException ex) {
                logPlayerCrash(cl, new RuntimeException("Player has no unit test.  Proceeding."));
                return true; // no unit test
            } catch (SecurityException ex) {
                Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
                logPlayerCrash(cl, new RuntimeException("Player violated security rule.  Proceeding."));
            }
            
        return false;
        
    }
    public static void main(String args[]) {

        // parse the command line
        int i = 0;
        while (i < args.length) {
            String s = args[i++];

            System.err.println("Testing " + s);
            if (runTest(s, new HighScore())) {
                System.err.println("Class passed the test.");
            } else {
                System.err.println("Class failed the test.");
            }

        }

        System.err.println("Usage: java -jar W -classpath .:./players/ was.PlayerTest PACKAGENAME.CLASS");
        System.err.println("Put player .jar files into players/");

    }
}
