package was;

import ch.aplu.jgamegrid.GameGrid;
import ch.aplu.jgamegrid.Location;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import was.Player.GamePiece;

/**
 *
 * @author dr
 */
public class GameBoard {

    WasGameBackend wasgamegrid = null;

    /**
     * The type describing the state of a single cell in the game grid.
     */


    final class Cell // don't allow extension - cells may move around within the board
    {
        Player player = null;
        int i = 0; // index in board

        Cell(int index) {
           
            this.player = null; // empty
            setLoc(index);
        }
        
        Cell(Player player, int index) {
            

            this.player = player;
            setLoc(index);
        }

        GamePiece getPiece() {
            if (player == null) {
                return GamePiece.EMPTY;
            }
            return player.getPiece();
        }

        Player getPlayer() {
            return player;
        }

        @Override
        public String toString() {
            if (player == null) {
                return " ";
            }
            return player.toString();
            
        }

        // make a move
        boolean move(Move m) {

            if (player == null)
            {
                throw new RuntimeException("Cell.move: trying to move an empty cell.");
            }
            if (player.isBusy()) {
                return false; // can't make a move
            }


            int x = GameBoard.this.getX(i) + (int) m.delta_x;
            int y = GameBoard.this.getY(i) + (int) m.delta_y;

            x = Math.max(0, x);
            y = Math.max(0, y);
            x = Math.min(GameBoard.this.cols - 1, x);
            y = Math.min(GameBoard.this.rows - 1, y);

            int idx = GameBoard.this.getIndex(x, y);

            // check if new x,y is free

            //System.out.println("x:"+x+" y:"+y);
            if (GameBoard.this.isEmptyCell(x, y)) {
                // swap empty cell

                GameBoard.this.swapCells(i, idx);
                player.keepBusyFor(1);
                

                return true;

            } else if (player.getPiece() == GamePiece.SHEEP && GameBoard.this.getPiece(idx) == GamePiece.PASTURE) {
                // a sheep makes it to the pasture

                // note score and remove player
                GameBoard.this.playerWins(player);

            } else if (player.getPiece() == GamePiece.SHEEP && GameBoard.this.getPiece(idx) == GamePiece.WOLF) {
                wolfEatSheep(idx, i);
            } else if (player.getPiece() == GamePiece.WOLF && GameBoard.this.getPiece(idx) == GamePiece.SHEEP) {
                wolfEatSheep(i, idx);
            } else if (player.getPiece() == GamePiece.WOLF && GameBoard.this.getPiece(idx) == GamePiece.PASTURE) {
                // wolf can't move onto pasture
            }

            return false;

        }

        void setLoc(int i)
        {
            this.i = i;
            if (player != null)
            {
                int x = GameBoard.this.getX(i);
                int y = GameBoard.this.getY(i);

                player.setLoc(x,y);
            }
        }
        
        void wolfEatSheep(int WolfIndex, int SheepIndex) {
            // the sheep dies
            //  notify
            // the wolf eats
            //  notify
            
            System.out.println("eating!");

            // this will cause a runtime exception if they're not sheep/wolf
            SheepPlayer sheep = (SheepPlayer) GameBoard.this.getPlayer(SheepIndex);
            WolfPlayer wolf = (WolfPlayer) GameBoard.this.getPlayer(WolfIndex);

            wolf.isEating();
            sheep.isBeingEaten();

            Cell wolfCell = GameBoard.this.board.get(WolfIndex);
            wolf.keepBusyFor(GameBoard.this.wolfEatingTime);

            // move wolf to sheep's position
            GameBoard.this.board.set(SheepIndex, wolfCell);
            // replace wolf cell with empty cell
            GameBoard.this.board.set(WolfIndex, new Cell(WolfIndex));

            // scoring and removal of objects            
            GameBoard.this.playerWins(wolf);
            GameBoard.this.playerLoses(sheep);


        }
    }
    private final int cols;
    private final int rows;
    private List<Cell> board = new ArrayList<Cell>();
    private List<Cell> players = new ArrayList();
    private HashMap<Player, int[]> scores = new HashMap();
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
    public final int MAXTIMESTEP = 30;
    final int NUMWOLVES = 1;
    int currentTimeStep = 0;

    GameBoard() {
        this(30, 30, false);
    }

