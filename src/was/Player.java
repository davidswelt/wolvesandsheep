package was;

import ch.aplu.jgamegrid.Actor;

/**
 * This is a general player class.  
 * 
 * @author reitter
 */
public abstract class Player {
    
    private static int counter=0;
    private int count = counter++;
    
    private double maxAllowedDistance = 2;
    Actor playerProxy = null;
    
    private int isBusyUntilTime = 0; // wolf is eating

    GameBoard gb = null;
    final void setGameBoard (GameBoard gb)  // available only to was class members
    {
        if (this.gb == null)
        {
            this.gb = gb;
        }
        else
        {
            throw new RuntimeException("Player's gameboard is already set.  Player added twice?");
        }
    }
    final void setMaxAllowedDistance (double d)
    {
        maxAllowedDistance = d;
    }
    final void setPlayerProxy (Actor a)
    {
        playerProxy = a;
    }
    
    final void keepBusyFor(int steps)
    {
        isBusyUntilTime = Math.max(isBusyUntilTime, gb.currentTimeStep + steps);
    }
      final  boolean isBusy() {
            return (gb.currentTimeStep < isBusyUntilTime);
        }

    final Move calcMove ()
    {
        
            if (isBusy()) {
                return null; // can't make a move
            }

        Move m = move(); // move is defined by extending class
        
        if (m.length() > maxAllowedDistance)
        {
            System.err.println(this.getClass()+" - illegal move: too long! " + m.length());
            return null;
        }

        gb.noteMove(this, m); // callback
        return m;
        
    }

    /**
     * Get the maximum allowed distance for this player
     * @return distance measured in steps
     */
    final public double getMaxAllowedDistance ()
    {
        return maxAllowedDistance;
    }
            
    
    abstract public String imageFile ();
    
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
