package was;

/**
 * A pasture is a form of player that does not move.
 * Pastures are added to the board as if they were players.
 * @author dr
 */
final public class Pasture extends Player{

    @Override
    GamePiece getPiece() {
        return GamePiece.PASTURE;
    }

    @Override
    public String imageFile() {
        return "pics/pasture_small.png";
    }
    @Override
    public String shortName()
    {
        return null;
    }
    @Override
    public Move move() {
        return null;
    }
    
    
}
