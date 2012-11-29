package was;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import was.Player.GamePiece;

/**
 * The GameBoard class represents the state of the shared game board.
 * The class makes information about the board, the player positions available.
 * Internally, it handles updates to all of these data and calls on players to
 * make their moves.  The GameBoard class implements some of the game rules that
 * involve collisions of players.
 * @author dr
 */
public class GameBoard {
    
    private final int cols;
    private final int rows;
    private List<Player> board = new ArrayList<Player>();
    Queue<Player> players = new ArrayDeque();
    private HashMap<Player, int[]> scores = new HashMap();
    
    HashMap<String, Object> sheepWhiteboard = new HashMap();
    
    /**
     * distance in grid squares that a wolf can cover
     */
    public final double maxWolfDistance = 2;
    /**
     * number of steps it takes the wolf to eat a sheep
     */
    public final int wolfEatingTime = 4;
    /**
     * maximal number of steps before game ends
     */
    public final int MAXTIMESTEP = 80;
    final int NUMWOLVES = 1;
    int currentTimeStep = 0;

    
    WasGameBackend wasgamegrid = null;

    
        // make a move
        boolean movePlayer(Player player, Move m) {

            if (player == null)
            {
                throw new RuntimeException("movePlayer: trying to move an empty cell.");
            }
            if (player.isBusy()) {
                return false; // can't move Player
            }
            LOG(player +" " + player.getLocation() + " " + m);
            
            if (player != board.get(getIndex(player.getLocation())))
            {
                LOG("player location mismatch. player thinks its at "+player.getLocation()+" while board thinks its at "+getWolfPosition());
            }
            
            
            
            int x = player.x;
            int y = player.y;
            int i = getIndex(x,y);
            
            x += (int) m.delta_x;
            y += (int) m.delta_y;
            x = Math.max(0, x);
            y = Math.max(0, y);
            x = Math.min(cols - 1, x);
            y = Math.min(rows - 1, y);

            int idx = getIndex(x, y);
            
            if (idx == i)
            {
                return true; // empty move
            }
            
             GamePiece playerCellPiece = player.getPiece();
             GamePiece targetCellPiece = getPiece(idx);

            // check if new x,y is free

            //System.out.println("x:"+x+" y:"+y);
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
                wolfEatSheep(idx, i);
                return true;
            } else if (playerCellPiece == GamePiece.WOLF && targetCellPiece == GamePiece.SHEEP) {
                wolfEatSheep(i, idx);
                return true;
            } else if (playerCellPiece == GamePiece.WOLF && targetCellPiece == GamePiece.PASTURE) {
                // wolf can't movePlayer onto pasture
            }
            // else: still can't movePlayer.
            
