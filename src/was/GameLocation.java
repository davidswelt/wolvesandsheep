
package was;

/**
 * The type describing the state of a single cell in the game grid.
 */
final public class GameLocation  {
    /**
     * The horizontal position (column)
     */
    public int x;
    /**
     * The vertical position (row)
     */
    public int y;

    /**
     * Make a new position
     * @param x
     * @param y
     */
    public GameLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString() {
  
        return "L("+x+","+y+")";
    }
    
    @Override
    public boolean equals (Object o) {
        if (this==o)
        {
            return true;
        }
        if (o==null)
        {
            return false;
        }
        if (! (o instanceof GameLocation))
        {
            return false;
        }
        return (x==((GameLocation) o).x && y==((GameLocation) o).y);
        
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.x;
        hash = 79 * hash + this.y;
        return hash;
    }
    
    
}
