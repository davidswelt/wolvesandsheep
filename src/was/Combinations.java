package was;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author dr
 */
public class Combinations {

    ArrayList items;

    public Combinations(ArrayList l) {
        items = l;
    }

    public ArrayList<ArrayList> drawNwithoutReplacement(int n) {
        return drawN(items, n);
    }
    static ArrayList combs;
    static ArrayList included;
    static ArrayList sourceArray;
    // draws n numbers out of 0..maxI

    static ArrayList<ArrayList> drawN(ArrayList array, int n) {
        combs = new ArrayList();
        included = new ArrayList();
        sourceArray = array;
        drawN(0, n, array.size());
        return combs;
    }

    static private void drawN(int i, int n, int numI) {
        if (included.size() == n) {
            // do not include any further
            combs.add(included.clone());
            return; // no recursion - no need to include any more
        }
        if (i >= numI) {
            return;
        }
        // recursion: once with i included, once without
        drawN(i + 1, n, numI); // without it
        included.add(sourceArray.get(i));
        drawN(i + 1, n, numI); // with it
        included.remove(sourceArray.get(i));
    }

    static void test() {
        Integer[] ia = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8};
        ArrayList test = new ArrayList();
        Collections.addAll(test, ia);

        Combinations cc = new Combinations(test);

        List x = cc.drawNwithoutReplacement(4);
        System.out.println(x);
    }

    public static void main(String a[]) {
        test();
    }
}
