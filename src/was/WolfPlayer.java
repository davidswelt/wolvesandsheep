package was;  // players may not use this package name

/**
 * Every wolf class has to extend WolfPlayer.
 * Implement move() (see Player class), and, optionally, isEating.
 * @author dr
 */
abstract public class WolfPlayer extends Player {

    // this inherits the move() method from Player.

    /**
     * isEating() is called just before this wolf is eating a sheep
     * the method is called only if this player is a wolf.
     */
    public void isEating () {};
    
}
