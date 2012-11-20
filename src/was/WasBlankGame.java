package was;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;
import was.Player.GamePiece;

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
    
    @Override
    public void addActor(Actor actor, Location location) {
    }
    @Override
    public boolean removeActor(Actor actor) { return true;}

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
                    Move move = c.getPlayer().move();

                    // check move
                    if (move.length() > c.getPlayer().getMaxAllowedDistance()) // sqrt(1+1)
                    {
                        System.err.println("illegal move: too long! " + move.length());
                        // illegal move
                        // sheep won't move at all
                        continue;
                    }

                    c.move(move); // let the cell make a move

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