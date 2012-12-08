/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package was;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    static List<Integer> scenarioParameterValues;
    Random rand = new Random();
    int requested = 0;

    GameBoard tmpGb = null;

    {
      scenarioParameterValues =  new ArrayList(Arrays.asList(1, 2, 3, 4, 5, 6, 7));
    }
    public Scenario() {
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
                board.MAXTIMESTEP = Math.max(board.MAXTIMESTEP, 500);
                break;
            
        }
    }

    int boardSize() {
        switch (requested) {
            case 7:
                return (int) (150);
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
        final ExamScenario other = (ExamScenario) obj;
        if (this.requested != other.requested) {
            return false;
        }
        return true;
    }

    void green(int x, int y) {
        setFigure(Pasture.class, x, y);
    }

    void grey(int x, int y) {
        setFigure(Obstacle.class, x, y);
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

    void setFigure(Class p, int x, int y) {
        GameLocation l = loc(x, y);
        if (l != null) {
            try {
                if (tmpGb.isEmptyCell(l.x, l.y)) {
                    tmpGb.addPlayer((Player) p.newInstance(), l);
                  
                }
            } catch (InstantiationException ex) {
                Logger.getLogger(ExamScenario.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(ExamScenario.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String toString() {
        return "Sc." + requested;
    }
    
}
