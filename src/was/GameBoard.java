package was;

import ch.aplu.jgamegrid.GGExitListener;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import was.Player.GamePiece;

/**
 * The GameBoard class represents the state of the shared game board. The class
 * makes information about the board, the player positions available.
 * Internally, it handles updates to all of these data and calls on players to
 * make their moves. The GameBoard class implements some of the game rules that
 * involve collisions of players.
 *
 * @author dr
 */
public class GameBoard {

    interface WolfSheepDelegate {

        public void wolfEatSheep(Player wolf, Player sheep);
    }
    boolean ui = false;
    private final int cols;
    private final int rows;
    private List<Player> board = new ArrayList<Player>();
    Deque<Player> players = new ArrayDeque();
    private Map<Player, int[]> scores = new HashMap();
    Map<String, Object> sheepWhiteboard = new HashMap();
    WolfSheepDelegate wolfEatSheepDelegate = null;
    /**
     * distance in grid squares that a wolf can cover
     */
    public final double maxWolfDistance = 2;
    /**
     * number of steps it takes the wolf to eat a sheep
     */
    public final int wolfEatingTime = 7;
    /**
     * maximal number of steps before game ends
     */
    int maxTimeStep;
    final int NUMWOLVES = 1;
    int currentTimeStep = 0;
    WasGameBackend wasgamegrid = null;

    // can move along line between a and b
    GameLocation clearShot(GameLocation a, GameLocation b) {

        // diagonal steps are OK
        Move m = new Move(b.x - a.x, b.y - a.y);

        double maxlen = m.length();
        m = m.scaledToLength(Math.sqrt(2));

        int lastemptyX = a.x;
        int lastemptyY = a.y;


        double x = a.x + 0.5; // we'll start in the center of the cell
        double y = a.y + 0.5;
        double distcovered = 0;
        double perstep = Math.sqrt(m.delta_x * m.delta_x + m.delta_y * m.delta_y);
        while (true) // limit search (to be sure we're terminating)
        {
            x += m.delta_x;
            y += m.delta_y;
            distcovered += perstep;
            if (distcovered > maxlen) {
                break;
            }

            int xx = (int) x;
            int yy = (int) y;

            if (getPiece(xx, yy) == GamePiece.OBSTACLE) {
                // return previous (known good) location
                return new GameLocation(lastemptyX, lastemptyY);
            }
            if (b.x == xx && b.y == yy) {
                break;
            }
            if (isEmptyCell(xx, yy)) {
                lastemptyX = xx;
                lastemptyY = yy;
            }
        }
        return b;
    }

