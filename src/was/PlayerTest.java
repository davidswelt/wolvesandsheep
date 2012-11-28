package was;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test of the player
 *
 * @author dr
 */
public class PlayerTest {
    
    public static boolean runTest(Class playerClass)  {
        try {
            boolean isWolf;
            if (Class.forName("was.SheepPlayer").isAssignableFrom(playerClass)) {
                isWolf = false;
            } else if (Class.forName("was.WolfPlayer").isAssignableFrom(playerClass)) {
                isWolf = true;
            } else {
                System.err.println("Class is neither derived from was.WolfPlayer nor from was.SheepPlayer.");
                return false;
            }
            if (Class.forName("reitter.WolfPlayer").isAssignableFrom(playerClass) && Class.forName("reitter.WolfPlayer") != playerClass) {
                System.err.println("Class inherits from reitter.WolfPlayer.");
                return false;
            } else if (Class.forName("reitter.SheepPlayer").isAssignableFrom(playerClass)&& Class.forName("reitter.SheepPlayer") != playerClass) {
                System.err.println("Class inherits from reitter.WolfPlayer.");
                return false;
            }
            return true;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PlayerTest.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
 
    public static boolean runTest(String playerClassStr) {

        // wolf or sheep?
        boolean isWolf;
        try {
            Class playerClass = Class.forName(playerClassStr);

            return runTest(playerClass);
            
        } catch (ClassNotFoundException ex) {
            System.err.println("Class coud not be found in supplied jar file: " + ex);
            System.err.println("Did you give it in the form of packagename.classname?");
            System.err.println("Did you put your class in the right package?");
            System.err.println("Did you make sure the package is included?");
            return false;
        }
    }

    public static void main(String args[]) {

        // parse the command line
        int i = 0;
        while (i < args.length) {
            String s = args[i++];

            System.err.println("Testing " + s);
            if (runTest(s)) {
                System.err.println("Class passed the test.");
            } else {
                System.err.println("Class failed the test.");
            }

        }


        System.err.println("Usage: java -jar W -classpath .:./players/ was.PlayerTest PACKAGENAME.CLASS");
        System.err.println("Put player .jar files into players/");

    }

   
}
