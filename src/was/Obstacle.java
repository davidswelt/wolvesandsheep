package was;

/**
 * An obstacle is a form of player that does not move.
 * Obstacles are added to the board as if they were players.
 * @author dr
 */
public class Obstacle extends Player{

    @Override
    Player.GamePiece getPiece() {
        return Player.GamePiece.OBSTACLE;
    }

    @Override
    public String imageFile() {
        return "pics/obstacle_small.jpg";
    }

    static Move emptyMove = new Move(0,0);
    @Override
    public Move move() {
        return emptyMove;
    }   
    
}
