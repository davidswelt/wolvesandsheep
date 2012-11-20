package was;

 class WasVideoGame extends ch.aplu.jgamegrid.GameGrid implements WasGameBackend {

    final GameBoard board;

    public WasVideoGame(GameBoard board) {
        super(50, 50, 10);
        this.board = board;
        //super(10, 10, 60, java.awt.Color.red, "sprites/reef.gif");
//    Fish nemo = new Fish();
//    addActor(nemo, new Location(2, 4));
//    show();
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
//
//  public static void main(String[] args)
//  {
//    new WasVideoGame();
//  }
}