
package was;

/**
 *
 * @author reitter
 */
public abstract class Player {
    
    private static int counter=0;
    private int count = counter++;

    
    // must override move.
    
    /**
     * This calculates the next move.
     * "return new Move (1,1)" would be a correct move.
     * Players cannot move into an obstacle, or off the grid.
     * If such a move is returned, the player will either not move,
     * or move partially, stopping at the boundary.
     * A Sheep player may not move more than one step at a time.
     * A Wolf player may not move more than the designated number of steps.
     * @return new object of type Move
     */
    abstract public Move move ();

    /**
     * Is this a Wolf player?
     * true if it's a wolf, false otherwise.
     */
    public static boolean isWolf = false;

    
    // may override isBeingEaten and isEating.
        
    /**
     * isBeingEaten() is called just before this sheep is eaten
     * the method is called only if this player is a sheep.
     */
    public void isBeingEaten () {};
    
    /**
     * isEating() is called just before this wolf is eating a sheep
     * the method is called only if this player is a wolf.
     */
    public void isEating () {};
    
    
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
