/* Example player */


package reitter;
import java.util.Random;
import was.Move;

/**
 *
 * @author dr
 */
public class SheepPlayer extends was.SheepPlayer {

    private static Random rand = new Random();
        
    Move direction = null;
    public SheepPlayer ()
    {
       super();
    }
    @Override
    public Move move() {
        if (direction == null)
        {
            direction = new Move (rand.nextFloat()*2.0-1.0,rand.nextFloat()*2.0-1.0).scaledToLength(getMaxAllowedDistance());
        }
        return direction;
    }

    
}
