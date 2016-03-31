/*
 * This class acts as a bridge between the JGameGrid framework and the 
 * players derived from SheepPlayer and WolfPlayer.
 */
package was;

import ch.aplu.jgamegrid.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class PlayerProxy extends Actor {

    Player player;
    static int cellSize = 12;

    PlayerProxy(Player player) {
        //                super("sprites/nemo.gif");
//bim.createGraphics().drawImage(newImg, 0, 0, null);
//FileOutputStream fos = new FileOutputStream(ofName);
//javax.imageio.ImageIO.write(bim, "jpg", fos);
//fos.close();

        super(true, scaledImage(player));
        this.player = player;

    }

    static BufferedImage imageToBufferedImage(Image im) {
        BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics bg = bi.getGraphics();
        bg.drawImage(im, 0, 0, null);
        bg.dispose();
        return bi;
    }

    static int getCellSize(GameBoard board) {

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getScreenSize();

        int csize = (int) ((dim.height - 120) / board.getRows());

        cellSize = Math.min(15, csize - 1);
        return cellSize;
    }

    static BufferedImage scaledImage(Player player) {
        try {
            String s = player.imageFile();

            URL r = player.getClass().getResource(s);
            if (r == null) {
                r = PlayerProxy.class.getResource(s);
            }
            Image img = javax.imageio.ImageIO.read(r);
            return imageToBufferedImage(img.getScaledInstance(getCellSize(player.getGameBoard()), getCellSize(player.getGameBoard()), Image.SCALE_SMOOTH));
        } catch (IOException ex) {
            Logger.getLogger(PlayerProxy.class.getName()).log(Level.SEVERE, null, ex);
        }

        //java.awt.image.BufferedImage bim = new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_RGB);
        return null;
    }
    List<java.awt.Point> trackPolygon = new ArrayList();
    GeneralPath track = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
    List<List<GameLocation>> generalTracks = new ArrayList();

    @Override
    final public void act() {

        if (player.isGone()) {
            return;
        }

        if (track.getCurrentPoint() == null) {
            track.moveTo(getPixelLocation().x, getPixelLocation().y);
        }

        GameLocation l = player.getLocation();
        int prev_x = l.x;
        int prev_y = l.y;

        Move theMove = player.calcMove(); // this asks the player to decide its move

        if (theMove == null) {
            return;
        }
        if (player.isGone()) {
            return;
        }
        GameLocation targetloc = player.getLocation();
        int target_x = targetloc.x; // getX() + (int) theMove.delta_x;
        int target_y = targetloc.y; // getY() + (int) theMove.delta_y;

        // determine new direction
        // and calculate target angle
        // turn actor in the right direction (rotates sprite)
        double angle = 0;

        // East = 0, North=270, South = 90, West=180
        if (theMove.delta_x == 0) {
            angle = ((theMove.delta_y > 0) ? 90 : 270);
        } else if (theMove.delta_y == 0) {
            angle = ((theMove.delta_x > 0) ? 0 : 180);
        } else {
            angle = Math.toDegrees(Math.atan(theMove.delta_y / theMove.delta_x)); // in radians            
        }

        // let JGameGrid know...
        turn(angle - getDirection()); // relative turn

        // make the move (not precise - integer)
        move((int) theMove.length()); // round down so we don't overshoot

        // let's make sure we're in the right spot
        setX(target_x);
        setY(target_y);

        if (player instanceof WolfPlayer || player instanceof SheepPlayer) {

            // update the polygon
            Point pt = getPixelLocation();
            track.lineTo(pt.x, pt.y);
            showPath(track, player.trackColor(), player.shortName(), pt);

            for (List<GameLocation> path : generalTracks) {
                showTrack(path, java.awt.Color.GRAY, false);
            }
        }
    }

    void showTrack(List<GameLocation> path, java.awt.Color color, boolean showPoints) {
        // now make polygon set by player
        java.awt.Shape caret = new javax.swing.text.DefaultCaret();
        
        GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        if (path.size() > 0) {
            boolean first = true;
            Point pt = getPixelLocation(); // placeholder, probably
            for (GameLocation point : path) {
                pt = gameGrid.toPoint(new Location(point.x, point.y));
                if (first) {
                    p.moveTo(pt.x, pt.y);
                    first = false;
                } else {
                    p.lineTo(pt.x, pt.y);
                }
//                if (showPoints)
//                { // doesn't work - will draw line to origin 0,0
//                    p.append(caret, first);
//                }
            }          
            showPath(p, color, null, pt);
            player.getGameBoard().wasgamegrid.refresh();
        }
    }
    /*
     * Add a list of GameLocations to be visualized.
     * Note: List is not copied and may be changed by caller
     * later.
     */

    public void visualizeTrack(List<GameLocation> locList) {
        generalTracks.add(locList);
    }
    /*
     * Remove all additional visualizations.
     */

    public void removeVisualizations() {
        generalTracks.clear();
    }

    private void showPath(GeneralPath track, java.awt.Color color, String label, Point labelPos) {

        if (color != null) {
            ch.aplu.jgamegrid.GGBackground bg = getBackground();
            bg.setPaintColor(color);
            bg.setLineWidth(2);
            bg.drawGeneralPath(track);
            if (label != null) {

                bg.setPaintColor(java.awt.Color.black);
                bg.setFont(new Font("TimesRoman", Font.PLAIN, 14));
                bg.drawText(label, new Point(labelPos.x + 5, labelPos.y));
            }
        }

    }
}