            // do not execute the movePlayer.  return false to inform caller.
            return false;

        }
        void wolfEatSheep(int WolfIndex, int SheepIndex) {
            // the sheep dies
            //  notify
            // the wolf eats
            //  notify
            
            LOG("eating.");

            // this will cause a runtime exception if they're not sheep/wolf
            SheepPlayer sheep = (SheepPlayer) getPlayer(SheepIndex);
            WolfPlayer wolf = (WolfPlayer) getPlayer(WolfIndex);

            if (sheep == null)
            {
                LOG("wolfeatsheep: sheep is gone!" + getLoc(SheepIndex) + " wolf="+getLoc(WolfIndex));
            }
            if (wolf == null)
            {
                LOG("wolfeatsheep: wolf is gone!" + getLoc(WolfIndex)+ " sheep="+getLoc(SheepIndex));
                GameLocation wl = getWolfPosition();
                LOG("Wolf is now at "+wl);
            }
            
            wolf.isEating();
            sheep.isBeingEaten();

            wolf.keepBusyFor(wolfEatingTime);

            // scoring and removal of objects            
            playerWins(wolf);
            playerLoses(sheep); // removes sheep
            // sheep is gone now, cell should be empty
            swapCells(WolfIndex, SheepIndex); // move wolf to where the sheep was

        }
     
   GameBoard() {
        this(30, 30, false);
    }

    GameBoard(int width, int height, boolean ui) {
        cols = width;
        rows = height;

        for (int i = 0; i < cols * rows; i++) {
            board.add(null); // null is empty
        }

        if (ui) {
            // if UI, then this game board is backed by a wasvideogame (GameGrid)
            wasgamegrid = new WasVideoGame(this);
        } else
        {
            wasgamegrid = new WasBlankGame(this);
        }
    }

    public int getTime() {
        return currentTimeStep;
    }

    GameLocation getLoc (int index)
    {
        return new GameLocation(getX(index), getY(index));
    }
    int getX(int index) {
        return index % cols;
    }

    int getY(int index) {
        return (int) (index / cols);
    }
    int getIndex(GameLocation l) {
        return l.y * cols + l.x;
    }
    int getIndex(int x, int y) {
        return y * cols + x;
    }

    /**
     * Get height
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }
    /**
     * Get width
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }
    
    
    /**
     * Find the wolf
     * @return a was.GameLocation object
     */
    public GameLocation getWolfPosition() {
        ArrayList<was.GameLocation> p = findAllPlayersLoc(GamePiece.WOLF);
        if (p.size()>0) {
            return p.get(0);
        }
        
        return null;
    }
    
    /**
     * Get the positions of all the sheep on the board
     * @return an ArrayList containing was.GameLocation objects, with x,y positions
     */
    public ArrayList<GameLocation> getSheepPositions() {
        return findAllPlayersLoc(GamePiece.SHEEP);
    }
    /**
     * Get the positions of all the pastures on the board
     * @return an ArrayList containing was.GameLocation objects, with x,y positions
     */
    public ArrayList<GameLocation> getPasturePositions() {
        return findAllPlayersLoc(GamePiece.PASTURE);
    }
    /**
     * Get the positions of all the obstacles on the board
     * @return an ArrayList containing was.GameLocation objects, with x,y positions
     */
    public ArrayList<GameLocation> getObstaclePositions() {
        return findAllPlayersLoc(GamePiece.OBSTACLE);
    }
    
    ArrayList<GameLocation> findAllPlayersLoc(GamePiece type) {
        ArrayList<was.GameLocation> sp = new ArrayList();
        for (Player p : players) {
            if (p != null && p.getPiece() == type) {
                sp.add(p.getLocation());
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
     * Returns game piece currently present in an x,y position
     *
     * @param x column
     * @param y row
     * @return a GameBoard.GamePiece. You may check, e.g. for an obstacle at
     * position 4,5: mygameboard.getPiece(4.5)==GameBoard.GamePiece.OBSTACLE
     */
    public GamePiece getPiece(int x, int y) {
        return getPiece(getIndex(x, y));
    }

    GamePiece getPiece(int i) {
        Player p = board.get(i);
        if (p==null) {
            return GamePiece.EMPTY;
        }
        
        return p.getPiece();
    }
    
    // player objects may not bleed over to players
    // (except for sheep, which share)
    private ArrayList<Player> getPlayers() {
        
        return findAllPlayers(null);
    }
    
    ArrayList<Player> findAllPlayers(GamePiece type) {
        ArrayList<Player> ps = new ArrayList();
        for (Player p : players) {
            if (p != null && (type==null || p.getPiece() == type)) {
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

    private void swapCells(int i1, int i2) {
        LOG("swap "+getLoc(i1)+board.get(i1)+" "+getLoc(i2)+board.get(i2));
        Player tmp = board.get(i1);
        board.set(i1, board.get(i2));
        board.set(i2, tmp);

        // update cell objects

        if (board.get(i1) != null) {
            board.get(i1).setLoc(getX(i1), getY(i1));
        }
        
        if (board.get(i2) != null) {
            board.get(i2).setLoc(getX(i2), getY(i2));
        }
        
        

    }
    static Random rand = new Random();

    GameLocation randomEmptyLocation()
    {
        int pos = -1;

        while (pos < 0 || !isEmptyCell(pos)) {
            // not efficient
            // choose random position
            pos = rand.nextInt(board.size());

        }
        return new GameLocation(getX(pos),getY(pos));
    }
    
    void addPlayer(Player p, GameLocation loc) {

        p.setGameBoard(this);

        p.setMaxAllowedDistance(
                (p instanceof SheepPlayer)
                ? allowedMoveDistance(GamePiece.SHEEP)
                : (p instanceof WolfPlayer)
                ? allowedMoveDistance(GamePiece.WOLF)
                : 0);


        // add a player
        // choose a cell

        if (loc==null)
        {
            loc = randomEmptyLocation();
        }
        int locI = getIndex(loc.x,loc.y);
        
        p.setLoc(loc.x,loc.y);
        board.set(locI, p);
        players.add(p);

        scores.put(p, new int[1]);
        
        PlayerProxy pprox = new PlayerProxy(p);
        wasgamegrid.addActor(pprox, loc);
        p.setPlayerProxy(pprox);

    }

    /**
     * Calculates distance that a certain game piece is allowed to movePlayer
     *
     * @param g: GameBoard.GamePiece, e.g., GameBoard.GamePiece.SHEEP
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
     * Gets distance that a certain player is allowed to movePlayer
     *
     * @param p: player
     * @return distance in steps
     */
    public double allowedMoveDistance(Player p) {
        return p.getMaxAllowedDistance();
    }

    void makeMove() {

       



    }
    void test(int location ) {
    
           
    }
    
    static void LOG(String s) {
        
    }
    
//    
//    to do:
//    implement second variant of the backing game (both implemnent the same interface)
//       thread waiting interface
//       gameNextTimeStep callback
//       and a callback to update movePlayer (and check legality)  cell.movePlayer(movePlayer)
//               
               
               // callback from game backend
    synchronized boolean  noteMove(Player p, Move move)
    {
        test(1);
        return movePlayer(p, move);
        
    }
            
    

       // callback from game backend
    void gameNextTimeStep() {
        test(7);
        currentTimeStep++; // advance time
    }

    // callback from game backend
    boolean isFinished() {
        return !(players.size() > NUMWOLVES && currentTimeStep < MAXTIMESTEP);
    }

    Map<Player, int[]> playGame() {
        
        
        // initialize the players
        for (Player p : players) {
            p.initialize();
        }
        
        if (wasgamegrid != null) {
            
            
            wasgamegrid.show();

            // we're not calling make movePlayer

            wasgamegrid.doRun();  
            
            // the JGameGrid version will spawn a separate thread,
            // so we'll wait for it to finish:

            while (!isFinished()) {
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
            print();

        } 
        return scores;
    }

    public void print() {

        for (int i = 0; i < board.size(); i++) {
            if (getX(i) == 0) {
                System.out.println();
            }
            
            System.out.print(board.get(i)==null?" ":board.get(i));
                
        }
        System.out.println();
    }

    private void playerWins(Player p) {
        scores.get(p)[0]++;
        // remove player from list
        test(2);
        if (p instanceof SheepPlayer)
        {
            removePlayer(p);
        }
                test(3);
    }

    private void playerLoses(Player p) {
                test(4);
        removePlayer(p);
                test(5);
    }

    private void removePlayer(Player p) {

        
        if (p == null || wasgamegrid == null) // already deleted
        {
            return;
        } 
        if (p.playerProxy != null)
        {
            wasgamegrid.removeActor(p.playerProxy);
            p.playerProxy = null;
        }
            
        // let's make sure there's no cell left

        LOG("removing player.");
        int i = getIndex(p.x, p.y);
        if (board.get(i)==p)
        {
            board.set(i, null);
        }
        p.markDeleted();
        
        players.remove(p);
//        
//        for (Cell c : players) {
//            if (c.player == p) {
//                //               players.remove(c);
//                c.player.markDeleted();
//                c.player = null; // player goes (do not remove from list)
//                // removal from list would lead to ConcurrentModificationException
//                
//
//            }
//        }
        
    }

    void printScores() {
        for (Map.Entry<Player, int[]> s : scores.entrySet()) {
            System.out.println(s.getKey() + ": " + s.getValue()[0]);
        }
    }
}
