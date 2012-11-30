package was;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test of the player
 *
 * @author dr
 */
public class PlayerTest {

    public static boolean runTest(Class playerClass) {
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
            if (Class.forName("reitter.Wolf").isAssignableFrom(playerClass) && Class.forName("reitter.Wolf") != playerClass) {
                System.err.println("Class inherits from reitter.Wolf.");
                return false;
            } else if (Class.forName("reitter.Sheep").isAssignableFrom(playerClass)&& Class.forName("reitter.Sheep") != playerClass) {
                System.err.println("Class inherits from reitter.Wolf.");
                return false;
            }
            return true;
        } catch (ClassNotFoundException ex) {
            // reitter.* may not be included
            return true;
        }
    }

    public static boolean runTest(String playerClassStr) {

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
            System.err.println("Class coud not be found in supplied jar file: " + playerClassStr);
            System.err.println("Did you give it in the form of packagename.classname?");
            System.err.println("Did you put your class in the right package?");
            System.err.println("Did you make sure the package is included?");
            return false;
        }
        return runTest(playerClass);

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
