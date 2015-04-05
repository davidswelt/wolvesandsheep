package was;

/**
 * CoordinatesOutOfBoundsException for x,y ordinates outside of game board
 *
 * This exception may be thrown when a WAS method was called with coordinates
 * that fall outside of the current Gameboard object, for example (-1,44), or
 * (60,100) for a 100x100-size game board. Typically, a player class will have
 * called a method such as isEmptyCell with incorrect coordinates.
 *
 * @author Reitter
 */
public class CoordinatesOutOfBoundsException extends RuntimeException {

    /**
     * The X ordinate that may have caused the violation
     */
    public int coordX;
    /**
     * The Y ordinate that may have caused the violation
     */
    public int coordY;
    /**
     * The width of the applicable game board
     */
    public int sizeW;
    /**
     * The height of the applicable game board
     */
    public int sizeH;

    /**
     *
     * @param message
     */
    public CoordinatesOutOfBoundsException(String message) {
        super(message);
    }

    /**
     * Create CoordinatesOutOfBoundsException with additional information
     *
     * @param x the x-ordinate that may be out of bounds
     * @param y the y-ordinate that may be out of bounds
     * @param sizeW the width of the game board
     * @param sizeH the heigh of the game board
     */
    public CoordinatesOutOfBoundsException(int x, int y, int sizeW, int sizeH) {
        
        super();
        coordX = x;
        coordY = y;
        this.sizeW = sizeW;
        this.sizeH = sizeH;
    }
}
