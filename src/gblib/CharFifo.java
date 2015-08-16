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
public class CharFifo {

    public CharFifo(final int sz) {
        N = sz;
        m_fifo = new char[N];
    }

    public CharFifo push(final char c) {
        if (isFull()) {
            throw new RuntimeException("push on full");
        }
        m_fifo[m_tail] = c;
        m_tail = (m_tail + 1) % N;
        m_cnt++;
        return this;
    }

    public char pop() {
        if (isEmpty()) {
            throw new RuntimeException("pop on empty");
        }
        final char c = m_fifo[m_head];
        m_head = (m_head + 1) % N;
        m_cnt--;
        return c;
    }

    public char pop(final int n) {
        if (n <= 0 || n > size()) {
            throw new RuntimeException("pop error");
        }
        final char c = peek(n-1);
        m_head = (m_head + n) % N;
        m_cnt -= n;
        return c;
    }

    public char peek(int n) {
        if (n < 0 || n >= size() || (n == 0 && isEmpty())) {
            throw new RuntimeException("peek error");
        }
        return m_fifo[(m_head + n) % N];
    }

    public char peek() {
        return peek(0);
    }

    public boolean isEmpty() {
        return (0 == size());
    }

    public boolean isFull() {
        return (size() >= N);
    }

    public int size() {
        return m_cnt;
    }

    public int capacity() {
        return N;
    }
    
    private int m_cnt = 0;
    private int m_head = 0, m_tail = 0;
    private final int N;
    private final char m_fifo[];
}
