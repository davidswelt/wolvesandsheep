package ta;

import was.GameBoard;
import was.Move;

/**
 * An example sheep
 * @author dr
 */
public class Sheep  extends was.SheepPlayer{

    private PythonSheepWrapper pythonSheep;
    GameBoard board = null;

    @Override
    public void initialize() {
        System.out.println("TA sheep initialize");
        /*
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.exec("import sys");
        interpreter.exec("sys.path.append('~/tmp/wolvesandsheep/src/ta')");
        */
        /*
        try {
            System.out.println("init sys");
            PySystemState.initialize();
            System.out.println("new sys");
            PySystemState sys = new PySystemState();
            System.out.println("app path");
            sys.path.append(new PyString("~/tmp/wolvesandsheep/src/ta"));
        } catch (Throwable e) {
            System.err.println(e);
        }
        System.out.println("path SET");
        */
        
        // Obtain an instance of the object factory
        JythonObjectFactory factory = JythonObjectFactory.getInstance();

        System.out.println(factory);
        // Call the createObject() method on the object factory by
        // passing the Java interface and the name of the Jython module
        // in String format. The returning object is casted to the the same
        // type as the Java interface and stored into a variable.
        pythonSheep = (PythonSheepWrapper) JythonObjectFactory.createObject(PythonSheepWrapper.class, "PythonSheep");
        
        /*
        PySystemObjectFactory factory = new PySystemObjectFactory(PythonSheepWrapper.class, "pythonsheep", "PythonSheep");
        System.out.println(factory);
        pythonSheep = (PythonSheepWrapper) factory.createObject();
        */
        System.out.println("GOT" + pythonSheep);
        // you cannot call "getGameBoard" in the constructor, as the
        // game board is created after all the players.
        board = getGameBoard();
        System.out.println("TA sheep initialize DONE");
    }

    @Override
    public Move move() {
        return null;
        //return pythonSheep.move();
    }
}