    // make a move
    // do not call this directly.  player moves must take place
    // during call to calcMove() so the game frontend (GUI) can see them.
    private boolean movePlayer(Player player, Move m) {

        if (m == null) // obstacles, and the like
        {
            return true;
        }

        log(player + " moves " + m);
        if (player == null) {
            throw new RuntimeException("movePlayer: trying to move an empty cell.");
        }

        GamePiece playerCellPiece = player.getPiece();
        GameLocation loc = player.getLocation();


        if (playerCellPiece == GamePiece.WOLF) {
            // is move surrounded by sheep?
            // check all sheep in game

            // If there are at least three sheep next to the wolf (closer than
            // distance 2, they can hurt the wolf.  The wolf will need
            // time to recover, and it will then be only half as fast as before.

            int closeSheep = 0;
            // loc is pre-move location of this wolf.
            for (Player sp : findAllPlayers(GamePiece.SHEEP)) {
                GameLocation sloc = sp.getLocation();
                double d = Math.sqrt(Math.pow(loc.x - sloc.x, 2) + Math.pow(loc.y - sloc.y, 2));
                if (d < 2) // close than distance 2?
                {
                    closeSheep++;
                }
            }
            if (closeSheep >= 3) {
                sheepSurroundWolf(player);
                return true;
            }

        }




        if (player.isBusy()) {
            return false; // can't move Player
        }
        log(player + " " + player.getLocation() + " " + m);

        if (player != board.get(getIndexUnchecked(player.getLocation()))) {
            System.err.println("player " + player + "location mismatch. player thinks its at " + player.getLocation() + " while board has something else there.. ");
            System.err.println("The board has, at " + player.getLocation() + ":" + board.get(getIndex(player.getLocation())));
        }

        if (m.length() > allowedMoveDistance(player) + 0.000005) {
            return false;
        }

        int x = loc.x;
        int y = loc.y;
        int i = getIndexUnchecked(x, y);

        x += (int) m.delta_x;
        y += (int) m.delta_y;
        x = Math.max(0, x);
        y = Math.max(0, y);
        x = Math.min(cols - 1, x);
        y = Math.min(rows - 1, y);

        int idx = getIndexUnchecked(x, y);

        if (idx == i) {
            return true; // empty move
        }

        GameLocation nloc = clearShot(loc, new GameLocation(x, y));
        x = nloc.x;
        y = nloc.y;

        idx = getIndexUnchecked(x, y);
        if (idx == i) {
            return true; // empty move
        }
        GamePiece targetCellPiece = getPiece(idx);

        if (isEmptyCell(x, y)) {
            // swap empty cell

            swapCells(i, idx);
            player.keepBusyFor(1);


            return true;

        } else if (playerCellPiece == GamePiece.SHEEP && targetCellPiece == GamePiece.PASTURE) {
            // a sheep makes it to the pasture
            // note score and remove player
            playerWins(player);
            return true;

        } else if (playerCellPiece == GamePiece.SHEEP && targetCellPiece == GamePiece.WOLF) {
            // A sheep moving on top of a wolf  
            wolfEatSheep(idx, i);
            return true;
        } else if (playerCellPiece == GamePiece.WOLF && targetCellPiece == GamePiece.SHEEP) {
            // A wolf catching a sheep
            wolfEatSheep(i, idx);
            return true;
        } else if (playerCellPiece == GamePiece.WOLF && targetCellPiece == GamePiece.PASTURE) {
            // wolf can't movePlayer onto pasture
            // do nothing
            log("wolf tried to move onto pasture");
        } else if (targetCellPiece == GamePiece.OBSTACLE) {
            log("hit obstacle");
        }
        // else: still can't movePlayer.

        // do not execute the movePlayer.  return false to inform caller.
        return false;

    }

    void sheepSurroundWolf(Player wolfPlayer) {
        log("Sheep have overpowered the wolf.  Poor wolf!");
        wolfPlayer.keepBusyFor(wolfEatingTime);
        wolfPlayer.shortenMaxAllowedDistance(0.75); // cut speed by 25%
        wolfPlayer.callIsAttacked();
    }

    void wolfEatSheep(int wolfIndex, int sheepIndex) {
        // the sheep dies
        //  notify
        // the wolf eats
        //  notify

        log("eating.");

        // this will cause a runtime exception if they're not sheep/wolf
        SheepPlayer sheep = (SheepPlayer) getPlayer(sheepIndex);
        WolfPlayer wolf = (WolfPlayer) getPlayer(wolfIndex);

        if (sheep == null) {
            System.err.println("wolfeatsheep: sheep is gone!" + getLoc(sheepIndex) + " wolf=" + getLoc(wolfIndex));
        }
        if (wolf == null) {
            System.err.println("wolfeatsheep: wolf is gone!" + getLoc(wolfIndex) + " sheep=" + getLoc(sheepIndex));
            GameLocation wl = getWolfPosition();
            System.err.println("Wolf is now at " + wl);
        }

        if (wolf != null && sheep != null) {
            wolf.callWillEat(sheep.getID());
            sheep.callIsBeingEaten();
            wolf.keepBusyFor(wolfEatingTime);
            // scoring and removal of objects  
            if (wolfEatSheepDelegate != null) {
                wolfEatSheepDelegate.wolfEatSheep(wolf, sheep); // logging
            }
            playerWins(wolf);
            playerLoses(sheep); // removes sheep
        }
        // sheep is gone now, cell should be empty
        swapCells(wolfIndex, sheepIndex); // move wolf to where the sheep was

    }

    GameBoard() {
        this(30, 30, false, 80);
    }

    GameBoard(int width, int height, boolean ui, int maxTimeStep) {
        cols = width;
        rows = height;
        this.maxTimeStep = maxTimeStep;

        for (int i = 0; i < cols * rows; i++) {
            board.add(null); // null is empty
        }
        this.ui = ui;
        if (ui) {
            // if UI, then this game board is backed by a wasvideogame (GameGrid)
            wasgamegrid = new WasVideoGame(this);
        } else {
            wasgamegrid = new WasBlankGame(this);
        }
    }

    public int getMaxTimeStep() {
        return this.maxTimeStep;
    }

