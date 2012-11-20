
package was;

/**
 *
 * @author dr
 */
public class Pasture extends Player{

    @Override
    GamePiece getPiece() {
        return GamePiece.PASTURE;
    }

    @Override
    public String imageFile() {
        return "pics/pasture_small.png";
    }

    static Move emptyMove = new Move(0,0);
    @Override
    public Move move() {
        return emptyMove;
    }
    
    
}
