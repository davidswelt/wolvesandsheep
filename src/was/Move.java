
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
    public Move quantized ()
    {
        
        return new Move ((int) (delta_x), (int) (delta_y));
    }
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
