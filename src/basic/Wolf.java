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
            direction = new Move(rand.nextFloat() * 2.0 - 1.0, rand.nextFloat() * 2.0 - 1.0).scaledToLength(getMaxAllowedDistance());
        }
        return direction;
    }
}
