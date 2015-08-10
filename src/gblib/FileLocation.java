/*
 * The MIT License
 *
 * Copyright 2015 gburdell.
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

/**
 *
 * @author gburdell
 */
public class FileLocation {

    public FileLocation(final File file, final int lnum, final int col) {
        m_file = file;
        m_lnum = lnum;
        m_col = col;
    }
    
    /**
     * Return new FileLocation which is copy of this one, with column offset.
     * @param colOffset add column offset.
     * @return new FileLocation with column offset.
     */
    public FileLocation offset(final int colOffset) {
        return new FileLocation(m_file, m_lnum, m_col+colOffset);
    }

    public static boolean equals(final FileLocation l1, final FileLocation l2) {
        if (null==l1 && null==l2) {
            return true;
        }
        if ((null==l1 && null!=l2) || (null!=l1 && null==l2)) {
            return false;
        }
        if (!l1.m_file.equals(l2.m_file)) {
            return false;
        }
        return (l1.m_lnum==l2.m_lnum && l1.m_col==l2.m_col);
    }
    
    public static String toString(final File file, final int lnum, final int col) {
        return file.getFilename() + ":" + lnum + ":" + col;
    }

    public File getFile() {
        return m_file;
    }

    public int getLineNum() {
        return m_lnum;
    }
    
    public int getColNum() {
        return m_col;
    }
        
    @Override
    public String toString() {
        return toString(m_file, m_lnum, m_col);
    }

    public int[] getLineColNum() {
        return new int[]{getLineNum(), getColNum()};
    }
    
    private final File m_file;
    private final int m_lnum, m_col;
}
