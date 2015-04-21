package was;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.GGExitListener;
import java.awt.Font;
import java.awt.Point;
import java.awt.RenderingHints;

/*
 * The WasVideoGame is the a form of game grid, based on the JGameGrid package.
 * It is used by the GameBoard whenever the graphical UI is used.
 * If the graphical UI is not in use, GameBoard uses WasBlankGame instead, 
 * which also implemeents the WasGameBackend interface, but does essentially nothing.
 */
class WasVideoGame extends ch.aplu.jgamegrid.GameGrid implements WasGameBackend, GGExitListener {

    final GameBoard board;

    public WasVideoGame(GameBoard board) {
        super(board.getCols(), board.getRows(), PlayerProxy.getCellSize(board));
        this.board = board;
        setBgColor(java.awt.Color.WHITE);
        setTitle("Wolves And Sheep");
        setSimulationPeriod(50);

        addExitListener(this);

    }

    @Override
    public void addActor(Actor actor, GameLocation location) {
        super.addActor(actor, new ch.aplu.jgamegrid.Location(location.x, location.y));
    }

    @Override
    public void act() {

        getBg().clear();

        ch.aplu.jgamegrid.GGBackground bg = getBg();
        bg.getContext().setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        bg.setPaintColor(java.awt.Color.black);
        bg.setFont(new Font("Arial", Font.PLAIN, 18));
        bg.drawText(board.scenario.toString(), new Point(10, 20));

        board.gameNextTimeStep();

        if (board.isFinished()) {
            // let the calling Gameboard thread know that we're done
            synchronized (board) {
                board.notify();
            }

        }

    }

    @Override
    public boolean notifyExit() {
        synchronized (board) {
            board.exitRequested();
            board.notify();

        }
        return false; // do not exit on my behalf
    }
}
