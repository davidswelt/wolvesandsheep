
package was;

import java.util.Random;

/**
 * A move made by a player described in terms of its relative x and y movement
 * @author dr
 */
public class Move {

    public double delta_x;
    public double delta_y;
    
    private static Random rand = new Random();
    
    /**
     * Create a move
     * "new Move (1,-2) would go 1 step to the right, and 2 steps up.
     * @param dx steps to the right (or -dx steps to the left)
     * @param dy steps down (or -dy steps up)
     */
    public Move(double dx, double dy) {
        delta_x = dx;
        delta_y = dy;        
    }
    
    /**
     * Calculate length of a move
     * @return calculate the distance covered by this move
     */
    final public double length() {
        return Math.sqrt((delta_x*delta_x) + (delta_y*delta_y));
    }
    
    public Move scaledToLength(double length) {
            double ratio = length() / length;
            return new Move((delta_x / ratio ), (delta_y / ratio ));
    }
    static protected int roundUp (double a)
    {
        if ((int) a == a)
            return (int) a;
        if (a > -0.0001 && a < 0.0001)
        {
            return 0;
        }
        return (int) (a+(a>0?1.0:-1.0));
        
    }
    static protected int roundDown (double a)
    {
        return (int) a;
    }
    
    /**
     * Converts move to integer
     * Attempts to round up/down so that length is as close as possible to maxLen
     * but lower or equal to maxLen
     * @param maxLen
     * @return quantized Move
     */
    public Move quantized (double maxLen)
    {
        Move m=new Move(roundUp(delta_x), roundUp(delta_y));
        if (m.length()<=maxLen)
        {
            return m;
        }
        Move m1=new Move(roundUp(delta_x), roundDown(delta_y));
        Move m2=new Move(roundDown(delta_x), roundUp(delta_y));
        if (m1.length()<=maxLen)
        {
            if (m2.length()<=maxLen)
            {
                return m1.length()>m2.length() ? m1 : m2;
            }
            else
            {
                return m1;
            }
            
        }
        else if (m2.length()<=maxLen)
        {
            return m2;
        }

        return new Move(roundDown(delta_x), roundDown(delta_y));
    }
    /**
     * Converts move to integer
     * Rounds down (always makes move shorter)
     * @return quantized Move
     */
    public Move quantized ()
    {
        
        return new Move ((int) (delta_x), (int) (delta_y));
    }
    
    /**
     * Quantizes move stochastically
     * Length of move may be greater or smaller
     * @return quantized move
     */
    public Move stochasticallyRounded()
    {
        double dx=delta_x, dy=delta_y;
        if (delta_x<0)
            dx -= rand.nextFloat();
        else
            dx += rand.nextFloat();
        if (delta_y<0)
            dy -= rand.nextFloat();
        else
            dy += rand.nextFloat();
        
        return new Move ((int) (dx), (int) (dy));
    }
    @Override
    public String toString() {
  
        return "M("+String.format("%.3g",delta_x)+","+String.format("%.3g",delta_y)+"="+String.format("%.3g",length())+")";
        
    }
}
