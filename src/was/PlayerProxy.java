package was;

import ch.aplu.jgamegrid.*;

class PlayerProxy extends Actor {

    Player player;

    PlayerProxy(Player player) {
        //                super("sprites/nemo.gif");
        super(player.imageFile());
        this.player = player;
        
        java.awt.image.BufferedImage bi = getImage();
        
        
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