    public int getTime() {
        return currentTimeStep;
    }

    GameLocation getLoc(int index) {
        return new GameLocation(getX(index), getY(index));
    }

    int getX(int index) {
        return index % cols;
    }

    int getY(int index) {
        return (int) (index / cols);
    }

    int getIndex(GameLocation l) {
        return getIndex(l.x, l.y);
    }

    int getIndex(int x, int y) {
        if (x < 0 || x >= cols || y < 0 || y >= rows) {
            throw new CoordinatesOutOfBoundsException();
        }
        return getIndexUnchecked(x,y);
    }
    
    int getIndexUnchecked(GameLocation l) {
        return getIndexUnchecked(l.x, l.y);
    }
    int getIndexUnchecked(int x, int y) {
        return y * cols + x;
    }

    /**
     * Get height
     *
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get width
     *
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Find the wolf
     *
     * @return a was.GameLocation object
     */
    public GameLocation getWolfPosition() {
        ArrayList<was.GameLocation> p = findAllPlayersLoc(GamePiece.WOLF);
        if (p.size() > 0) {
            return p.get(0);
        }

        return null;
    }

    /**
     * Get the positions of all the sheep on the board
     *
     * @return an ArrayList containing was.GameLocation objects, with x,y
     * positions
     */
    public ArrayList<GameLocation> getSheepPositions() {
        return findAllPlayersLoc(GamePiece.SHEEP);
    }

    /**
     * Get the positions of all the pastures on the board
     *
     * @return an ArrayList containing was.GameLocation objects, with x,y
     * positions
     */
    public ArrayList<GameLocation> getPasturePositions() {
        return findAllPlayersLoc(GamePiece.PASTURE);
    }

    /**
     * Get the positions of all the obstacles on the board
     *
     * @return an ArrayList containing was.GameLocation objects, with x,y
     * positions
     */
    public ArrayList<GameLocation> getObstaclePositions() {
        return findAllPlayersLoc(GamePiece.OBSTACLE);
    }

    /**
     * Get the position of a named player. Return null if this player does not
     * or no longer exists.
     *
     * @param playerIDString the ID of the player
     * @return a GameLocation object
     */
    public GameLocation getPlayerPositionByID(String playerIDString) {
        // Note: this is not very efficient.
        // To Do: maintain a map for this if people end up using it.
        for (Player p : players) {
            if (p != null && !p.isGone() && p.getID().equals(playerIDString)) {
                return p.getLocation(); // this is a copy
            }
        }
        return null;
    }

    /**
     * Get the ID of the player at a given position. Return null if there is no
     * player at that position.
     *
     * @param loc the GameLocation object identifying the location
     * @return a String with the unique ID of the player instance
     */
    public String getPlayerIDatPosition(GameLocation loc) {
        if (loc.x < 0 || loc.x >= cols || loc.y < 0 || loc.y >= rows) {
            return null;
        }
        int idx = getIndexUnchecked(loc.x, loc.y);
        Player p = getPlayer(idx);
        if (p != null) {
            return p.getID();
        }
        return null;
    }

    // we return an ArrayList,  not a List
    // to make the public API down the line easier to Java novices
    ArrayList<GameLocation> findAllPlayersLoc(GamePiece type) {
        ArrayList<was.GameLocation> sp = new ArrayList();
        for (Player p : players) {
            if (p != null && !p.isGone() && p.getPiece() == type) {
                sp.add(p.getLocation());// this is a copy
            }
        }
        return sp;
    }

    void printPlayerOverview() {

        String str = "";
        for (Player p : players) {
            if (p instanceof WolfPlayer || p instanceof SheepPlayer) {
                str = p.getID() + p.getLocation() + " " + str; // reverse order
            }
        }
        System.out.println("Players: " + str);
    }

    /**
     * Structure containing information about a player at a given point in time
     * This information is available to all other players.
     */
    public class PublicPlayerInfo {

        /**
         * The type of the player (GamePiece)
         */
        public GamePiece type;
        /**
         * A unique ID string.
         */
        public String id;
        /**
         * The name of the class implementing the player.
         */
        public String className;
        /*
         * The current location of player.
         */
        public GameLocation loc;
        /*
         * The point in time for which the location and other info is valid.
         */
        public int timeValid;
    }

