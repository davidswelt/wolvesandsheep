package was;

/**
 * The type describing the state of a single cell in the game grid.
 */
final public class GameLocation /* implements Comparable */ {

    /**
     * The horizontal position (column)
     */
    public int x;
    /**
     * The vertical position (row)
     */
    public int y;

    /**
     * Make a new position
     *
     * @param x
     * @param y
     */
    public GameLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {

        return "L(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof GameLocation)) {
            return false;
        }
        return (x == ((GameLocation) o).x && y == ((GameLocation) o).y);

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.x;
        hash = 79 * hash + this.y;
        return hash;
    }

    /*
    public int compareTo(Object other) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (this == other) {
            return EQUAL;
        }
        if (other == null) {
            return BEFORE;
        }
        if (!(other instanceof GameLocation)) {
            return AFTER;
        }
        // comparison is made by hash code
        int hc = hashCode();
        int ohc = other.hashCode();
        if (ohc > hc) {
            return BEFORE;
        }
        if (ohc == hc) {
            return EQUAL;
        }
        return AFTER;
    }
    */
}
