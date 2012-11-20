package was;

 class WasVideoGame extends ch.aplu.jgamegrid.GameGrid implements WasGameBackend {

    final GameBoard board;

    public WasVideoGame(GameBoard board) {
        super(board.getCols(), board.getRows(), 48);
        this.board = board;
        setBgColor(java.awt.Color.WHITE);
              
        
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