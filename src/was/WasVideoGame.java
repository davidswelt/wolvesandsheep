package was;

import ch.aplu.jgamegrid.Actor;

/*
 * The WasVideoGame is the a form of game grid, based on the JGameGrid package.
 * It is used by the GameBoard whenever the graphical UI is used.
 * If the graphical UI is not in use, GameBoard uses WasBlankGame instead, 
 * which also implemeents the WasGameBackend interface, but does essentially nothing.
 */
class WasVideoGame extends ch.aplu.jgamegrid.GameGrid implements WasGameBackend {

    final GameBoard board;

    public WasVideoGame(GameBoard board) {
        super(board.getCols(), board.getRows(), PlayerProxy.cellSize);
        this.board = board;
        setBgColor(java.awt.Color.WHITE);


    }

     @Override
    public void addActor(Actor actor, GameLocation location) {
         super.addActor(actor, new ch.aplu.jgamegrid.Location (location.x, location.y));
    }

    
    @Override
    public void act() {
        board.gameNextTimeStep();

        if (board.isFinished()) {
            // let the calling Gameboard thread know that we're done
            synchronized (board) {
                board.notify();
            }

        }

    }
}