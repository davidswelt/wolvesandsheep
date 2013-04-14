package was;

import ch.aplu.jgamegrid.Actor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a general player class.
 *
 * @author reitter
 */
public abstract class Player {
    
    private static final int MOVE = 0;
    private static final int INITIALIZE = 1;
    private static final int IS_BEING_EATEN = 2;
    private static final int IS_EATING = 3;

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
    static boolean catchExceptions = false;
    static boolean logToFile = false;
    PrintStream logstream = null;
    double totalRunTime = 0.0;
    long totalRuns = 0;
    boolean disqualified = false;

    public Player() {
        if (logToFile) {
            String filename = "log/" + getClass().getName() + ".log";

            try {
                logstream = new PrintStream(new FileOutputStream(filename, true));
            } catch (FileNotFoundException ex) {
                System.err.println("can't output to " + filename);

            }
        }

    }
    
    /**
     * Override this function to run your own unit test.
     *
     * @return true if the the test was passed, false otherwise.
     */
    public static boolean test () {
        return true;
    }

    /**
     * Override this function to keep track of your player's version number
     *
     * @return
     */
    public double versionNumber() {
        return 1.0;
    }

    /**
     * Called when the game is over. The player may do any cleanup here.
     */
    void finished() {
        if (logstream != null) {
            logstream.close();
            logstream = null;
        }
    }

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
            // this may happen when gameboard is scaled
            //  throw new RuntimeException("Player's gameboard is already set.  Player added twice?");
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

    final public String getID() {
        return getClass().getName() + "." + versionNumber() + count;
    }