    GameBoard(int width, int height, boolean ui) {
        cols = width;
        rows = height;

        for (int i = 0; i < cols * rows; i++) {
            board.add(new Cell(i));
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

    int getX(int index) {
        return index % cols;
    }

    int getY(int index) {
        return (int) (index / cols);
    }

    int getIndex(int x, int y) {
        return y * cols + x;
    }

    public int getRows() {
        return rows;
    }
    public int getCols() {
        return cols;
    }
    
    
    /**
     * returns true if cell is empty
     *
     * @param x column
     * @param y row
     * @return
     */
    public boolean isEmptyCell(int x, int y) {
        return isEmptyCell(getIndex(x, y));
    }

    boolean isEmptyCell(int i) {
        return board.get(i).getPiece() == GamePiece.EMPTY;
    }

    /**
     * returns game piece currently present in an x,y position
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
        Cell c = board.get(i);
        return c.getPiece();
    }

    private ArrayList<Player> getPlayers() {
        ArrayList<Player> ps = new ArrayList();
        for (Cell p : players) {
            if (p.player != null) {
                ps.add(p.player);
            }
        }
        return ps;
    }
     List<Cell> getCells() {
        return players;
    }
    boolean hasPlayer(Player p) {
        return scores.containsKey(p);
    }

    /**
     *
     * @return number of players
     */
    public int numPlayers() {
        return players.size();
    }

    private Player getPlayer(int i) // private because we don't want players to modify each other/call each other's methods.
    {
        Cell c = board.get(i);
        return c.player;
    }

    private void swapCells(int i1, int i2) {
        Cell m = board.get(i1);
        board.set(i1, board.get(i2));
        board.set(i2, m);

        // update cell objects

        board.get(i1).setLoc(i1);
        board.get(i2).setLoc(i2);
        
        

    }
    static Random rand = new Random();

    void addPlayer(Player p) {

        p.setGameBoard(this);

        p.setMaxAllowedDistance(
                (p instanceof SheepPlayer)
                ? allowedMoveDistance(GamePiece.SHEEP)
                : (p instanceof WolfPlayer)
                ? allowedMoveDistance(GamePiece.WOLF)
                : 0);




        // add a player
        // choose a cell

        int pos = -1;

        while (pos < 0 || !isEmptyCell(pos)) {
            // not efficient
            // choose random position
            pos = rand.nextInt(board.size());

        }

        Cell c = new Cell(p, pos);
        board.set(pos, c);
        players.add(board.get(pos));

        scores.put(p, new int[1]);
        
        PlayerProxy pprox = new PlayerProxy(p);
        wasgamegrid.addActor(pprox, new Location(getX(pos), getY(pos)));
        p.setPlayerProxy(pprox);

    }

    /**
     * Calculates distance that a certain game piece is allowed to move
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
     * Gets distance that a certain player is allowed to move
     *
     * @param p: player
     * @return distance in steps
     */
    public double allowedMoveDistance(Player p) {
        return p.getMaxAllowedDistance();
    }

    void makeMove() {

       



    }
    
//    
//    to do:
//    implement second variant of the backing game (both implemnent the same interface)
//       thread waiting interface
//       gameNextTimeStep callback
//       and a callback to update move (and check legality)  cell.move(move)
//               
               
               // callback from game backend
    boolean noteMove(Player p, Move move)
    {
        for(Cell c : players)
        {
            if (c.player==p) // cell found
            {
                return c.move(move); // check for collisions etc
            }
        }
//        Cell c = getCellForPlayer(p);
//        c.move(move);
        return false;
    }
            
    

       // callback from game backend
    void gameNextTimeStep() {
        currentTimeStep++; // advance time
    }

    // callback from game backend
    boolean isFinished() {
        return !(players.size() > NUMWOLVES && currentTimeStep < MAXTIMESTEP);
    }

    Map<Player, int[]> playGame() {
        // while there are any sheep left

        if (wasgamegrid != null) {
            wasgamegrid.show();

            // we're not calling make move

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
            System.out.print(board.get(i));
        }
        System.out.println();
    }

    private void playerWins(Player p) {
        scores.get(p)[0]++;
        // remove player from list

        removePlayer(p);
    }

    private void playerLoses(Player p) {
        removePlayer(p);
    }

    private void removePlayer(Player p) {

        if (p == null || wasgamegrid == null)
        {
            return;
        }
        wasgamegrid.removeActor(p.playerProxy);

        // let's make sure there's no cell left

        for (Cell c : board) {
            c.player = null;
        }
        for (Cell c : players) {
            if (c.player == p) {
                //               players.remove(c);
                c.player = null; // player goes (do not remove from list)
                // removal from list would lead to ConcurrentModificationException
                

                return;
            }
        }
    }

    void printScores() {
        for (Map.Entry<Player, int[]> s : scores.entrySet()) {
            System.out.println(s.getKey() + ": " + s.getValue()[0]);
        }
    }
}
