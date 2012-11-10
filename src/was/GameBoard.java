package was;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author dr
 */
public class GameBoard {

    public static enum GamePiece {

        EMPTY, SHEEP, WOLF, OBSTACLE, PASTURE
    };

    final class Cell // don't allow extension - cells may move around within the board
    {

        GamePiece piece = GamePiece.EMPTY;
        Player player = null;
        int i = 0; // index in board
        int isBusyUntilTime = 0; // wolf is eating

        public Cell(Player player, int index) {
            if (player instanceof SheepPlayer) {
                this.piece = GamePiece.SHEEP;
            } else if (player instanceof WolfPlayer) {
                this.piece = GamePiece.WOLF;
            } else {
                throw new RuntimeException("GameBoard.Cell called with player that is neither SheepPlayer nor WolfPlayer.");
            }

            this.player = player;
            this.i = index;
        }

        public Cell(GamePiece piece, int index) {
            if (piece == GamePiece.SHEEP || piece == GamePiece.WOLF) {
                throw new RuntimeException("GameBoard.Cell called with SHEEP or WOLF as game piece.  Must give player object instead.");
            }
            if (piece != GamePiece.EMPTY && piece != GamePiece.OBSTACLE) {
                throw new RuntimeException("GameBoard.Cell called with unknown enum as game piece.");

            }
            this.piece = piece;
            this.player = null;
            this.i = index;
        }

        public GamePiece getPiece() {
            return piece;
        }

        public Player getPlayer() {
            return player;
        }

        public boolean isBusy() {
            return (GameBoard.this.currentTimeStep < isBusyUntilTime);
        }

        @Override
        public String toString () {
            
            switch (piece)
            {
                case EMPTY: return " ";
                case SHEEP: return "s";
                case WOLF: return "W";
                case OBSTACLE: return "#";
                case PASTURE: return ".";
            }
            return "";
        }
        
        // make a move
        boolean move(Move m) {

            if (isBusy()) {
                return false; // can't make a move
            }


            int x = GameBoard.this.getX(i) + m.delta_x;
            int y = GameBoard.this.getY(i) + m.delta_y;

            x = Math.max(0, x);
            y = Math.max(0, y);
            x = Math.min(GameBoard.this.cols-1, x);
            y = Math.min(GameBoard.this.rows-1, y);

            int idx = GameBoard.this.getIndex(x, y);

            // check if new x,y is free

            //System.out.println("x:"+x+" y:"+y);
            if (GameBoard.this.isEmptyCell(x, y)) {
                // swap empty cell

                GameBoard.this.swapCells(i, idx);
                isBusyUntilTime = GameBoard.this.currentTimeStep + 1; // for safety


                return true;

            } else if (piece == GamePiece.SHEEP && GameBoard.this.getPiece(idx) == GamePiece.PASTURE) {
                // a sheep makes it to the pasture

                // note score and remove player
                GameBoard.this.playerWins(player);

                

            } else if (piece == GamePiece.SHEEP && GameBoard.this.getPiece(idx) == GamePiece.WOLF) {
                wolfEatSheep(idx, i);
            } else if (piece == GamePiece.WOLF && GameBoard.this.getPiece(idx) == GamePiece.SHEEP) {
                wolfEatSheep(i, idx);
            }

            return false;

        }

        void wolfEatSheep(int WolfIndex, int SheepIndex) {
            // the sheep dies
            // maybe notify
            // the wolf eats
            // maybe notify

            Player sheep = GameBoard.this.getPlayer(SheepIndex);
            Player wolf = GameBoard.this.getPlayer(WolfIndex);
            
            wolf.isEating();
            sheep.isBeingEaten();

            Cell wolfCell = GameBoard.this.board.get(WolfIndex);
            wolfCell.isBusyUntilTime = GameBoard.this.currentTimeStep + GameBoard.this.wolfEatingTime;


            // move wolf to sheep's position
            GameBoard.this.board.set(SheepIndex, wolfCell);
            // replace wolf cell with empty cell
            GameBoard.this.board.set(WolfIndex, new Cell(GamePiece.EMPTY, WolfIndex));

            // scoring and removal of objects            
            GameBoard.this.playerWins(wolf);
            GameBoard.this.playerLoses(sheep);

            
        }
    }
    protected final int cols;
    protected final int rows;
    private List<Cell> board = new ArrayList<Cell>();
    private List<Cell> players = new ArrayList();
    protected HashMap<Player, int[]> scores = new HashMap();
    
     
    /**
     * distance in grid squares that a wolf can cover
     */
    public final double maxWolfDistance = 2;
    /**
     * number of steps it takes the wolf to eat a sheep
     */
    public final int wolfEatingTime = 4;
    final int MAXTIMESTEP = 30;
    final int NUMWOLVES = 1;

