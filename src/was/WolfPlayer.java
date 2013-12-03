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

    // you may override if you like
    @Override
    public String imageFile() {
        return "pics/wolf_head_small_red.gif";
    }

    // this inherits the move() method from Player.
    /**
     * isEating() is called just before this wolf is eating a sheep
     *
     */
    public void isEating() {
    }
     
    /**
     * isAttacked() is called when this wolf is about to be attacked by sheep
     * 
     */
    public void isAttacked() {
    }

}
