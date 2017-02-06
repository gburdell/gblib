/*
 * The MIT License
 *
 * Copyright 2014 gburdell.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package gblib;

import java.util.LinkedList;
import java.util.List;
import static gblib.MessageMgr.message;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

public class Util {

    public static boolean isUpperCase(final String s) {
        return s.equals(s.toUpperCase());
    }
    
    public static String escape(final char c) {
        String s = "";
        switch (c) {
            case '\n':
                s = "\\n";
                break;
            case '\t':
                s = "\\t";
                break;
            case '\\':
                s = "\\\\";
                break;
            default:
                s = Character.toString(c);
        }
        return s;
    }

    public static String escape(final String s) {
        return s.replace("\n", "\\n");
    }

    public static char unescape(final String s) {
        invariant((2 == s.length()) && (s.charAt(0) == '\\'));
        char c = 0;
        switch (s) {
            case "\\n":
                c = '\n';
                break;
            case "\\r":
                c = '\r';
                break;
            case "\\'":
                c = '\'';
                break;
            case "\\t":
                c = '\t';
                break;
            case "\\":
                c = '\\';
                break;
            default:
                invariant(false);
        }
        return c;
    }

    public static String stripDoubleQuotes(final String s) {
        int len = s.length();
        String ns = s.substring(1, len - 1);
        return ns;
    }

    public static void invariant(boolean c) {
        if (false == c) {
            Thread.dumpStack();
            System.exit(1);
        }
    }

    private final static String stDOT = ".";

    public static String getToolRoot() {
        String root = System.getProperty("tool.root");
        if (null == root) {
            root = stDOT;
        }
        return root;
    }

    public static void fatal(Exception ex) {
        PrintStream err = System.err;
        err.print(ex.getMessage());
        ex.printStackTrace(err);
        System.exit(1);
    }

    /**
     * Lookup property value.
     *
     * @param prop property name
     * @return true if property exists and set to "true" or else false.
     */
    public static boolean getPropertyAsBool(String prop) {
        String pv = System.getProperty(prop);
        boolean v = (null == pv) ? false : Boolean.parseBoolean(pv);
        return v;
    }

    public static int getPropertyAsInt(String nm) {
        int rval = Integer.MIN_VALUE;
        String str = System.getProperty(nm);
        if (null != str) {
            rval = Integer.parseInt(str);
        }
        return rval;
    }

    public static void abnormalExit(Exception ex) {
        System.err.println(ex.getMessage());
        ex.printStackTrace(System.err);
        System.exit(1);
    }

    public static List<String> arrayToList(String s[]) {
        return Arrays.asList(s);
    }

    public static <T> List<T> createList(T... items) {
        return Arrays.asList(items);
    }

    public static <T> int linearSearch(final T[] eles, final T ele) {
        for (int i = 0; i < eles.length; i++) {
            if (eles[i] == ele) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return a null x as an empty collection.
     */
    public static <T> T asEmpty(T x, T empty) {
        return (null != x) ? x : empty;
    }

    public static <T> T downCast(Object o) {
        return (T) o;
    }

    /**
     * Add only new elements to list.
     *
     * @param <T> type of list element.
     * @param to list to update with only new elements.
     * @param from list to get new elements from.
     */
    public static <T> void addAllNoDups(List<T> to, List<T> from) {
        for (T ele : from) {
            if (!to.contains(ele)) {
                to.add(ele);
            }
        }
    }

    private final static String m_nl = System.getProperty("line.separator");

    public static int streamCopy(BufferedInputStream from, BufferedOutputStream to) throws IOException {
        final int bufSz = 2048;
        byte buf[] = new byte[bufSz];
        int tlCnt = 0, cnt = 0;
        while (0 <= (cnt = from.read(buf, 0, bufSz))) {
            tlCnt += cnt;
            to.write(buf, 0, cnt);
        }
        to.flush();
        return tlCnt;
    }

    public static String nl() {
        return m_nl;
    }

    public static interface Equals<T> {

        public boolean equals(T a, T b);
    }

    /**
     * Check if objects are equal.
     *
     * @param <T> type of objects.
     * @param a 1st object (could be null).
     * @param b 2ns object (could be null).
     * @param eq optional interface to provide equals method.
     * @return true iff. both objects are null or both are non-null and equal.
     */
    public static <T> boolean equalsInclNull(T a, T b, Equals<T> eq) {
        if ((null == a) && (null == b)) {
            return true;
        }
        if ((null != a) && (null != b)) {
            if (null != eq) {
                return eq.equals(a, b);
            } else {
                return a.equals(b);
            }
        }
        return false;
    }

    public static <T> boolean equalsInclNull(T a, T b) {
        return equalsInclNull(a, b, null);
    }

    public static char intToChar(int l) throws ConversionException {
        if (l < Character.MIN_VALUE) {
            throw new ConversionException(l + "<" + Character.MIN_VALUE);
        } else if (l > Character.MAX_VALUE) {
            throw new ConversionException(l + ">" + Character.MAX_VALUE);
        }
        return (char) l;
    }

    public static int longToInt(long l) throws ConversionException {
        if (l < Integer.MIN_VALUE) {
            throw new ConversionException(l + "<" + Integer.MIN_VALUE);
        } else if (l > Integer.MAX_VALUE) {
            throw new ConversionException(l + ">" + Integer.MAX_VALUE);
        }
        return (int) l;
    }

    public static class ConversionException extends RuntimeException {

        public ConversionException(String msg) {
            super(msg);
        }
    }

    public static class Triplet<T1, T2, T3> extends Pair<T1,T2> {

        public Triplet() {
        }

        public Triplet(T1 a1, T2 a2, T3 a3) {
            super(a1,a2);
            e3 = a3;
        }
        public T3 e3;
    }

    /**
     * Generate comma separated String of list elements.
     *
     * @param <T> element type (must has toString() method).
     * @param eles list of elements.
     * @return comma separated String of list elements.
     */
    public static <T> String toCommaSeparatedString(final List<T> eles) {
        StringBuilder s = new StringBuilder();
        if (null != eles) {
            for (T ele : eles) {
                if (0 < s.length()) {
                    s.append(',');
                }
                s.append(ele.toString());
            }
        }
        return s.toString();
    }

    public static String toString(StringBuilder s) {
        return (null != s) ? s.toString() : "";
    }

    /**
     * Return union of 2 lists.
     *
     * @param <T>
     * @param l1 first list.
     * @param l2 second list.
     * @param allowDups true to allow duplicates.
     * @return union of lists.
     */
    public static <T> List<T> union(final List<T> l1, final List<T> l2, boolean allowDups) {
        LinkedList<T> u = new LinkedList<>(l1);
        for (T e : l2) {
            if (allowDups || (0 > u.indexOf(e))) {
                u.add(e);
            }
        }
        return u;
    }

    public static <T> List<T> union(final List<T> l1, final List<T> l2) {
        return union(l1, l2, false);
    }

    public static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            abnormalExit(new RuntimeException(msg));
        }
    }

    public static void assertFalse(boolean cond, String msg) {
        assertTrue(!cond, msg);
    }

    public static void assertNever(String msg) {
        abnormalExit(msg);
    }
    
    public static void abnormalExit(String msg) {
        abnormalExit(new Exception(msg));
    }

    public static void info(String code, Object... args) {
        message('I', code, args);
    }

    public static void warn(String code, Object... args) {
        message('W', code, args);
    }

    public static void info(int minLvl, String code, Object... args) {
        message(minLvl, 'I', code, args);
    }

    public static void warn(int minLvl, String code, Object... args) {
        message(minLvl, 'W', code, args);
    }

    public static void error(String code, Object... args) {
        message('E', code, args);
    }

    /**
     * Check if 2 files/dirs refer to same (physical file/dir).
     *
     * @param f1 first file/dir.
     * @param f2 second file/dir.
     * @return true if both refer to same physical location.
     */
    public static boolean filesAreSame(String f1, String f2) {
        return (new gblib.File(f1)).equals(new gblib.File(f2));
    }

    /**
     * Append join+s2 to s1 iff. s2 != null.
     *
     * @param s1 prefix.
     * @param join join s2 with this (iff. s2 != null).
     * @param s2 append join + s2 iff. s2 != null.
     * @return s1 with optional join+s2.
     */
    public static String appendIfNotNull(String s1, String join, String s2) {
        StringBuilder s = new StringBuilder(s1);
        if (null != s2) {
            s.append(join).append(s2);
        }
        return s.toString();
    }

}
