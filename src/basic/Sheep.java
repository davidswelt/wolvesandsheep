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

    @Override
    public void initialize() {
        // you cannot call "getGameBoard" in the constructor, as the
        // game board is created after all the players.
        board = getGameBoard();
    }

    @Override
    public Move move() {

        return new Move (rand.nextFloat()*2.0-1.0,rand.nextFloat()*2.0-1.0).scaledToLength(getMaxAllowedDistance());
    }
}