    int currentTimeStep = 0;
    
    
    GameBoard() {
        this(30, 30);
    }

    GameBoard(int width, int height) {
        cols = width;
        rows = height;

        for (int i = 0; i < cols * rows; i++) {
            board.add(new Cell(GamePiece.EMPTY, i));
        }
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

    /**
     * returns true if cell is empty
     * @param x column
     * @param y row
     * @return
     */
    public boolean isEmptyCell(int x, int y) {
        return isEmptyCell(getIndex(x, y));
    }

    boolean isEmptyCell(int i) {
        return board.get(i).piece == GamePiece.EMPTY;
    }

    /**
     * returns game piece on cell
     * @param x column
     * @param y row
     * @return
     */
    public GamePiece getPiece(int x, int y) {
        return getPiece(getIndex(x, y));
    }

    GamePiece getPiece(int i) {
        Cell c = board.get(i);
        return c.piece;
    }

    private ArrayList<Player> getPlayers ()
    {
        ArrayList<Player> ps = new ArrayList();
        for (Cell p : players)
        {
            if (p.player != null)
            {
                ps.add(p.player);
            }
        }
        return ps;
    }
    public boolean hasPlayer (Player p)
    {
        return scores.containsKey(p);
    }
    public int numPlayers ()
    {
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
        board.get(i1).i = i1;  // maybe use "notifyMove" instead
        board.get(i2).i = i2;

    }
    static Random rand = new Random();

    
    public void addPlayer(Player p) {

        // add a player
            // choose a cell

            int pos = -1;

            while (pos < 0 || !isEmptyCell(pos)) {
                // not efficient
                pos = rand.nextInt(board.size());

            }

            Cell c = new Cell(p, pos);
            board.set(pos, c);
            players.add(board.get(pos));

            scores.put(p, new int[1]);
       

    }

    public double allowedMoveDistance(GamePiece g) {
        if (g == GamePiece.SHEEP) {
            return 1.42;
        }
        if (g == GamePiece.WOLF) {
            return maxWolfDistance;
        }
        return 0;
    }
    // Wolf moves last
    static GamePiece[] moveOrder = new GamePiece[]{GamePiece.SHEEP, GamePiece.WOLF};

    public void makeMove() {

        currentTimeStep++; // advance time

        // sheep
        for (GamePiece g : moveOrder) {
            for (Cell c : players) {
                if (c.player == null)
                { // player has been removed
                    continue;
                }
                if (c.getPiece() != g) {
                    continue;
                }

                if (c.isBusy()) {
                    // Wolf is eating
                    continue;
                }
                Move move = c.getPlayer().move();

                // check move
                if (move.length() > allowedMoveDistance(g)) // sqrt(1+1)
                {
                    System.err.println("illegal move: too long! "+ move.length());
                    // illegal move
                    // sheep won't move at all
                    continue;
                }

                c.move(move); // let the cell make a move

            }
        }



    }

    public  Map<Player, int[]>  playGame ()
    {
        // while there are any sheep left
        
        
        while (players.size()>NUMWOLVES && currentTimeStep < MAXTIMESTEP)
        {
            System.out.println(currentTimeStep);
            makeMove();
            print();
        }
        return scores;
    }
        
    public void print ()
    {
        
        for (int i=0; i<board.size(); i++)
        {
            if (getX(i) == 0)
            {
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
    private void playerLoses (Player p)
    {
        removePlayer(p);
    }
    private void removePlayer (Player p)
    {
        // let's make sure there's no cell left
        
        for (Cell c : board)
        {
            c.player = null;
            c.piece = GamePiece.EMPTY;
        }        
        for (Cell c : players)
        {
            if (c.player == p)
            {
 //               players.remove(c);
                c.player = null; // player goes (do not remove from list)
                // removal from list would lead to ConcurrentModificationException
                c.piece = GamePiece.EMPTY;
                
                return;
            }
        }
    }
    
    void printScores ()
    {
        for (Map.Entry<Player, int[]>  s : scores.entrySet())
        {
            System.out.println(s.getKey() + ": " + s.getValue()[0]);
        }
    }
    
    
}
