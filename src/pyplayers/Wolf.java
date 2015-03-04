package pyplayers;

import org.plyjy.factory.PySystemObjectFactory;
import was.Move;

/**
 * The Java-side wolf proxy
 * @author Yuan-Hsin Chen
 */
public class Wolf extends was.WolfPlayer{
    private static final PySystemObjectFactory factory = new PySystemObjectFactory(PythonSheepWrapper.class, "PythonWolf", "PythonWolf");
    private PythonWolfWrapper pythonWolf;

    @Override
    public void initialize() {
        pythonWolf = (PythonWolfWrapper) factory.createObject();
        pythonWolf.initialize(this);
    }

    @Override
    public Move move() {
        return pythonWolf.move();
    }
    
    @Override
    public void isEating() {
        pythonWolf.isEating();
    }
     
    @Override
    public void isAttacked() {
        pythonWolf.isAttacked();
    }
}
