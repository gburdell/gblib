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
import java.util.Arrays;

public class Util {

    /**
     * Check if objects are equal.
     *
     * @param <T> type of objects.
     * @param a 1st object (could be null).
     * @param b 2ns object (could be null).
     * @return true iff. both objects are null or both are non-null and equal.
     */
    public static <T> boolean equalsInclNull(T a, T b) {
        if ((null == a) && (null == b)) {
            return true;
        }
        if ((null != a) && (null != b)) {
            return a.equals(b);
        }
        return false;
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

    public static class ConversionException extends Exception {

        public ConversionException(String msg) {
            super(msg);
        }
    }

    public static String nl() {
        return System.lineSeparator();
    }

    /**
     * Return null as an empty collection.
     *
     * @param <T> base type.
     * @param x scalar to test for null.
     * @param empty empty collection to return iff. x is null.
     * @return x or empty (if x is null).
     */
    public static <T> T asEmpty(T x, T empty) {
        return (null != x) ? x : empty;
    }

    @SuppressWarnings("unchecked")
    public static <T> T downCast(Object o) {
        return (T) o;
    }

    public static class Pair<T1, T2> {

        public Pair() {
        }

        public Pair(T1 a1, T2 a2) {
            e1 = a1;
            e2 = a2;
        }
        public T1 e1;
        public T2 e2;
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

    private final static String stDOT = ".";

    public static String getToolRoot() {
        String root = System.getProperty("tool.root");
        if (null == root) {
            root = stDOT;
        }
        return root;
    }

    public static void assertFalse(boolean cond, String msg) {
        if (false != cond) {
            abnormalExit(new RuntimeException(msg));
        }
    }

    public static void abnormalExit(Exception ex) {
        System.err.println(ex.getMessage());
        ex.printStackTrace(System.err);
        System.exit(1);
    }

    public static void abnormalExit(String msg) {
        abnormalExit(new Exception(msg));
    }

    public static void info(String code, Object... args) {
        message('I', code, args);
    }

    public static void error(String code, Object... args) {
        message('E', code, args);
    }

    public static void warn(String code, Object... args) {
        message('W', code, args);
    }
}
