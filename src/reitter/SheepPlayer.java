/* Example player */


package reitter;
import was.Move;

/**
 *
 * @author dr
 */
public class SheepPlayer extends was.SheepPlayer {

         
    @Override
    public Move move() {
        return new Move (0,-1);
    }

    
}
