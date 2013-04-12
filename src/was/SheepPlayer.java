package was;  // players may not use this package name
import java.util.ArrayList;



/**
 * Every sheep class has to extend SheepPlayer.
 * Implement move() (see Player class), and, optionally, isBeingEaten.
 * 
 * @author dr
 */
public abstract class SheepPlayer extends Player {
    
    
    @Override
    public GamePiece getPiece() 
    {
        return GamePiece.SHEEP;
    }
    
    // you may override if you like
    
    @Override
    public String imageFile () {
        return "pics/sheep_head_small.jpg";
    }
    
    /**
     * Get a list of all sheep in the game.
     * The ordering of the list is stable as long as sheep don't die.
     * @return a list of player objects (all sheep)
     */
    protected ArrayList<Player> getSheepObjects ()
    {
        return getGameBoard().findAllPlayers(GamePiece.SHEEP);
    }
    /**
     * Get specific other sheep in the game.
     * @param className name of the class defining the sheep to be 
     *                   obtained, e.g. "smith.Sheep"
     * @return a list of player objects (all sheep of this class)
     */
    protected ArrayList<Player> getSheepObjects (String className)
    {
        Class c = Tournament.name2class(className,new String[] {"", ".Sheep"});
        ArrayList<Player> pl = getSheepObjects();
        ArrayList<Player> result = new ArrayList();
        for (Player p : pl)
        {
            if (p.getClass() == c)
            {
                result.add(p);
            }
        }
        return result;
    }
        
    /**
     * Gets a shared property (any object) associated with a key.
     * The property is shared among all sheep in a team.
     * Properties are empty at the beginning of each game.
     * @param key string giving the key for lookup
     * @return the value, if any, of null if the property is not set.
     */
    protected Object getWhiteboardProperty(String key)
    {
        return getGameBoard().sheepWhiteboard.get(key);
    }
    /**
     * Sets a shared property (any object) associated with a key.
     * The property is shared among all sheep in a team.
     * @param key string giving the key for lookup (case-sensitive)
     * @param value any object to be associated with the key.
     * @return the value, if any, of null if the property is not set.
     */
    protected Object setWhiteboardProperty(String key, Object value)
    {
        return getGameBoard().sheepWhiteboard.put(key, value);
    }   
    
    // this inherits the move() method from Player.
    
    /**
     * isBeingEaten() is called just before this sheep is eaten
     * the method is called only if this player is a sheep.
     */
    public void isBeingEaten () {};
    
}