    /**
     * Get the Gameboard for this player.
     *
     * @return a gameboard object
     */
    final public GameBoard getGameBoard() {
        if (gb == null)
        {
            throw new RuntimeException("getGameBoard may not be called from a player constructor.  Use the Player class's 'initialize' method.");
        }
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

    /**
     * Return the average runtime of this player (all functions)
     *
     * @return avg. runtime in milliseconds or 0.0 if no measurements
     */
    final public double meanRunTime() {
        if (totalRuns > 0) {
            return totalRunTime / totalRuns;
        } else {
            return 0.0;
        }
    }
    // we discard a player's move if it takes longer than 2*180 ms.
    // currently, players are not disqualified.
    int individualRunFactor = 3;
    long TIMEOUT = 70;

    final Object callPlayerFunction(int fn) {
        Object m = null; // return var

        if (disqualified) {
            return null;
        }

        PrintStream prevErrStream = System.err;
        PrintStream prevOutStream = System.out;

        final Player thePlayer = this;
        final int func = fn;

        FutureTask ft = new FutureTask<Object[]>(new Callable<Object[]>() {
            private static final int MOVE = 0;
            @Override
            public Object[] call() {
                ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                long startTime = threadMXBean.getCurrentThreadCpuTime();

                Object m = null;
                switch (func) {
                    case MOVE:
                        m = move(); // move is defined by extending class
                        break;
                    case INITIALIZE:
                        initialize(); // move is defined by extending class
                        break;
                    case IS_BEING_EATEN:
                        m = null;
                        if (thePlayer instanceof SheepPlayer) {
                            ((SheepPlayer) thePlayer).isBeingEaten();
                        }
                        break;
                    case IS_EATING:
                        m = null;
                        if (thePlayer instanceof WolfPlayer) {
                            ((WolfPlayer) thePlayer).isEating();
                        }
                        break;
                }


                Object[] ret2 = new Object[4];
                ret2[0] = m;
                ret2[1] = (Long) ((threadMXBean.getCurrentThreadCpuTime() - startTime) / 1000); // nanosec to 1000*millisec
                return ret2;
            }
        });

        try {

            if (logstream != null && logToFile) {
                System.setOut(logstream);
                System.setErr(logstream);
            }



            ft.run();

            /* I think the timeout in FutureTask refers to clock time, not CPU time.
             * Thus, we need to allow for a much longer timeout.  This will only catch
             * cases where a player hangs.  Because we disqualify it in that situation,
             * we can afford to wait a whole second.
             */
            Object[] result = (Object[]) ft.get(1, TimeUnit.SECONDS); // timeout

            /* dur will contain the actual, measured CPU time for the thread. 
             This is going to be much more accurate. */
            long dur = (Long) result[1];
            m = (Object) result[0];
            //timing.add(p.getClass().getName(), (double) dur / 1000.0);
            // we allow for 5 times the nominal average run time in certain cases

            totalRunTime += dur / 1000.0;
            if (func == MOVE) // only count function 0 runs (others add up)
            {
                totalRuns++;
            }

            if (dur > individualRunFactor * TIMEOUT * 1000) {  // convert to 1000*millisec
                System.err.println("!! Runtime: " + dur / 1000 + "ms."); // convert to millisec.
                disqualified = true;
                throw new TimeoutException();
            }
            if (dur > TIMEOUT * 1000) {  // convert to 1000*millisec
                System.err.println("Runtime: " + dur / 1000 + "ms."); // convert to millisec.
                throw new TimeoutException();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
            Tournament.logPlayerCrash(this.getClass(), ex);
            //throw new IllegalGameMoveException("makeMove was interrupted.", p, null);
        } catch (ExecutionException ex) {
            
            if (catchExceptions) {
                
                LOG("Player " + this.getClass().getName() + " runtime exception " + ex.getCause());
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex.getCause());
                Tournament.logPlayerCrash(this.getClass(), (RuntimeException) ex.getCause());
                m = null;
            } else {
                throw (RuntimeException) ex.getCause();
            }
        } catch (TimeoutException ex) {
            System.err.println("Player " + thePlayer.getClass().getName() + " timed out " + TIMEOUT + "ms max.");
            Tournament.logPlayerCrash(this.getClass(), ex);
            //            int[][] availableMoves = (int[][]) board.getFreeCells();
            //            int chosen = random.nextInt(availableMoves.length);
            //            move = availableMoves[chosen];
//                        disqualifiedPlayers.add(p.getClass());

        } catch (RuntimeException ex) {
            System.err.println("runtime ex.");
            if (catchExceptions) {
                System.err.println("catching");
                LOG("Player " + this.getClass().getName() + " runtime exception " + ex);
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                Tournament.logPlayerCrash(this.getClass(), ex);

                m = null;
            } else {
                System.err.println("NOT catching");
                throw ex;
            }
        } finally {
            if (logstream != null && logToFile) {
                System.setOut(prevOutStream);
                System.setErr(prevErrStream);
            }

        }
        return m;

    }

    final void callInitialize() {
        callPlayerFunction(INITIALIZE);
    }
    
    final void callIsBeingEaten() {
        callPlayerFunction(IS_BEING_EATEN);
    }

    final void callIsEating() {
        callPlayerFunction(IS_EATING);
    }

    final Move calcMove() {

        if (isBusy()) {

            return null; // can't make a move
        }

        Move m;

        // redirect output if needed

        m = (Move) callPlayerFunction(MOVE); // call "move"

        if (m == null) {

            LOG("move() returned null.");
            m = new Move(0, 0);
        } else {
            if (m.length() > 0.1) {
//            System.err.println("Len: "+m.length()+" maxallowed: "+ maxAllowedDistance);
                if (m.length() > maxAllowedDistance + 0.000005) {
                    // String str = "illegal move: too long! " + m + ": " + m.length() + " > " + maxAllowedDistance;
                    //System.err.println(this.getClass() + str);

                    Tournament.logPlayerCrash(this.getClass(), new RuntimeException("illegal move: too long"));


                    // trim move
                    m = m.scaledToLength(maxAllowedDistance);
                }
                m = m.quantized(maxAllowedDistance);
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

    // helper
    // hack
//    public long getBuildTime() {
//        try {
//            Class cl = getClass();
//            String rn = cl.getName().replace('.', '/') + ".class";
//            File path = new File(cl.getResource(rn).getPath());
//
//            return path.lastModified();
//        } catch (Exception e) {
//            return -1;
//        }
//    }
}
