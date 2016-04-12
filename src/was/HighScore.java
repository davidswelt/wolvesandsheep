package was;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains hacks. To do: re-write.
 *
 * @author dr
 */
public class HighScore extends TreeMap<String, Double> {

    TreeMap<String, Integer> uses = new TreeMap();
    TreeMap<String, ArrayList<Double>> vals = new TreeMap();

    boolean normalizing = false;  // set to true if noteUse is called once
    boolean trackIndividualValues = false; // true will cause a memory leak
    boolean showCI = false;
    boolean tabSep = false;
    String keyHeaderName = "";
    String extraColumnName = null;
    String extraColumnValue = null;
    String unitName = "";
    boolean printHeader = true;
    PrintStream out = System.out;

    /* Currently, not showing confidence intervals as it would
     be informative only if across players, i.e., aggregating all scenarios.
    
     */
    
    private final int outputPrecision = 3;
    public boolean printAsPercentage = false;
    String title = "";

    HighScore setTitle(String t, String keytitle) {
        title = t;
        keyHeaderName = keytitle;
        return this;
    }

    HighScore setExtraColumn(String title, String value) {
        extraColumnName = title;
        extraColumnValue = value;
        return this;
    }

    
    HighScore setUnit(String u) {
        unitName = u;
        return this;
    }
    
