package basic;

import java.util.Random;
import was.GameBoard;
import was.Move;

/**
 * An example sheep
 * @author dr
 */
public class Sheep  extends was.SheepPlayer{
    
    private static Random rand = new Random();
    GameBoard board = null;
    Move direction = null; // direction we're taking

    @Override
    public void initialize() {
        // you cannot call "getGameBoard" in the constructor, as the
        // game board is created after all the players.
        board = getGameBoard();
    }

    @Override
    public Move move() {
        // if direction is not yet set, choose a random one
        if (direction == null) {
            direction = new Move(rand.nextFloat() * 2.0 - 1.0, rand.nextFloat() * 2.0 - 1.0);
            // scale direction to lenght and quantize it to maximize distance covered.
            direction = direction.scaledToLength(getMaxAllowedDistance()).quantized();
        }
        return direction;
        
        /* Note:
         * You may visualize a path using the visualizeTrack() method from Player.
         * The following code shows a path from the player's location to
         * location <5,5>.
         * You may visualize as many paths as you like.

            List<GameLocation> trk = new ArrayList();
            trk.add(getLocation());
            trk.add(new GameLocation(5,5));            
            removeVisualizations(); // remove all previously set tracks
            visualizeTrack(trk);
        */
    }
}
