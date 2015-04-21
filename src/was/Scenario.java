/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package was;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dr
 */
public class Scenario {

    //protected static List<Integer> scenarioParameterValues;
    static Random rand = new Random();
    int requested = 0;
    GameBoard tmpGb = null;
    static List<Integer> parmValues = new ArrayList(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
    static boolean useSecretScenarioClass = false;
    volatile static int repeatCount = 0; // used to avoid randomization when repeating rounds
    volatile static int gameCount = 0; // counts total number of games played (for info)
    GameBoard gb = null;

    protected Scenario() {
    }

    static Class scenarioClass() {
        Class cl = null;

        try {
            if (useSecretScenarioClass) {
                try {
                    cl = Class.forName("was.SecretScenario"); // not provided to students - held out evaluation scenarios
                } catch (ClassNotFoundException ex) {

                    cl = Class.forName("was.Scenario");

                }
            } else {
                cl = Class.forName("was.Scenario");
            }
        } catch (ClassNotFoundException ex1) {
            throw new RuntimeException(ex1);
        }
        return cl;
    }

    static List<Integer> getParameterValues() {
        // get parameter values for best available Scenario class
        try {
            java.lang.reflect.Field f1;
            f1 = scenarioClass().getDeclaredField("parmValues");
            return (List<Integer>) f1.get(null);
        } catch (Exception ex) {
            Logger.getLogger(Scenario.class.getName()).log(Level.SEVERE, null, ex);
        }

        return parmValues;
    }
    
    static double getMaxWolfDistance () {
        return 2.0;
    }
    static int getWolfEatingTime() {
        return 7;
    }

    static final Scenario makeScenario(int requestedScenario) {
        Scenario sc = null;

        try {
            sc = (Scenario) scenarioClass().newInstance();
            //        sc = new Scenario();
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

        gameCount ++;
        
        sc.requested = requestedScenario;
        // any parametrization should happen here
        if (sc.requested == 0) {
            sc.requested = getParameterValues().get(rand.nextInt(getParameterValues().size()));
        }
        return sc;
    }

    void addToBoard(GameBoard board, Stack<GameLocation> wolfP, Stack<GameLocation> sheepP) {
        int rows = board.getRows();
        int cols = board.getCols();
        tmpGb = board;
        switch (requested) {
            case 1:
                green(1, 1);
                green(1, 2);
                green(2, 2);
                grey(15, 15);
                grey(16, 15);
                break;
            case 2:
                green(-1, 1);
                green(-2, 1);
                green(-3, 1);
                grey(14, -1);
                grey(15, -1);
                grey(16, -1);
                break;
            case 3:
                green(2, 2);
                green(3, 2);
                green(28, 1);
                green(27, 1);
                green(27, 2);
                green(28, 2);
                grey(15, -1);
                grey(16, -1);
                grey(14, -1);
                break;
            case 4:
                green(1, 1);
                green(1, 2);
                green(2, 1);
                green(2, 2);
                grey(0, 4);
                grey(1, 4);
                grey(2, 4);
                grey(3, 4);
                grey(4, 3);
                grey(4, 4);
                grey(5, 2);
                grey(5, 3);
                sheepP.add(loc(5, -1));
                sheepP.add(loc(10, -1));
                sheepP.add(loc(20, -1));
                sheepP.add(loc(25, -1));
                wolfP.add(loc(-1,-1));
                break;
            case 5:
                green(1, 1);
                green(1, 2);
                green(2, 1);
                green(2, 2);
                grey(20, 1);
                grey(20, 2);
                grey(20, 3);
                grey(20, 4);
                grey(20, 5);
                grey(21, 5);
                grey(22, 6);
                grey(23, 7);
                grey(24, 8);
                grey(21, 1);
                grey(21, 2);
                grey(21, 3);
                grey(21, 4);
                grey(21, 6);
                grey(22, 5);
                grey(23, 6);
                grey(24, 7);
                grey(25, 8);
                wolfP.add(loc(-2, 3));
                break;
            case 6:
                wolfP.add(loc(15, 15));
                sheepP.add(loc(10, 15));
                sheepP.add(loc(15, 10));
                sheepP.add(loc(20, 15));
                sheepP.add(loc(15, 20));
                green(0, 13);
                green(0, 14);
                green(0, 15);
                green(-1, 15);
                green(-1, 16);
                green(-1, 17);
                grey(-2, 15);
                grey(2, 15);
                break;
            case 7:
                // input scale is 150
                //                for (int y = 0; y < 150; y++) {
                //                    grey(-1, y);
                //                    grey(0, y);
                //                }
                //                for (int x = 1; x < 150 - 1; x++) {
                //                    grey(x, -1);
                //                    grey(x, 0);
                //                }
                for (int y = 2; y < 12; y += 2) {
                    for (int x = 2; x < 10; x += 2) {
                        sheepP.add(loc(x, y));
                    }
                }
                for (int y = -3; y > -12; y -= 2) {
                    for (int x = 2; x < 10; x += 2) {
                        sheepP.add(loc(x, y));
                    }
                }
                Collections.shuffle(sheepP);
                green(-1, 0);
                green(-1, 1);
                green(-1, rows / 2);
                grey(-1, rows / 2 - 1);
                grey(-1, rows / 2 + 1);
                grey(-2, rows / 2 - 2);
                grey(-2, rows / 2 + 2);
                board.maxTimeStep = Math.max(board.maxTimeStep, 500);
                break;
            case 8:
                // 50x50
                green(30, 0);
                green(31, 0);
                green(32, 0);
                green(30, 1);
                green(31, 1);
                green(32, 1);
                line(15, 19, 50 - 15, 19);
                line(15, 19, 15, 25);
                line(-15, 19, -15, 28);
                line(15, 25, 17, 27);
                line(0, -1, 8, -8);
                line(0, -2, 8, -9);
                line(0, -3, 8, -10);
                line(8, -8, 12, -4);
                line(9, -8, 13, -4);
                line(9, -9, 14, -4);
                line(12, -4, 9, -2);
                line(10, -2, -4, -2);
                line(10, -3, -4, -3);
                line(-1, -5, -10, -5);
                line(-1, -6, -10, -6);
                wolfP.add(loc(5, -3));
                sheepP.add(loc(17, 22));
                sheepP.add(loc(20, 22));
                sheepP.add(loc(23, 22));
                sheepP.add(loc(26, 22));
                board.maxTimeStep = Math.max(board.maxTimeStep, 200);
                break;
            default:
                throw new RuntimeException("Specified scenario ("+requested+") unavailable.");
        }
    }

    int boardSize() {
        switch (requested) {
            case 7:
                return (int) (150);
            case 8:
                return (int) (50);
        }
        return (int) (30);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Scenario other = (Scenario) obj;
        if (this.requested != other.requested) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.requested;
        hash = 97 * hash + this.getClass().hashCode();
        return hash;
    }

    Player green(int x, int y) {
        return setFigure(Pasture.class, x, y);
    }

    Player grey(int x, int y) {
        return setFigure(Obstacle.class, x, y);
    }

    GameLocation loc(double x, double y) {
        int xx = (int) x; // Math.max(0, Math.min(tmpGb.getCols()-1, (int) (x*(scale/inputscale))));
        int yy = (int) y; // Math.max(0, Math.min(tmpGb.getRows()-1, (int) (y*(scale/inputscale))));

        xx = normX(xx);
        yy = normY(yy);
        if (tmpGb.isEmptyCell(xx, yy)) {
            return new GameLocation(xx, yy);
        } else {
            return null;
        }
    }

    int normX(int x) {
        if (x < 0) {
            x = tmpGb.getCols() + x;
        }
        return x;
    }

    int normY(int y) {
        if (y < 0) {
            y = tmpGb.getRows() + y;
        }
        return y;
    }

    Player setFigure(Class p, int x, int y) {
        try {
            GameLocation l = loc(x, y);
            if (l != null) {

                if (tmpGb.isEmptyCell(l.x, l.y)) {
                    return tmpGb.addPlayer((Player) p.newInstance(), l);
                }

            }
        } catch (CoordinatesOutOfBoundsException ex) {
        } catch (IndexOutOfBoundsException ex) {
        } catch (InstantiationException ex) {
            Logger.getLogger(Scenario.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Scenario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    protected void delete(Player[] dots) {
        for (Player p : dots) {
            tmpGb.removePlayer(p);
        }
    }
    int lastLineX = 0;
    int lastLineY = 0;

    protected Player[] lineTo(int x, int y) {
        Player[] dots;
        dots = line(lastLineX, lastLineY, x, y);
        lastLineX = x;
        lastLineY = y;
        return dots;
    }

    protected Player[] line(int x1, int y1, int x2, int y2) {

        List<Player> dots = new ArrayList();
        lastLineX = x2;
        lastLineY = y2;

        x1 = normX(x1);
        y1 = normY(y1);
        x2 = normX(x2);
        y2 = normY(y2);
        if (x2 < x1) {
            int t = x2;
            x2 = x1;
            x1 = t;
            t = y2;
            y2 = y1;
            y1 = t;
        }

        double sy = (double) (y2 - y1) / (x2 - x1);
        double sx = 1;

        if (sy > 1 || sy < -1) {
            sx /= sy;
            sy = 1;
            if (sx < 0) {
                sx = -sx;
                sy = -1;
            }
        }

        double x = x1;
        double y = y1;
        while ((sy > 0 ? y <= y2 : y >= y2) && (sx > 0 ? x <= x2 : x >= x2)) {
            Player p = setFigure(Obstacle.class, (int) x, (int) y);
            if (p != null) {
                dots.add(p);
            }
            if ((int) x < (int) x + sx) // horizontal shift?
            {

                p = setFigure(Obstacle.class, (int) x + 1, (int) y);
                if (p != null) {
                    dots.add(p);
                }
            }
            x += sx;
            y += sy;
        }
        return (Player[]) dots.toArray(new Player[1]);
    }

    void updateBoard(GameBoard board, int it) {
    }

    @Override
    public String toString() {
        return "Sc." + String.format("%02d", requested);
    }

    static public String toString(int sc) {
        return "Sc." + String.format("%02d", sc);
    }
    
}
