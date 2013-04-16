package was;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author dr
 */
public class HighScore extends TreeMap<String, Double> {

    TreeMap<String, Integer> uses = new TreeMap();

    final int COLUMNWIDTH = 8;
    
    HighScore setTitle(String t) {
        title = t;
        return this;
    }

    void addHighScore(HighScore other) {
        for (Map.Entry<String, Integer> e : other.uses.entrySet()) {
            noteUse(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Double> e : other.entrySet()) {
            inc(e.getKey(), e.getValue());
        }
    }

    void inc(String s) {
        //            System.out.println("increase " + s);
        inc(s, 1.0);
    }

    void inc(String s, double by) {
        //            System.out.println("increase " + s + " by " + by);
        put(s, new Double(get(s) + by));
    }

    void noteUse(String s) {
        noteUse(s, 1);
    }

    void noteUse(String s, int by) {
        Integer prev = uses.get(s);
        uses.put(s, new Integer(prev == null ? by : prev + by));
    }

    public double get(String s) {
        Double f = super.get(s);
        if (f == null) {
            return 0.0;
        } else {
            return f.floatValue();
        }
    }

    public double getNormalized(String s) {
        Double f = super.get(s);
        if (f == null) {
            return 0.0;
        } else {
            Integer n = uses.get(s);
            if (n == null || n.equals(0)) {
                return f.floatValue();
            }
            return f.floatValue() / n;
        }
    }

    public void print() {
        setAlignment();
        printKeys(new ArrayList(keySet()), false, null);
    }

    public class TreeValueComparator implements Comparator<String> {

        @Override
        public int compare(String a, String b) {
            return ((Double) HighScore.this.getNormalized(b)).compareTo(HighScore.this.getNormalized(a));
        }
    }
    int printAlignment = 20;

    void setAlignment() {
        printAlignment = 10;
        for (String k : keySet()) {
            printAlignment = Math.max(printAlignment, removePrefix(k).length());
        }
    }
    String title = "";

    String leftAlign(String s, int a) {
        return (a - s.length() > 0 ? String.format("%" + (a - s.length()) + "s", "") : "")
                + String.format("%s", s);
    }

    String rightAlign(String s, int a) {
        return String.format("%" + a + "s", s);
    }

    void printHeader(Collection<HighScore> extraColumns) {
        System.out.printf("\n%" + printAlignment + "s  %s", "", leftAlign(title, COLUMNWIDTH));
        if (extraColumns != null) {
            for (HighScore h : extraColumns) {
                if (h != null) {
                    System.out.printf("%s", leftAlign(h.title, COLUMNWIDTH));
                }
            }

        }
    }

    public void printKeys(List<String> keys, boolean sorted, Collection<HighScore> extraColumns) {
        // sort it
        Collections.sort(keys, new TreeValueComparator());
        for (String k : keys) {

            System.out.print(rightAlign(removePrefix(k), printAlignment) + ": ");
            System.out.print(rightAlign(String.format("%.3f", getNormalized(k)), COLUMNWIDTH));
            if (extraColumns != null) {
                for (HighScore h : extraColumns) {

                    if (h != null) {
                        System.out.print(rightAlign(String.format("%.3f", h.getNormalized(k)), COLUMNWIDTH));
                    }
                }

            }
            System.out.println();
        }
    }

    String removePrefix(String k) {
        StringTokenizer t = new StringTokenizer(k, k.contains("\\") ? "\\" : "");
        String result;
        result = t.nextToken();
        if (t.hasMoreTokens()) {
            result = t.nextToken();
        }
        return result;
    }

    public void printByCategory(Collection<HighScore> extraColumns) {
        printInternal(extraColumns, false);

    }

    public void printByClass(Collection<HighScore> extraColumns) {
        printInternal(extraColumns, true);

    }

    void printInternal(Collection<HighScore> extraColumns, boolean byClass) {

        setAlignment();
        printHeader(extraColumns);
        Set<String> keys = keySet();
        Map<String, List<String>> cats = new TreeMap(); // categories
        // categories
        for (String k : keys) {
            //categories marked with \ or .
            StringTokenizer t = new StringTokenizer(k, k.contains("\\") ? "\\" : (byClass? "." : ""));
            String classname;
            classname = t.nextToken();
            if (byClass && t.hasMoreTokens()) {
                classname = t.nextToken();
            }
            if (cats.get(classname) == null) {
                cats.put(classname, new ArrayList());
            }
            cats.get(classname).add(k); // add whole key
            // add whole key
        }
        // for each category, print it, sorted
        for (String k : new TreeSet<String>(cats.keySet())) {
            System.out.println();
            System.out.println(k + ":");
            printKeys(cats.get(k), true, extraColumns);
        }
    }
}
