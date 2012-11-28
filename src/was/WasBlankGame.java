package was;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;
import was.Player.GamePiece;


/*
 * The WasBlankGame is used to avoid visualizing the game porgress.
 * If the graphical UI is not in use, GameBoard uses WasBlankGame instead of WasVideoGame
 * which also implemeents the WasGameBackend interface.
 */
class WasBlankGame implements WasGameBackend {

    final GameBoard board;

    public WasBlankGame(GameBoard board) {

        this.board = board;
    }

    @Override
    public void show() {
        // empty
    }

    @Override
    public void hide() {
        // empty        
    }

    public void doPause() {
    }

    @Override
    public void addActor(Actor actor, GameLocation location) {
    }

    @Override
    public boolean removeActor(Actor actor) {
        return true;
    }
    // Wolf moves last
    static GamePiece[] moveOrder = new GamePiece[]{GamePiece.SHEEP, GamePiece.WOLF};

    @Override
    public void doRun() {
        while (!board.isFinished()) {
            System.out.println(board.getTime());
            board.gameNextTimeStep();

            // sheep
            for (GamePiece g : moveOrder) {
                for (GameBoard.Cell c : board.getCells()) {
                    if (c.player == null) { // player has been removed
                        continue;
                    }
                    if (c.getPiece() != g) {
                        continue;
                    }

                    if (c.player.isBusy()) {
                        // Wolf is eating
                        continue;
                    }
                    Move move = c.getPlayer().calcMove();

                    if (move != null) // valid move
                    {
                        // check move
                        if (move.length() > c.getPlayer().getMaxAllowedDistance()) // sqrt(1+1)
                        {
                            System.err.println("illegal move: too long! " + move.length());
                            // illegal move
                            // sheep won't move at all
                            continue;
                        }
                        board.noteMove(c.getPlayer(), move);

                    }
                }
            }
        }

        synchronized (board) {
            board.notify();
        }

    }

    public void act() {
        board.gameNextTimeStep();

        if (board.isFinished()) {
            // let the calling Gameboard thread know that we're done
            synchronized (board) {
                board.notify();
            }

        }

    }
//
//  public static void main(String[] args)
//  {
//    new WasVideoGame();
//  }
}