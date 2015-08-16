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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author gburdell
 */
public class LineCharReader {

    public LineCharReader(final Reader in) {
        init(in);
    }

    public LineCharReader(final Reader in, final int sz, final int la) {
        init(in, sz, la);
    }

    protected LineCharReader() {
    }

    protected final void init(final Reader in, final int sz, final int la) {
        m_rdr = new BufferedReader(in, sz);
        m_fifo = new CharFifo(la);
    }

    protected final void init(final Reader in) {
        init(in, stSize, stLa);
    }

    public char la(final int n) throws IOException {
        assert n<=m_fifo.capacity()&& 0<=n;
        //read in more if needed
        char c;
        for (int cnt = n+1 - m_fifo.size(); 0 < cnt; cnt--) {
            c = read();
            m_fifo.push(c);
            if (EOF == c) {
                return c;
            }
        }
        return m_fifo.peek(n);
    }

    /**
     * Read next character, skipping over carriage return (\r).
     *
     * @return next character which is not a carriage return.
     * @throws IOException
     */
    private char read() throws IOException {
        int c;
        do {
            c = m_rdr.read();
        } while ('\r' == (char) c && (0 <= c));
        return (0 <= c) ? (char) c : EOF;
    }

    public int la() throws IOException {
        return la(0);
    }

    public char accept(final int n) throws IOException {
        if (n > m_fifo.size()) {
            la(n - m_fifo.size());
        }
        final char c = m_fifo.pop(n);
        if (EOL != c) {
            m_col += n;
        } else {
            m_lnum++;
            m_col = 1;
        }
        return c;
    }

    public int accept() throws IOException {
        return accept(1);
    }

    public boolean isEOF() throws IOException {
        return (EOF == la());
    }

    public int getLineNum() {
        return m_lnum;
    }

    public int getColNum() {
        return m_col;
    }

    public static final char EOF = (char)-1;
    public static final char EOL = '\n';

    private int m_lnum = 1;
    private int m_col = 1;
    // index of la(0)
    private CharFifo m_fifo;
    private BufferedReader m_rdr;
    private static final int stSize = 1 << 20;
    private static final int stLa = 16;

}
