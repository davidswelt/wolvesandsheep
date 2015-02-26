package pyplayers;

import was.Move;
import was.WolfPlayer;

/**
 * An example sheep
 * @author dr
 */
public interface PythonWolfWrapper {
    public void initialize(WolfPlayer myself);
    public Move move();
}
