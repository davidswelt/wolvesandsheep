package was;

import ch.aplu.jgamegrid.Actor;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a general player class.
 *
 * @author reitter
 */
public abstract class Player {

    public static enum GamePiece {

        EMPTY, SHEEP, WOLF, OBSTACLE, PASTURE
    };
    private static int counter = 0;
    private int count = counter++;
    private double maxAllowedDistance = -1;
    Actor playerProxy = null;
    private int isBusyUntilTime = 0; // wolf is eating
    GameBoard gb = null;
    private int x = 0, y = 0;
    String team;

    /**
     * The track color for this player Override this method to return your own
     * color!
     *
     * @return a color to visualize it. null if no track to be shown.
     */
    public java.awt.Color trackColor() {
        return new java.awt.Color(getClass().getName().hashCode());
    }

    /**
     * Get a short name for this player
     *
     * @return a String
     */
    public String shortName() {
        return getClass().getPackage().getName();
    }

    abstract GamePiece getPiece();

    final void setGameBoard(GameBoard gb) // available only to was class members
    {
        if (this.gb == null) {
            this.gb = gb;
        } else {
            throw new RuntimeException("Player's gameboard is already set.  Player added twice?");
        }
    }

    final void markDeleted() {
        gb = null;
    }

    final boolean isGone() {
        return (gb == null);
    }

    final void setTeam(String s) {
        team = s;
    }

    final String getTeam() {
        return team;
    }

    /**
     * Initialize the player. Override this method to do any initialization of
     * the player. This will be called once before each game, and after the game
     * board has been set up.
     */
    public void initialize() {
    }

    /**
     * Get the Gameboard for this player.
     *
     * @return a gameboard object
     */
    final public GameBoard getGameBoard() {
        return gb;
    }

    final void setMaxAllowedDistance(double d) {
        maxAllowedDistance = d;
    }

    final void setPlayerProxy(Actor a) {
        playerProxy = a;
    }

    // can't be called by inheriting classes
    final void setLoc(int x, int y) {

        gb.checkPlayerAt(this, x, y);


        this.x = x;
        this.y = y;
        LOG("player " + this + "new loc: " + new GameLocation(x, y));
    }

    /**
     * Get this player's location
     *
     * @return a GameLocation object
     */
    public final GameLocation getLocation() {
        if (gb == null) {
            throw new RuntimeException("can't call getLocation before board has been initialized.");
        }
        return new GameLocation(x, y); // copies location
    }

    final void keepBusyFor(int steps) {
        isBusyUntilTime = Math.max(isBusyUntilTime, gb.currentTimeStep + steps);
    }

    final boolean isBusy() {
        return (gb == null || gb.currentTimeStep < isBusyUntilTime);
    }

    // called by PlayerProxy
    final Move calcMove() {

        if (isBusy()) {

            return null; // can't make a move
        }

        Move m;
        try {
            m = move(); // move is defined by extending class
        } catch (RuntimeException ex) {
            LOG("Player " + this.getClass().getName() + " runtime exception " + ex);
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            m = null;
        }
        if (m == null) {

            LOG("move() returned null.");
            m = new Move(0, 0);
        } else {
            if (m.length() > 0.1) {
//            System.err.println("Len: "+m.length()+" maxallowed: "+ maxAllowedDistance);
                if (m.length() > maxAllowedDistance + 0.000005) {
                    System.err.println(this.getClass() + " - illegal move: too long! " + m.length() + " > " + maxAllowedDistance);
                    // trim move
                    m = m.scaledToLength(maxAllowedDistance);
                }
                m = m.quantized();
            }
            // keep move inside boundaries

            int tx = x + (int) m.delta_x;
            int ty = y + (int) m.delta_y;
            tx = Math.max(0, tx);
            ty = Math.max(0, ty);
            tx = Math.min(gb.getCols() - 1, tx);
            ty = Math.min(gb.getRows() - 1, ty);

            m = new Move(tx - x, ty - y);
        }

        if (gb.noteMove(this, m)) {

            return m;
        } else {
            LOG("noteMove returned null.");
            return new Move(0, 0); // move was impossible (e.g., obstacle)
        }

    }

    /**
     * Get the maximum allowed distance for this player Not available during
     * initialization of the object.
     *
     * @return distance measured in steps. returns -1 if not available yet.
     */
    final public double getMaxAllowedDistance() {
        return maxAllowedDistance;
    }

    /**
     * Returns the name of the image file representing this player Implement
     * this function to return a custom sprite.
     *
     * @return a string with the path and file name of the image file
     */
    abstract public String imageFile();

    // must override move.
    /**
     * This calculates the next move. "return new Move (1,1)" would be a correct
     * implementation. Players cannot move into an obstacle, or off the grid. If
     * such a move is returned, the player will either not move, or move
     * partially, stopping at the boundary. A Sheep player may not move more
     * than one step at a time. A Wolf player may not move more than the
     * designated number of steps.
     *
     * @return new object of type Move
     */
    abstract public Move move();

    @Override
    public String toString() {
        String s = "";
        switch (getPiece()) {
            case EMPTY:
                s = " ";
                break;
            case SHEEP:
                s = "s";
                break;
            case WOLF:
                s = "W";
                break;
            case OBSTACLE:
                s = "#";
                break;
            case PASTURE:
                s = ".";
                break;
        }
        if (isBusy()) {
            s = s + "!";
        }
        if (gb == null) {
            s = "D" + s;
        }
        return s;
    }

    static void LOG(String s) {
    }

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
    final public int hashCode() {
        return count;
    }
}
