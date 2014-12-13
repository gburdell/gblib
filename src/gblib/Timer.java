/*
 *  The MIT License
 * 
 *  Copyright 2011 Karl W. Pfalzer.
 *  Copyright 2014- George P. Burdell
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package gblib;

/**
 * A timer.
 * @author gburdell
 */
public class Timer {
    public Timer() {
        restart();
    }
    public final void restart() {
        m_start = System.currentTimeMillis();
        m_stop = -1;
    }
    public long stop() {
        return (m_stop = System.currentTimeMillis());
    }
    public long elapsed() {
        return System.currentTimeMillis() - m_start;
    }
    @Override
    public String toString() {
        long elapsed = (0 > m_stop) ? elapsed() : (m_stop - m_start);
        return toString(elapsed);
    }
    public static String toString(long durInMillis) {
        long secs = durInMillis / 1000;
        if (0 >= secs) {
            secs = 1;
        }
        long mins = secs / 60;
        secs = (secs - (mins * 60));
        return String.format("%d:%02d (MM:SS)", mins, secs);        
    }
    private long m_start;
    private long m_stop = -1;
}
