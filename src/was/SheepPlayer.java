package was;  // players may not use this package name

/**
 * Every sheep class has to extend SheepPlayer.
 * Implement move() (see Player class), and, optionally, isBeingEaten.
 * 
 * @author dr
 */
public abstract class SheepPlayer extends Player {
    
    
    public GamePiece getPiece() 
    {
        return GamePiece.SHEEP;
    }
    
    // you may override if you like
    
    public String imageFile () {
        return "pics/sheep_head_small.jpg";
    }
    
    
    
    
    // this inherits the move() method from Player.
    
    /**
     * isBeingEaten() is called just before this sheep is eaten
     * the method is called only if this player is a sheep.
     */
    public void isBeingEaten () {};
    
}
