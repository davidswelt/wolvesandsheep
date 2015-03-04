package pyplayers;

import was.Move;
import was.SheepPlayer;

/**
 * The Java-side superclass for the actual Python class.
 * @author Yuan-Hsin Chen
 */
public interface PythonSheepWrapper {
    public void initialize(SheepPlayer myself);
    public Move move();
    public void isBeingEaten ();
}
