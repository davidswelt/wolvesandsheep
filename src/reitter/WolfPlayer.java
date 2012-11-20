package reitter;

import was.Move;

/**
 *
 * @author dr
 */
public class WolfPlayer extends was.WolfPlayer {

    
    
    @Override
    public Move move() {
        return new Move(2,1).scaledToLength(getMaxAllowedDistance());
    }
    
}
