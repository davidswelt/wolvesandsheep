package basic;

import java.util.Random;
import was.Move;

/**
 * An example sheep
 * @author dr
 */
public class Sheep  extends was.SheepPlayer{
    
    private static Random rand = new Random();
        

    
    @Override
    public Move move() {

        return new Move (rand.nextFloat()*2.0-1.0,rand.nextFloat()*2.0-1.0).scaledToLength(getMaxAllowedDistance());
    }
}
