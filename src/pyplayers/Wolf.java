package pyplayers;

import org.plyjy.factory.PySystemObjectFactory;
import was.Move;

/**
 * An example sheep
 * @author dr
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
}
