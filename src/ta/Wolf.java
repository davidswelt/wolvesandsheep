package ta;
//-p -r 10 greene.Wolf derhammer.Sheep derhammer.Sheep derhammer.Sheep derhammer.Sheep

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import was.GameBoard;
import was.GameLocation;
import was.Move;

/**
 * Use this wolf (it sticks to the naming convention)
 *
 * @author dr
 */
public class Wolf extends was.WolfPlayer {
    GameBoard board = null;

    @Override
    public void initialize() {
        // you cannot call "getGameBoard" in the constructor, as the
        // game board is created after all the players.
        board = getGameBoard();
    }

    private double distanceBetween(GameLocation p1, GameLocation p2) {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;

        return Math.sqrt(dx * dx + dy* dy);
    }

    @Override
    public Move move() {
        ArrayList<GameLocation> sheepLocs = board.getSheepPositions();
        GameLocation myLoc = getLocation();
        double distance = Double.MAX_VALUE;
        GameLocation target = null;

        for (GameLocation loc : sheepLocs) {
            double d = distanceBetween(myLoc, loc);

            if (d < distance) {
                distance = d;
                target = loc;
            }
        }

        Move m = null;
        if (target != null) {
            m = new Move(target.x - myLoc.x, target.y - myLoc.y);
            double d = Math.min(getMaxAllowedDistance(), m.length());
            m = m.quantized(d);
        }

        return m;
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
