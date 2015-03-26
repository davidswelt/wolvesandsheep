package was;  // players may not use this package name

/**
 * Every wolf class has to extend WolfPlayer. Implement move() (see Player
 * class), and, optionally, isEating.
 *
 * @author dr
 */
abstract public class WolfPlayer extends Player {

    // cannot be overridden - it's final.
    @Override
    final public GamePiece getPiece() {
        return GamePiece.WOLF;
    }

    @Override
    final public boolean isIncludedInHighScore() {
        return true;
    }
    
    
    // you may override if you like
    @Override
    public String imageFile() {
        return "pics/wolf_head_small_red.gif";
    }

    // this inherits the move() method from Player.
    
    /**
     * isKeepingBusy() is called while this wolf is eating a sheep.
     *
     * This is called once per iteration to allow for making observations. 
     */
    public void isKeepingBusy() {
    }
    /**
     * isEating() is called while this wolf is eating a sheep.
     * Deprecated. Use willEatSheep() instead.
     * This is called once per iteration to allow for making observations. 
     */
    public void isEating() {
    }
    
    /**
     * willEatSheep() is called just before the wolf is eating
     *
     * @param sheepID is the unique identifier for the sheep that is being
     * eaten.
     */
    public void willEatSheep(String sheepID) {
    }
     
    /**
     * isAttacked() is called when this wolf is about to be attacked by sheep
     * 
     */
    public void isAttacked() {
    }

}
