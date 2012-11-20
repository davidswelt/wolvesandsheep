
package was;

/**
 * A move made by a player described in terms of its relative x and y movement
 * @author dr
 */
public class Move {

    public int delta_x;
    public int delta_y;
    
    /**
     * Create a move
     * "new Move (1,-2) would go 1 step to the right, and 2 steps up.
     * @param dx steps to the right (or -dx steps to the left)
     * @param dy steps down (or -dy steps up)
     */
    public Move(int dx, int dy) {
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
}
