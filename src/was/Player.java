package was;

import ch.aplu.jgamegrid.Actor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a general player class.
 *
 * The class is abstract and defines the general interface for players.
 * {@link SheepPlayer} and {@link WolfPlayer}, among others, inherit from this
 * class.
 *
 * @author reitter
 */
public abstract class Player {

    // Timeout rules
    // we discard a player's move if it takes longer than 2*180 ms., and disqualify the player for the rest of the game.
    final private static int individualRunFactor = 60; // 300; // overage for individual runs
    // we disqualify a player if on overage a function call takes more than this much time
    final private static long TIMEOUT = 100; // 500; // 4 milliseconds
    // permanent disqualification in a scenario occurs if a player is disqualified due to overtime in 80 games in that scenario.
    final private static int permanentDisqualificationThreshold = 80; // 80;
    private static final int MOVE = 0;
    private static final int INITIALIZE = 1;
    private static final int IS_BEING_EATEN = 2;
    private static final int IS_KEEPING_BUSY = 3;
    private static final int WILL_EAT = 4;
    private static final int IS_ATTACKED = 5;
    boolean willNotMove = false;
    static boolean debuggable = true; // is set to false by class tournament code
    // not accessible from outside of was package.

    public static enum GamePiece {

        EMPTY, SHEEP, WOLF, OBSTACLE, PASTURE
    };
    private static int counter = 0;
    private int count = counter++;
    private double maxAllowedDistance = -1;
    private double allowedDistanceDivider = 1; // none for now
    PlayerProxy playerProxy = null;
    private int isBusyUntilTime = 0; // wolf is eating
    GameBoard gb = null;
    private int x = 0, y = 0;
    String team;
    static boolean catchExceptions = false;
    static boolean logToFile = false;
    PrintStream logstream = null;
    private double totalRunTime = 0.0;
    private long totalRuns = 0;
    private boolean disqualified = false; // this player object is disqualified (i.e., player is gone for this game)
    // disqualified counts per class:
    private static Map<String, Integer> disqualifiedCount = new HashMap();
    private Thread myThread = null;
    // move requests by the scenario
    // ignored by active players
    // but useful for scenario-controlled players
    Move requestedNextMove = null;

