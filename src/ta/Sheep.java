package ta;

import org.plyjy.factory.PySystemObjectFactory;
import was.Move;

/**
 * An example sheep
 * @author dr
 */
public class Sheep extends was.SheepPlayer{
    private static final PySystemObjectFactory factory = new PySystemObjectFactory(PythonSheepWrapper.class, "PythonSheep", "PythonSheep");
    private PythonSheepWrapper pythonSheep;

    @Override
    public void initialize() {
        pythonSheep = (PythonSheepWrapper) factory.createObject();
        pythonSheep.initialize(this);
    }

    @Override
    public Move move() {
        return pythonSheep.move();
    }
}
