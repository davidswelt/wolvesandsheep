/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package was;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import static was.Tournament.logPlayerCrash;

/**
 *
 * @author dr
 */
public class PlayerFactory {

        
    
    

    static void logPlayerCrash(Class cl, Exception ex) {
        Tournament.logPlayerCrash(cl, ex);
    }

    //new String[]{"", ".Wolf", ".Sheep"})
    public static Class getClassForName(String name) {
        return getClassForName(name, new String[]{"", ".Wolf", ".Sheep"});
    }
//    public static Class getClassForName (String name, String[] postfix, Class def)

    public static Class getClassForName(String name, String[] postfix) {
        Class c = null;
        String n = "";



        // we'll try different variants

        String[] na = new String[4];
        na[0] = name;
        na[1] = name.toLowerCase();
        na[2] = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        na[3] = name.toUpperCase();

        for (String s : na) {
            try {
                for (String p : postfix) {
                    try {
                        c = Class.forName(s + p);
                        n = s + p;
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
            n = name + na[1];
            System.err.print("No such class: " + na[1]);
            System.err.println("");

        }
        return c;
    }

    static Player makePlayerInstance(Class c) {
        try {
            Constructor co = c.getConstructor();
            try {
                return (Player) co.newInstance();
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
                    logPlayerCrash(c, ex);
                } else {
                    throw ex;
                }
            } catch (InvocationTargetException ex) {
                if (!Player.logToFile) {
                    Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
                }
                logPlayerCrash(c, ex);
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

    static ArrayList<String> string2missingClasslist(String names, String postfix) 
        {
            return (ArrayList<String>) string2classlistI(names, postfix, true);
        }  
        

    static ArrayList<Class> string2classlist(String names, String postfix) 
        {
            return (ArrayList<Class>) string2classlistI(names, postfix, false);
        }
    
    private static ArrayList string2classlistI(String listofPlayerPClassNames, String postfix, boolean missing) {
        StringTokenizer st = new StringTokenizer(listofPlayerPClassNames, ":");
        if (st.hasMoreElements()) {
            st.nextToken();
            if (st.hasMoreElements()) {
                listofPlayerPClassNames = st.nextToken();
            }
        }

        String[] pfa = new String[]{postfix};

        ArrayList players = new ArrayList();
        st = new StringTokenizer(listofPlayerPClassNames, ", ");

        while (st.hasMoreElements()) {
            String name = st.nextToken();
            Class cs = PlayerFactory.getClassForName(name, pfa);
            if (missing == (cs == null)) {
                players.add(missing ? name : cs);
            }
        }

        return players;
    }
}
