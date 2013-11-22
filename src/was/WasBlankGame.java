package was;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;
import java.util.Iterator;
import java.util.ListIterator;
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
//    static GamePiece[] moveOrder = new GamePiece[]{GamePiece.SHEEP, GamePiece.WOLF};

    @Override
    public void doRun() {
        while (!board.isFinished()) {

            board.gameNextTimeStep();

            // sheep
            Iterator li = board.players.descendingIterator();
            // Iterate in reverse.  
            // JGameGrid calls act() on the last-added player first.
            // So we'll do the same.
            while (li.hasNext()) {
                Player c = (Player) li.next();


                if (c == null || c.isGone()) { // player has been removed
                    continue;
                }
                if (c.isBusy()) {
                    // Wolf is eating
                    continue;
                }
                Move move = c.calcMove(); // calls gameboard.noteMove
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