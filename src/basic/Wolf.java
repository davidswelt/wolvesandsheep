package basic;

import java.util.Random;
import was.GameBoard;
import was.Move;

/**
 * Use this wolf (it sticks to the naming convention)
 *
 * @author dr
 */
public class Wolf extends was.WolfPlayer {

    private static Random rand = new Random();
    Move direction = null;
    GameBoard board = null;

    @Override
    public void initialize() {
        // you cannot call "getGameBoard" in the constructor, as the
        // game board is created after all the players.
        board = getGameBoard();
    }

    @Override
    public Move move() {
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
