package was;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class contains hacks. To do: re-write.
 *
 * @author dr
 */
public class HighScore extends TreeMap<String, Double> {

    TreeMap<String, Integer> uses = new TreeMap();
    TreeMap<String, ArrayList<Double>> vals = new TreeMap();

    final int COLUMNWIDTH = 8;
    boolean normalizing = false;  // set to true if noteUse is called once
    public boolean printAsPercentage = false;
    String title = "";

    HighScore setTitle(String t) {
        title = t;
        return this;
    }

    void addHighScore(HighScore other) {
        for (Map.Entry<String, Integer> e : other.uses.entrySet()) {
            noteUse(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Double> e : other.entrySet()) {
            put(e.getKey(), get(e.getKey()) + e.getValue());
        }
        for (Map.Entry<String, ArrayList<Double>> e : other.vals.entrySet()) {
            String s = e.getKey();
            if (other.vals.get(s) != null) {
                if (!vals.containsKey(s)) {
                    vals.put(s, new ArrayList<Double>());
                }
                vals.get(s).addAll(other.vals.get(s));
            }
        }
    }

    void inc(String s) {
        inc(s, 1.0);
    }

    void inc(String s, double by) {
        put(s, new Double(get(s) + by));
        if (!vals.containsKey(s)) {
            vals.put(s, new ArrayList<Double>());
        }
        vals.get(s).add(by);
    }

    void noteUse(String s) {
        noteUse(s, 1);
    }

    void noteUse(String s, int by) {
        Integer prev = uses.get(s);
        uses.put(s, new Integer(prev == null ? by : prev + by));
        normalizing = true;
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
        return getNormalized(s, 0.0);
    }

    public Double getNormalized(String s, Double def) {
        Double f = super.get(s);
        if (f == null) {
            return def;
        } else {
            Integer n = uses.get(s);
            if (n == null || n.equals(0)) {
                return (double) f.floatValue();
            }
            return (double) f.floatValue() / n;
        }
    }

    public Double getSD(String s) {
        if (vals.size() == 0) {
            return 0.0;
        }
        double m = getNormalized(s);
        double v = 0.0;
        for (double x : vals.get(s)) {
            v += (x - m) * (x - m);
        }
        v /= vals.size();
        return Math.sqrt(v);
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

        String format;
        if (normalizing) {
            format = "%.3f";
        } else {
            format = "%.0f";
        }

        if (printAsPercentage) {
            format += "%%";
        }
        // sort it
        Collections.sort(keys, new TreeValueComparator());
        for (String k : keys) {

            System.out.print(rightAlign(removePrefix(k), printAlignment) + ": ");
            System.out.print(rightAlign(String.format(format, getNormalized(k)), COLUMNWIDTH));
            if (normalizing) {
                System.out.print(rightAlign(String.format("+-" + format, getSD(k) * 1.96), COLUMNWIDTH));
            }
            if (extraColumns != null) {
                for (HighScore h : extraColumns) {

                    if (h != null) {
                        Double n = h.getNormalized(k, null);
                        if (n != null) {

                            if (printAsPercentage) {
                                n *= 100;
                            }
                            System.out.print(rightAlign(String.format(format, n), COLUMNWIDTH));
                        }
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

    public String getPackage(String k) {
        StringTokenizer t = new StringTokenizer(k, ".");
        if (t.hasMoreTokens()) {
            return t.nextToken();
        }
        return k;
    }

    /* Print this high score as a 2-dimensional table
     * keys are assumed to be of form cat1//cat2
     */
    public void printAsTable() {
        Set<String> keys = keySet();
        SortedSet<String> cols = new TreeSet();
        SortedSet<String> rows = new TreeSet();
        int rowlen = 1;

        // categories
        for (String k : keys) {
            String row = "";
            String column = "";
            StringTokenizer t = new StringTokenizer(k, k.contains("\\") ? "\\" : "");
            String classname;
            String r = t.nextToken();
            rows.add(r);
            rowlen = Math.max(rowlen, getPackage(r).length());
            if (t.hasMoreTokens()) {
                cols.add(t.nextToken());
            }
        }

        // Header line (all column names)
        System.out.print(rightAlign("", rowlen) + "\t");
        for (String c : cols) {
            String cs = getPackage(c);
            System.out.print(leftAlign(cs, cs.length()) + "\t");

        }
        System.out.println();
        for (String r : rows) {
            System.out.print(rightAlign(getPackage(r), rowlen) + "\t");
            for (String c : cols) {
                String key = r + "\\" + c;
                Integer count = (int) get(key);

                System.out.print(leftAlign(count.toString(), getPackage(c).length()) + "\t");

            }
            System.out.println();

        }
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

            // UGLY HACK.  DON'T DO THIS AT HOME.
            //categories marked with \ or .
            StringTokenizer t = new StringTokenizer(k, k.contains("\\") ? "\\" : (byClass ? "." : ""));
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
