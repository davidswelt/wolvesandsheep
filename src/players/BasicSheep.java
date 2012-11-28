/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package players;

import java.util.Random;
import was.Move;

/**
 *
 * @author dr
 */
public class BasicSheep extends was.SheepPlayer{
    
    private static Random rand = new Random();
        

    
    @Override
    public Move move() {

        return new Move (rand.nextFloat()*2.0-1.0,rand.nextFloat()*2.0-1.0).scaledToLength(getMaxAllowedDistance());
    }
}