    /**
     * Get a snapshot of all players and their positions This contains player
     * representations of the obstacles and pastures along with all living sheep
     * and the wolf.
     *
     * @return a map from player ID (key) to a PublicPlayerInfo structure.
     */
    public Map<String, GameBoard.PublicPlayerInfo> findAllPlayers() {
        Map<String, PublicPlayerInfo> sp = new HashMap();
        for (Player p : players) {
            if (p != null && !p.isGone()) {
                GameBoard.PublicPlayerInfo pi = new GameBoard.PublicPlayerInfo();
                // none of this info is mutable, so we're not creating a reference leak.
                pi.type = p.getPiece();
                pi.id = p.getID();
                pi.className = p.getClass().getName();
                pi.loc = p.getLocation();
                pi.timeValid = getTime();

                sp.put(pi.id, pi);
            }
        }
        return sp;
    }

    /**
     * $Returns true if cell is empty
     *
     * @param x column
     * @param y row
     * @return true if cell X, Y is empty.
     */
    public boolean isEmptyCell(int x, int y) {
        return isEmptyCell(getIndex(x, y));
    }

    boolean isEmptyCell(int i) {
        return board.get(i) == null;
    }

    /**
     * Returns game piece currently present in an x,y position If x,y position
     * is not on the board, returns GamePiece.OBSTACLE.
     *
     * @param x column
     * @param y row
     * @return a GameBoard.GamePiece. You may check, e.g. for an obstacle at
     * position 4,5: mygameboard.getPiece(4.5)==GameBoard.GamePiece.OBSTACLE
     *
     */
    public GamePiece getPiece(int x, int y) {

        if (x < 0 || x >= cols || y < 0 || y >= rows) {
            return GamePiece.OBSTACLE;  // out of bounds - instead of throwing an Exception
        }

        return getPiece(getIndexUnchecked(x, y));
    }

    GamePiece getPiece(GameLocation i) {
        return getPiece(i.x, i.y);
    }

    GamePiece getPiece(int i) {
        Player p = board.get(i);
        if (p == null) {
            return GamePiece.EMPTY;
        }

        return p.getPiece();
    }

    // player objects may not bleed over to players
    // (except for sheep, which share)
    private List<Player> getPlayers() {

        return findAllPlayers(null);
    }

    ArrayList<Player> findAllPlayers(GamePiece type) {
        ArrayList<Player> ps = new ArrayList();
        for (Player p : players) {
            if (p != null && p.isActive() && (type == null || p.getPiece() == type)) {
                ps.add(p);
            }
        }
        return ps;
    }

    boolean hasPlayer(Player p) {
        return scores.containsKey(p);
    }

    /**
     * Returns the number of players present on the board.
     *
     * @return number of players
     */
    public int numPlayers() {
        // TODO: what about null players (removed)?
        return players.size();
    }

    private Player getPlayer(int i) // private because we don't want players to modify each other/call each other's methods.
    {
        return board.get(i);
    }

    void checkPlayerAt(Player p, int x, int y) {
        // check to make sure board is up to date
        if (board.get(getIndex(x, y)) != p) {
            throw new RuntimeException("player setLoc called without player being in right new place on board.");
        }
    }

    private void swapCells(int i1, int i2) {
        log("swap " + getLoc(i1) + board.get(i1) + " " + getLoc(i2) + board.get(i2));
        Player tmp = board.get(i1);

        board.set(i1, null);
        setPlayerAt(i1, board.get(i2));
        setPlayerAt(i2, tmp);

    }
    static Random rand = new Random();

    GameLocation randomEmptyLocation(List<GameLocation> e1, List<GameLocation> e2) {
        int pos = -1;
        GameLocation posl = null;

        while (pos < 0 || !isEmptyCell(pos) || (e1 != null && e1.contains(posl)) || (e2 != null && e2.contains(posl))) {
            // not efficient
            // choose random position
            pos = rand.nextInt(board.size());
            posl = new GameLocation(getX(pos), getY(pos));

        }
        return posl;
    }

    void setPlayerAt(int i, Player p) {
        Player ep = board.get(i);
        if (ep != null) {
            System.err.println("Trying to set player p=" + p.getID());
            System.err.println("Existing player at loc" + i + " is " + ep.getID());

            throw new RuntimeException("setPlayerAt - trying to step on existing player.");
        }

        board.set(i, p);
        if (p != null) {
            p.setLoc(getX(i), getY(i));
        }

    }

