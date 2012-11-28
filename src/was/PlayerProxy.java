/*
 * This class acts as a bridge between the JGameGrid framework and the 
 * players derived from SheepPlayer and WolfPlayer.
 */

package was;

import ch.aplu.jgamegrid.*;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class PlayerProxy extends Actor {

    Player player;

    static final int cellSize = 10;
    
    PlayerProxy(Player player) {
        //                super("sprites/nemo.gif");
//bim.createGraphics().drawImage(newImg, 0, 0, null);
//FileOutputStream fos = new FileOutputStream(ofName);
//javax.imageio.ImageIO.write(bim, "jpg", fos);
//fos.close();

        super(true, scaledImage(player.imageFile()));
        this.player = player;
        
    }
      static BufferedImage imageToBufferedImage(Image im) {
     BufferedImage bi = new BufferedImage
        (im.getWidth(null),im.getHeight(null),BufferedImage.TYPE_INT_RGB);
     Graphics bg = bi.getGraphics();
     bg.drawImage(im, 0, 0, null);
     bg.dispose();
     return bi;
  }
  
    static BufferedImage scaledImage(String file)
    {
        try {
            return imageToBufferedImage(javax.imageio.ImageIO.read(new File(file)).getScaledInstance(cellSize, cellSize,Image.SCALE_SMOOTH));
        } catch (IOException ex) {
            Logger.getLogger(PlayerProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    
        //java.awt.image.BufferedImage bim = new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_RGB);
        return null;
    }
    

    @Override
    final public void act() {
        
        Move theMove = player.calcMove(); // this asks the player to decide its move

        if (theMove == null)
        {
            return;
        }
        
        int target_x = getX() + (int) theMove.delta_x;
        int target_y = getY() + (int) theMove.delta_y;


        // determine new direction
        // and calculate target angle

        // turn actor in the right direction (rotates sprite)
        double angle = 0;

        // East = 0, North=270, South = 90, West=180
        if (theMove.delta_x == 0)
        {
            angle = ((theMove.delta_y>0)? 90 : 270);
        } else
             if (theMove.delta_y == 0)
        {
            angle = ((theMove.delta_x>0)? 0 : 180);
        } else
        {
            angle = Math.toDegrees(Math.atan(theMove.delta_y / theMove.delta_x)); // in radians            
        }
        
        turn(angle - getDirection()); // relative turn

        // make the move (not precise - integer)
        move((int) theMove.length()); // round down so we don't overshoot

        // let's make sure we're in the right spot

        setX(target_x);
        setY(target_y);
        

//    if (!isMoveValid())
//      turn(180);
    }
}