    HighScore setOutputFile(String outputCSV) {
        if (outputCSV == null) {
            out = System.out;
            tabSep = false;
        } else {
            try {
                if (new File(outputCSV).exists()) {
                    // if file (CSV) is already there, presumably
                    // it's already got a header
                    printHeader = false;
                }
                out = new PrintStream(
                        new FileOutputStream(outputCSV, true));
                tabSep = true;
            } catch (IOException ex) {
                Logger.getLogger(HighScore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return this;
    }

    synchronized void addHighScore(HighScore other) {
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

    synchronized void inc(String s, double by) {
        put(s, new Double(get(s) + by));
        if (trackIndividualValues) {
            if (!vals.containsKey(s)) {
                vals.put(s, new ArrayList<Double>());
            }
            vals.get(s).add(by);
        }
    }

    void noteUse(String s) {
        noteUse(s, 1);
    }

    synchronized void noteUse(String s, int by) {
        Integer prev = uses.get(s);
        uses.put(s, new Integer(prev == null ? by : prev + by));
        normalizing = true;
    }

    public void scale(String key, Double by) {
        Integer u = uses.get(key);
        if (u != null) {
            u = (int) (u * (1.0/by) + .5);
            uses.put(key, u);

        }
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
        if (super.get(s) == null || !vals.containsKey(s)) {
            return 0.0;
        }
        ArrayList<Double> vs = vals.get(s);
        int n = vs.size();
        if (n < 2) {
            return 0.0;
        }
        double m = super.get(s) / n;
        double v = 0.0;
        for (double x : vs) {
            v += (x - m) * (x - m);
        }
        v /= n - 1;
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

    // alignment is for the key on the left (name of item)
    void setAlignment() {
        if (tabSep) {
            printAlignment = 1;
        } else {
            printAlignment = 10;
            for (String k : keySet()) {
                printAlignment = Math.max(printAlignment, removePrefix(k).length());
            }
        }
    }
    
    // calculate width of a column
    int columnWidth()
    {
        return 5+outputPrecision+unitName.length(); // to do, use max. width of actual values (this assumes <10)
    }
        

    String leftAlign(String s, int a) {
        if (tabSep) {
            return s;
        }
        return (a - s.length() > 0 ? String.format("%" + (a - s.length()) + "s", "") : "")
                + String.format("%s", s);
    }

    String rightAlign(String s, int a) {
        if (tabSep) {
            return s;
        }
        return String.format("%" + a + "s", s);
    }

    synchronized void printHeader(Collection<HighScore> extraColumns) {
        String t = tabSep ? "\t" : "";
        out.printf("%s " + t + " %s" + t, leftAlign(keyHeaderName, printAlignment), leftAlign(title, columnWidth()));
        if (normalizing && showCI) {
            out.printf("%s" + t, leftAlign("CI", columnWidth()));
        }

        if (extraColumns != null) {
            for (HighScore h : extraColumns) {
                if (h != null) {
                    out.printf("%s" + t, leftAlign(h.title, columnWidth()));
                }
            }

        }

        if (extraColumnName != null) {
            out.printf("%s" + t, leftAlign(extraColumnName, extraColumnValue.length()));
        }

        out.println();
    }

    synchronized public void printKeys(List<String> keys, boolean sorted, Collection<HighScore> extraColumns) {

        String t = "";
        String format;
        if (tabSep) {
            format = "%f\t";
            t = "\t";
        } else {
            if (normalizing) {
                format = "%."+outputPrecision+"f";
            } else {
                format = "%.0f";
            }

            if (printAsPercentage) {
                format += "%%";
            }
        }
        // sort it
        Collections.sort(keys, new TreeValueComparator());
        for (String k : keys) {

            out.print(rightAlign(removePrefix(k), printAlignment) + (tabSep ? t : ": "));
            out.print(rightAlign(String.format(format, getNormalized(k))+unitName, columnWidth()));
            if (normalizing && showCI) {
                out.print(rightAlign(String.format("+-" + format, getSD(k) * 1.96), columnWidth()));
            }
            if (extraColumns != null) {
                for (HighScore h : extraColumns) {

                    if (h != null) {
                        Double n = h.getNormalized(k, null);
                        if (n != null) {

                            if (printAsPercentage) {
                                n *= 100;
                            }
                            out.print(rightAlign(String.format(format, n)+unitName, columnWidth()));
                        }
                    }
                }

            }

            if (extraColumnName != null) {
                out.printf(extraColumnValue + t);
            }
            out.println();
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
    synchronized public void printAsTable() {
        Set<String> keys = keySet();
        SortedSet<String> cols = new TreeSet();
        SortedSet<String> rows = new TreeSet();
        int rowlen = 1, collen = 1;

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
                String c = t.nextToken();
                cols.add(c);
                collen = Math.max(collen, getPackage(c).length());
            }
        }
        if (cols.size() > rows.size()) {
            printTable(collen, rows, cols);
        } else {
            printTable(rowlen, cols, rows);
        }
    }

    private void printTable(int rowlen, SortedSet<String> cols, SortedSet<String> rows) {
        // Header line (all column names)
        out.print(rightAlign("", rowlen) + "\t");
        for (String c : cols) {
            String cs = getPackage(c);
            out.print(leftAlign(cs, cs.length()) + "\t");

        }
        out.println();
        for (String r : rows) {
            out.print(rightAlign(getPackage(r), rowlen) + "\t");
            for (String c : cols) {
                String key = r + "\\" + c;
                String key2 = c + "\\" + r;
                // we don't know which one is which...
                Integer count = (int) (super.get(key) != null ? get(key) : get(key2));

                out.print(leftAlign(count.toString(), getPackage(c).length()) + "\t");

            }
            out.println();

        }
    }

    public void printByCategory(Collection<HighScore> extraColumns) {
        printInternal(extraColumns, false);
    }

    public void printByClass(Collection<HighScore> extraColumns) {
        printInternal(extraColumns, true);

    }

    synchronized void printInternal(Collection<HighScore> extraColumns, boolean byClass) {

        setAlignment();
        if (printHeader) {
            printHeader(extraColumns);
        }
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
            if (!tabSep) {
                out.println();
                out.println(k + ":");
            }
            printKeys(cats.get(k), true, extraColumns);
        }
    }

    static boolean testassert(double v, double gold) {
        System.out.print("SD=" + v + "   should be: " + gold + " ");
        if (Math.abs(v - gold) < Math.abs(v + gold) / 200.0) {
            System.out.println("OK");
            return true;
        } else {
            System.out.println("fail");
        }

        return false;
    }

    public static void test() {
        System.out.println("Unit tests for Highscore class:");
        HighScore h = new HighScore();
        HighScore h2 = new HighScore();
        int[] nums = {6, 3, 4, 6, 3, 0, -800, 20, 20};
        double[] nums2 = {7.34, 59.3, 575.12, -547.3, -9.0, 0, 0};

        for (int i : nums) {
            h.inc("pos", i);
            h.inc("neg", -i);
            h.noteUse("neg");
            h.inc("flt", i / 7.0);
            h.noteUse("flt");
        }
        testassert(h.getSD("pos"), 269.3488);
        testassert(h.getSD("neg"), 269.3488);
        testassert(h.getSD("flt"), 38.47839);
        for (double i : nums2) {
            h2.inc("pos", i);
            h2.inc("neg", -i);
            h2.noteUse("neg"); // should not matter
            h2.inc("flt", i / 771.0);
            h2.noteUse("flt"); // should not matter
        }
        testassert(h2.getSD("pos"), 324.7841);
        testassert(h2.getSD("neg"), 324.7841);
        testassert(h2.getSD("flt"), 0.4212505);
        h.addHighScore(h2);
        testassert(h.getSD("pos"), 288.4724);
        testassert(h.getSD("neg"), 288.4724);
        testassert(h.getSD("flt"), 28.73736);
        testassert(h.getNormalized("flt"), -6.582358);

    }

    public static void main(String a[]) {
        HighScore.test();

    }

}
