package pyplayers;

import was.Move;
import was.WolfPlayer;

/**
 * The Java-side interface of the Python Wolf class
 * @author Yuan-Hsin Chen
 */
public interface PythonWolfWrapper {
    public void initialize(WolfPlayer myself);
    public Move move();
    public void isEating();
    public void isAttacked();    
}