    Player addPlayer(Player p, GameLocation loc) {

        p.setGameBoard(this);

        p.setMaxAllowedDistance(
                (p instanceof SheepPlayer)
                ? allowedMoveDistance(GamePiece.SHEEP)
                : (p instanceof WolfPlayer)
                ? allowedMoveDistance(GamePiece.WOLF)
                : 0);


        // add a player
        // choose a cell

        if (loc == null) {
            loc = randomEmptyLocation(null, null);
        }
        int locI = getIndex(loc.x, loc.y);

        setPlayerAt(locI, p);
        players.add(p);

        scores.put(p, new int[1]);

        if (this.ui) {
            PlayerProxy pprox = new PlayerProxy(p);
            wasgamegrid.addActor(pprox, loc);
            p.setPlayerProxy(pprox);
        }
        return p;
    }

    /**
     * Calculates distance that a certain game piece is allowed to move
     *
     * @param g GameBoard.GamePiece, e.g., GameBoard.GamePiece.SHEEP
     * @return distance in steps
     */
    public double allowedMoveDistance(GamePiece g) {
        if (g == GamePiece.SHEEP) {
            return 1.42;
        }
        if (g == GamePiece.WOLF) {
            return maxWolfDistance;
        }
        return 0;
    }

    /**
     * Gets distance that a certain player is allowed to move
     *
     * @param p player
     * @return distance in steps
     */
    public double allowedMoveDistance(Player p) {
        return p.getMaxAllowedDistance();
    }

    static void log(String s) {
        /* No-op.  Used for debugging. */
    }

    // callback from game backend
    synchronized boolean noteMove(Player p, Move move) {

        return movePlayer(p, move);
    }

    synchronized void logPlayerCrash(Class pl, Exception ex) {
        Tournament.logPlayerCrash(pl, ex);
    }

    synchronized void exitRequested() {
        // set marker so that repetitive processes know to terminate
        Tournament.exitRequested = true;
    }
    Scenario scenario = null;
    // callback from game backend

    void gameNextTimeStep() {
        currentTimeStep++; // advance time
        if (scenario != null) {
            scenario.updateBoard(this, currentTimeStep);
        }

    }

    // callback from game backend
    boolean isFinished() {
        return getSheepPositions().isEmpty() || (currentTimeStep >= maxTimeStep);
    }

    Map<Player, int[]> playGame(boolean pauseInitially) {


        // initialize the players
        for (Player p : players) {
            p.callInitialize();
        }

        try {


            if (wasgamegrid != null) {


                wasgamegrid.show();

                // we're not calling make movePlayer

                wasgamegrid.doRun();
                if (pauseInitially) {
                    wasgamegrid.doPause();
                }

                // the JGameGrid version will spawn a separate thread,
                // so we'll wait for it to finish:

                if (!isFinished()) {
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(GameBoard.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                wasgamegrid.doPause();
                wasgamegrid.hide();
                wasgamegrid = null; // go away!
                //print();

            }
        } finally {
            // de-construct the players
            // right now this just releases some streams if needed.
            for (Player p : players) {
                p.finished();
            }
        }
        return scores;
    }

    public void print() {

        for (int i = 0; i < board.size(); i++) {
            if (getX(i) == 0) {
                System.out.println();
            }

            System.out.print(board.get(i) == null ? " " : board.get(i));

        }
        System.out.println();
    }

    private void playerWins(Player p) {
        scores.get(p)[0]++;
        // remove player from list
        if (p instanceof SheepPlayer) {
            removePlayer(p);
        }
    }

    private void playerLoses(Player p) {
        removePlayer(p);
    }

    void removePlayer(Player p) {


        if (p == null || wasgamegrid == null) // already deleted
        {
            return;
        }
        if (p.playerProxy != null) {
            wasgamegrid.removeActor(p.playerProxy);
            p.playerProxy = null;
        }

        // let's make sure there's no cell left

        log("removing player.");
        int i = getIndexUnchecked(p.getLocation());
        if (board.get(i) == p) {
            board.set(i, null);
        }
        p.markDeleted();

        // can't actually remove the player (concurrentModificationException)
        //players.remove(p);
//        
    }

    void printScores() {
        for (Map.Entry<Player, int[]> s : scores.entrySet()) {
            System.out.println(s.getKey() + ": " + s.getValue()[0]);
        }
    }
}
