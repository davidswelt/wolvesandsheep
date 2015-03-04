package pyplayers;

import org.plyjy.factory.PySystemObjectFactory;
import was.Move;

/**
 * The Java-side sheep proxy
 * @author Yuan-Hsin Chen
 */
public class Sheep extends was.SheepPlayer{
    private static final PySystemObjectFactory factory = new PySystemObjectFactory(PythonSheepInterface.class, "PythonSheep", "PythonSheep");
    private PythonSheepInterface pythonSheep;

    @Override
    public void initialize() {
        pythonSheep = (PythonSheepInterface) factory.createObject();
        pythonSheep.initialize(this);
    }

    @Override
    public Move move() {
        return pythonSheep.move();
    }
    
    @Override
    public void isBeingEaten () {
        pythonSheep.isBeingEaten();
    };
    
    
}
