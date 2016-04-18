
package was;

import java.util.Random;

/**
 * A move made by a player described in terms of its relative x and y movement
 * @author dr
 */
public class Move {

    public double delta_x;
    public double delta_y;
    
    static Random rand = new Random();
    
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
     /**
     * Scale length of a move
     * @return scale the length of a move to match length
     * @param length target length of the move
     * @return Move of scaled (not quantized) length in same direction as this move.
     */  
    public Move scaledToLength(double length) {
            double ratio = length() / length;
            return new Move((delta_x / ratio ), (delta_y / ratio ));
    }
    static protected int roundUp (double a)
    { 
        /*
            1.0 --> 1.0
            1.1 --> 2.0
            -1.0 --> -1.0
            -1.1 --> -2.0
        */
        if ((int) a == a)
            return (int) a;
        if (a > -0.0001 && a < 0.0001)
        {
            return 0;
        }
        //return (int) (a+(a>0?0.9999:-0.9999));
        return (int) (a+(a>0?1:-1));
    }
    static protected int roundDown (double a)
    {
        return (int) a;
    }
    
    /**
     * Converts move to integer
     * Attempts to round up/down so that length is as close as possible to maxLen
     * but lower or equal to maxLen
     * @param maxLen upper bound for move length
     * @return quantized Move
     */
    public Move quantized (double maxLen)
    {
        // try rounding up both
        Move m=new Move(roundUp(delta_x), roundUp(delta_y));
        if (m.length()<=maxLen)
        {
            return m;
        }
        // try rounding up either and choose better one
        Move m1=new Move(roundUp(delta_x), roundDown(delta_y));
        Move m2=new Move(roundDown(delta_x), roundUp(delta_y));
        if (m1.length()<=maxLen)
        {
            if (m2.length()<=maxLen)
            {   // both are short enough, choose longer one
                return m1.length()>m2.length() ? m1 : m2;
            }
            else
            {   // only m1 is short enough, so return that.
                return m1;
            }
            
        }
        else if (m2.length()<=maxLen)
        {   // only m2 is short enough, so return that
            return m2;
        }
        // so, we must round both down.
        Move m3 = new Move(roundDown(delta_x), roundDown(delta_y));
        if (m3.length()<=maxLen)
        {   // short enough?
            return m3;
        }
        // that didn't work.  We scale to length, then we try
        // quantizing again (it is guaranteed to work that time).
        return scaledToLength(maxLen).quantized(maxLen);
        
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
