
package was;

/**
 * This is a general player class.  
 * 
 * @author reitter
 */
public abstract class Player {
    
    private static int counter=0;
    private int count = counter++;

    
    // must override move.
    
    /**
     * This calculates the next move.
     * "return new Move (1,1)" would be a correct implementation.
     * Players cannot move into an obstacle, or off the grid.
     * If such a move is returned, the player will either not move,
     * or move partially, stopping at the boundary.
     * A Sheep player may not move more than one step at a time.
     * A Wolf player may not move more than the designated number of steps.
     * @return new object of type Move
     */
    abstract public Move move ();
    
    
    // Don't override these
    
    @Override
    final public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Player other = (Player) obj;
        if (this.count != other.count) {
            return false;
        }
        return true;
    }
    @Override
    final public int hashCode() {return count;}

    
}