    /**
     * Constructor for Player objects.
     *
     * Note: do not expect to have the game board available at this time.
     * Perform player initialization instead in {@link #initialize()}.
     *
     */
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
    public static boolean test() {
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
     * Identify a tallied-up player. This method is overloaded by SheepPlayer
     * and WolfPlayer.
     *
     * @return true if this player is included in the High Score.
     */
    public boolean isIncludedInHighScore() {
        return false;
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

    /**
     * Get the GamePiece assigned to this player
     *
     * @return an object of type GamePiece
     */
    abstract GamePiece getPiece();

    private boolean isDisqualified() {
        int scenarioNum = gb.scenario.requested;
        Integer dc = (Integer) disqualifiedCount.get(this.getClass().getName() + ".Sc" + new Integer(scenarioNum));
        if (dc == null) {
            dc = 0;
        }
        // either disqualified permanently for the scenario, or just in this round
        return (dc > permanentDisqualificationThreshold || disqualified);
    }

    private void markDisqualified() {
        int scenarioNum = gb.scenario.requested;
        String playerID = this.getClass().getName() + ".Sc" + new Integer(scenarioNum);
        Integer dc = (Integer) disqualifiedCount.get(playerID);
        if (dc == null) {
            dc = 0;
        }
        dc++;
        //the class is disqualified for this scenario
        disqualifiedCount.put(playerID, dc);
        disqualified = true; // disqualified in this round (this player object)

        if (dc > permanentDisqualificationThreshold) {
            Tournament.logPlayerCrash(this.getClass(), new RuntimeException("Player disqualified for scenario."));
        } else {
            Tournament.logPlayerCrash(this.getClass(), new RuntimeException("Player disqualified."));
        }
    }

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

    final boolean isActive() {
        return (gb != null);
    }

    /**
     * Initialize the player.
     *
     * Override this method to do any initialization of the player. This will be
     * called once before each game, and after the game board has been set up.
     */
    public void initialize() {
    }

    /**
     * Get a human-readable identification string for this player object.
     *
     * This may be used to display the player. The string contains the version
     * number (see {@link #versionNumber()}).
     *
     * @return a String
     */
    final public String getID() {
        return getClass().getName() + "." + versionNumber() + count;
    }

    /**
     * Get the Gameboard used by this player.
     *
     * @return a reference to the game board shared by all players in this
     * round.
     */
    final public GameBoard getGameBoard() {
        if (gb == null) {
            throw new RuntimeException("getGameBoard may not be called from a player constructor.  Use the Player class's 'initialize' method.");
        }
        return gb;
    }

    final void setMaxAllowedDistance(double d) {
        maxAllowedDistance = d;
    }

    final void shortenMaxAllowedDistance(double factor) {
        maxAllowedDistance *= factor;
        allowedDistanceDivider /= factor;
    }

    final void setPlayerProxy(PlayerProxy a) {
        playerProxy = a;
    }

    /**
     * Add a list of GameLocations to be visualized later.
     *
     * A polygon will be drawn between the points given after the player's move
     * is over. Note: List is not copied and may be changed by caller later. If
     * the graphical interface is not in use, nothing happens. Use
     * {@link removeVisualizations()} to remove all paths.
     *
     * @param locList the list of points
     */
    public void visualizeTrack(List<GameLocation> locList) {
        if (playerProxy != null) {
            playerProxy.visualizeTrack(locList);
        }
    }

    /**
     * Add a list of GameLocations to be drawn as a path now.
     *
     * A polygon will be drawn immediately that connects all locations given.
     * The polygon will disappear before the the next round. This function is
     * useful for debugging.
     *
     * @param locList the list of points
     * @param color the color (e.g, java.awt.Color.RED)
     */
    public void visualizeTrackNow(List<GameLocation> locList, java.awt.Color color) {
        if (playerProxy != null) {
            playerProxy.showTrack(locList, color, true);
        }
    }

    /**
     * Remove all visualized paths. Polygons drawn will be removed after the
     * player's move. Call this function before adding visualizations to replace
     * them.
     */
    public void removeVisualizations() {
        if (playerProxy != null) {
            playerProxy.removeVisualizations();
        }
    }

    // can't be called by inheriting classes
    final void setLoc(int x, int y) {

        gb.checkPlayerAt(this, x, y);

        this.x = x;
        this.y = y;
        LOG("player " + this + "new loc: " + new GameLocation(x, y));
    }

    /**
     * Get this player's location.
     *
     * @return a GameLocation object
     */
    public final GameLocation getLocation() {
        if (!isActive()) {
            throw new RuntimeException("can't call getLocation for inactive players (before board has been initialized)");
        }
        return new GameLocation(x, y); // copies location
    }

    final void keepBusyFor(int steps) {
        isBusyUntilTime = Math.max(isBusyUntilTime, gb.currentTimeStep + steps);
    }

    final boolean isBusy() {
        return (gb == null || willNotMove || gb.currentTimeStep < isBusyUntilTime);
    }
    // called by PlayerProxy

    /**
     * Return the average runtime of this player (all functions)
     *
     * @return Avg. runtime in milliseconds or 0.0 if no measurements
     */
    final public double meanRunTime() {
        if (totalRuns > 0) {
            return totalRunTime / totalRuns;
        } else {
            return 0.0;
        }
    }

    final Object callPlayerFunction(int fn) {
        return callPlayerFunction(fn, null);
    }

    // arg should be immutable (multithreading).
    final Object callPlayerFunction(int fn, Object arg) {

        if (fn == MOVE) {
            Tournament.logPlayerMoveAttempt(this.getClass());
        }
        if (isDisqualified()) {

            if (fn == MOVE) {
                Tournament.logPlayerCrash(this.getClass(), new RuntimeException("not playing (disqualified)"));
            }
            return null;
        }


        /*
         * Dear Student,
         * Cool that you're looking at this to understand my code.
         * This uses syntax and Java functions that we haven't discussed in class.
         * What is happening here is that we execute the player move in a separate
         * 'thread', that is, in parallel.  The reason why we are doing it this way
         * is that the player code that does a "move" or an "initialize" may hang
         * or take too long.  By running it in a separate thread, we can abandon
         * it and keep control of the execution.  Otherwise, a single "rogue" player 
         * could halt the tournament.  D.R.
         * 
         */
        try {
            if (debuggable) {
                ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                long startTime = threadMXBean.getCurrentThreadCpuTime();

                Object m = callPlayerFunction_direct(fn, arg);
                long dur = (Long) ((threadMXBean.getCurrentThreadCpuTime() - startTime) / 1000); // nanosec to 1000*millisec
                if (dur > TIMEOUT * 1000) // convert to 1000*millisec
                {
                    System.err.println("Warning: player takes too long.");
                }
                return m;
            } else {

                return callPlayerFunction_timed(fn, arg);
            }
        } catch (RuntimeException ex) {
            if (catchExceptions) {
                LOG("Player " + this.getClass().getName() + " runtime exception " + ex);
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                Tournament.logPlayerCrash(this.getClass(), ex);
            } else {
                throw ex;
            }
        }
        return null;
    }

    final Object callPlayerFunction_direct(int func, Object arg) {
        // this function may be executed from the secondary thread.
        // (no direct try/catch in here to prevent re-entrance)
        final Player thePlayer = this;

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
            case IS_KEEPING_BUSY:
                m = null;
                if (thePlayer instanceof WolfPlayer) {
                    ((WolfPlayer) thePlayer).isKeepingBusy();
                }
                break;
            case WILL_EAT:
                m = null;
                if (thePlayer instanceof WolfPlayer) {
                    ((WolfPlayer) thePlayer).isEating(); // deprecated
                    ((WolfPlayer) thePlayer).willEatSheep((String) arg);
                }
                break;
            case IS_ATTACKED:
                m = null;
                if (thePlayer instanceof WolfPlayer) {
                    ((WolfPlayer) thePlayer).isAttacked();
                }
                break;
        }
        return m;

    }

    final Object callPlayerFunction_timed(int fn, final Object arg) {
        Object m = null; // return var

        PrintStream prevErrStream = System.err;
        PrintStream prevOutStream = System.out;

        final Player thePlayer = this;
        final int func = fn;

        FutureTask ft = new FutureTask<Object[]>(new Callable<Object[]>() {
            /* What follows is a code block that is given as an argument
             * to the "Callable" constructor.  This is essentially like an
             * "anonymous function" or a "lambda expression" that you may know
             * from CS theory, or from functional programming in a language such 
             * as Lisp or Python.
             */
            private static final int MOVE = 0;

            @Override
            public Object[] call() {
                ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                long startTime = threadMXBean.getCurrentThreadCpuTime();

                Object m = null;
                m = callPlayerFunction_direct(func, arg);

                Object[] ret2 = new Object[4];
                ret2[0] = m;
                // A nanosecond (ns) is one billionth of a second
                ret2[1] = (Long) ((threadMXBean.getCurrentThreadCpuTime() - startTime) / 1000); // nanosec to 1000*millisec
                return ret2;
            }
        });

        try {

            if (logstream != null && logToFile) {
                System.setOut(logstream);
                System.setErr(logstream);
            }

            // must execute in separate thread for it to be stoppable
            myThread = new Thread(ft);
            myThread.start();

            // running it directly would just run it in the current thread
            // ft.run();

            /* I think the timeout in FutureTask refers to clock time, not CPU time.
             * Thus, we need to allow for a much longer timeout.  This will only catch
             * cases where a player hangs.  Because we disqualify it in that situation,
             * we can afford to wait 300 milliseconds.
             */
            Object[] result = (Object[]) ft.get(300, TimeUnit.MILLISECONDS); // timeout

            // finish off the thread so we don't have old threads hanging around
            myThread.join();
            myThread = null;

            // we only need to do this if an exception occurs
            //myThread.stop();
            /* dur will contain the actual, measured CPU time for the thread. 
             This is going to be much more accurate. */
            long dur = (Long) result[1];
            m = (Object) result[0];
            //timing.add(p.getClass().getName(), (double) dur / 1000.0);
            // we allow for 5 times the nominal average run time in certain cases
            // dur is sec/1000000
            totalRunTime += dur / 1000.0; // millisec
            if (func == MOVE) // only count function 0 runs (others add up)
            {
                totalRuns++;
            }

            if (dur > individualRunFactor * TIMEOUT * 1000 || (totalRuns > 10 && meanRunTime() > TIMEOUT)) {  // convert to 1000*millisec

                // totalRunTime will be reset to 0 when a new player object is created.
                throw new TimeoutException();
            }

        } catch (CancellationException ex) {
        } catch (InterruptedException ex) {
            Logger.getLogger(Tournament.class.getName()).log(Level.SEVERE, null, ex);
            Tournament.logPlayerCrash(this.getClass(), ex);
            //throw new IllegalGameMoveException("makeMove was interrupted.", p, null);
        } catch (ExecutionException ex) {

            if (catchExceptions) {

                LOG("Player " + this.getClass().getName() + " runtime exception " + ex.getCause());
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex.getCause());
                Tournament.logPlayerCrash(this.getClass(), ex.getCause());
                m = null;
            } else {
                throw new RuntimeException("Exception in Player " + this.getClass(), ex.getCause());
            }
        } catch (TimeoutException ex) {
            System.err.println("player TimeoutException");
            String reason = "";
            switch (fn) {
                case MOVE:
                    reason = "move";
                    break;
                case INITIALIZE:
                    reason = "initialize";
                    break;
                case IS_BEING_EATEN:
                    reason = "is_being_eaten";
                    break;
                case IS_KEEPING_BUSY:
                    reason = "is_keeping_busy";
                    break;
                case WILL_EAT:
                    reason = "will_eat";
                    break;
            }

            System.err.println("Player " + thePlayer.getClass().getName() + " timed out " + TIMEOUT + "ms max." + " in function " + reason);
            Tournament.logPlayerCrash(this.getClass(), ex);
            //            int[][] availableMoves = (int[][]) board.getFreeCells();
            //            int chosen = random.nextInt(availableMoves.length);
            //            move = availableMoves[chosen];
//                        disqualifiedPlayers.add(p.getClass());

            markDisqualified();

            if (myThread != null) {
//                System.err.println("killing thread");
                if (myThread.isAlive()) {
//                    System.err.println("is alive, sending interrupt");
                    myThread.interrupt();
                    try {
//                        System.err.println("sleeping");
                        Thread.sleep(400); // wait to give it a chance to stop
                    } catch (InterruptedException ex1) {
                    }
                    if (myThread.isAlive()) {
//                        System.err.println("still alive after 400ms.  stopping");
                        myThread.stop();
//                        System.err.println("thread stopped? " + myThread.isAlive());
                    }
                } else {
//                    System.err.println("thread no longer alive");
                }

            }

        } // RuntimeException is caught higher up
        finally {
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

    final void callWillEat(String sheepID) {
        callPlayerFunction(WILL_EAT, sheepID);
    }

    final void callIsKeepingBusy() {
        callPlayerFunction(IS_KEEPING_BUSY);
    }

    final void callIsAttacked() {
        callPlayerFunction(IS_ATTACKED);
    }

    final Move calcMove() {

        if (isBusy()) {
            callPlayerFunction(IS_KEEPING_BUSY); // call "move"

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
                if (m.length() > allowedDistanceDivider * maxAllowedDistance + 0.000005) {
                    // String str = "illegal move: too long! " + m + ": " + m.length() + " > " + maxAllowedDistance;
                    //System.err.println(this.getClass() + str);

                    Tournament.logPlayerCrash(this.getClass(), new RuntimeException("illegal move: too long"));

                    // trim move
                    // we penalize the player a little by reducing the maxAllowedDistance
                    // to 1  (i.e., no diagonal moves allowed)
                    m = m.scaledToLength(1.0);
                } else if (allowedDistanceDivider != 1.0) {
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
     * Returns the name of the image file representing this player
     *
     * Implement this function to return a custom sprite.
     *
     * You may return a different image file (e.g., put it in the "pics" folder
     * and include that folder in the returned string (e.g.,
     * "pics/joes_sheep.jpg").
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

    /**
     * isKeepingBusy() is typically called while this wolf is eating a sheep.
     *
     * This is called once per iteration to allow for making observations.
     * Overridden as public for WolfPlayer.
     */
    private void isKeepingBusy() {
    }

    /**
     * Provide a string representation
     *
     * @return a short string suitable to depict the type of the player
     */
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

    /**
     * Check for equivalency
     *
     * @return true if this Player objects is equivalent to another one
     */
    // Don't override these
    @Override
    final public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
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

    /**
     * Return a code unique to the player object within this tournament.
     *
     * @return An integer containing a unique hash code.
     */
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
