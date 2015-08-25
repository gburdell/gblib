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

import static gblib.Util.invariant;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Read characters from file for line-oriented processing.
 *
 * @author gburdell
 */
public class FileCharReader implements AutoCloseable {

    public static final int EOF = -1;
    public static final char NL = '\n';

    /**
     * Create file reader.
     *
     * @param fname file name for reader.
     * @throws FileNotFoundException
     */
    public FileCharReader(final String fname) throws FileNotFoundException, IOException {
        final boolean isGzip = fname.endsWith(".gz");
        m_file = new File(fname);
        final long fsize = (!isGzip && (m_file.length() < stFileBufSize)) ? m_file.length() : stFileBufSize;
        if (isGzip) {
            m_ifs = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fname))));
        } else {
            m_ifs = new BufferedReader(new FileReader(m_file), Util.longToInt(fsize));
        }
        nextLine();
    }

    /**
     * Lookahead within current line.
     *
     * @param n lookahead distance from current position.
     * @return lookahead character, NL, or EOF. line.
     */
    public int la(int n) {
        assert 0 <= n;
        int c = EOF;
        if (!isEOF()) {
            int pos = m_pos + n;
            invariant(pos < length());
            c = m_line.charAt(pos);
        }
        return c;
    }

    public int la() {
        return la(0);
    }

    /**
     * Get current character and advance position.
     *
     * @return
     */
    public int next() {
        final int c = la(0);
        if (!isEOF()) {
            if (++m_pos >= length()) {
                nextLine();
            }
        }
        return c;
    }

    /**
     * Unconditionally accept n chars from current position. Can cross into next
     * line(s).
     *
     * @param n number of chars to accept.
     */
    public void accept(int n) {
        int m = n + m_pos;
        if (m < length()) {
            m_pos = m;
        } else {
            n = m - (length() - 1);
            m_pos = length() - 1;
            for (int i = 0; i < n; i++) {
                next();
            }
        }
    }

    public boolean acceptOnMatch(final char c) {
        boolean match = (c == (char) la());
        if (match) {
            next();
        }
        return match;
    }

    /**
     * Get substring or n chars starting at current position.
     *
     * @param n substring length.
     * @return substring of length n starting at current position. If n offset
     * larger than current line, only substring to end of current line is
     * returned; i.e., does not cross into next line.
     */
    public String substring(final int n) {
        int end = m_pos + n;
        if (end > length()) {
            end = length();
        }
        invariant(end > m_pos);
        return m_line.substring(m_pos, end);
    }

    /**
     * Match substring with current position.
     *
     * @param to matches substring.
     * @return true on matches and also accept: advance current position past
     * matches; else false and do not advance.
     */
    public boolean acceptOnMatch(final String to) {
        final boolean match = to.equals(substring(to.length()));
        if (match) {
            accept(to.length());
        }
        return match;
    }

    /**
     * Set remainder of line starting at current position.
     *
     * @return remainder of line.
     */
    public String setRemainder() {
        return m_remainder = substring(length());
    }

    /**
     * Replace substring in m_buf[span[0],span[1]) with repl.
     *
     * @param span position in buffer to replace.
     * @param repl replacement string.
     */
    public void replace(int[] span, final String repl) {
        // Only replace where we are (or after); not before.
        span[0] += m_pos;
        span[1] += m_pos;
        invariant(span[1] <= m_line.length());
        m_line.replace(span[0], span[1], repl);
    }

    /**
     * Get length of buffer.
     *
     * @return length of buffer.
     */
    private int length() {
        return m_line.length();
    }

    private boolean nextLine() {
        if (!m_eof) {
            try {
                String line = m_ifs.readLine();
                m_lnum++;
                m_pos = 0;
                if (null != line) {
                    m_line.setLength(0);
                    m_line.append(line).append(NL);
                } else {
                    m_ifs.close();
                    m_eof = true;
                }
            } catch (IOException ex) {
                Util.abnormalExit(ex);
            }
        }
        return !isEOF();
    }

    public boolean isEOF() {
        return m_eof;
    }

    public boolean isEOL() {
        return (la() == NL);
    }

    public FileLocation getFileLocation() {
        return new FileLocation(getFile(), getLineNum(), getColNum());
    }

    public String getLocation() {
        return FileLocation.toString(getFile(), getLineNum(), getColNum());
    }

    public int[] getLineColNum() {
        return new int[]{getLineNum(), getColNum()};
    }

    public int getLineNum() {
        return m_lnum;
    }

    public int getColNum() {
        return m_pos + 1;
    }

    public File getFile() {
        return m_file;
    }

    @Override
    public void close() throws IOException {
        m_ifs.close();
    }

    public static String stBlockComment[] = new String[]{"/*", "*/"};

    /**
     * Process block comment after accepting leading "/*".
     *
     * @param keep true to return block comment.
     * @return block comment on keep, else null.
     * @throws ParseError
     */
    public String blockComment(final boolean keep) throws ParseError {
        StringBuilder sb = new StringBuilder(stBlockComment[0]);
        while (!isEOF()) {
            if (acceptOnMatch(stBlockComment[1])) {
                sb.append(stBlockComment[1]);
                break;  //while
            } else if (acceptOnMatch(stBlockComment[0])) {
                throw new ParseError(ErrorType.eNestedBlockComment);
            }
            char c = (char) next();
            if (keep) {
                sb.append(c);
            }
        }
        if (isEOF()) {
            throw new ParseError(ErrorType.eUnexpectedEOF);
        }
        return keep ? sb.toString() : null;
    }

    public void blockComment() throws ParseError {
        blockComment(false);
    }

    public static String stLineComment = "//";

    public String lineComment(final boolean keep) {
        StringBuilder sb = new StringBuilder(stLineComment);
        final String rem = setRemainder();
        if (keep) {
            sb.append(rem);
        }
        accept(rem.length());
        return keep ? sb.toString() : null;
    }

    public void lineComment() {
        lineComment(false);
    }

    /**
     * Match line contents against pattern. If >0 group(s) matched, save them.
     * If all groups matched, then return true.
     *
     * @param line line to matches against pattern.
     * @param patt pattern to match.
     * @param cnt number of groups expected.
     * @return true if all groups matched; false if 0.
     * @throws ParseError if less than cnt group(s) matched.
     */
    public boolean matchSaveAccept(final String line, final Pattern patt,
            final Integer cnt[]) throws ParseError {
        setMatcher(patt, line);
        if (!m_matcher.lookingAt()) {
            return false;
        }
        int groupCnt = m_matcher.groupCount();
        final FileLocation start = getFileLocation();
        FileLocation loc, prevLoc = null;
        int n;
        int nullCnt = 0;
        for (int i = 1; i <= groupCnt; i++) {
            n = m_matcher.start(i);
            if (0 <= n) {    //skip if null
                loc = start.offset(n);
                //dont add same
                if ((null == prevLoc) || (loc.getColNum() != prevLoc.getColNum())) {
                    getMatched().add(new Util.Pair<>(loc, m_matcher.group(i)));
                    prevLoc = loc;
                }
            } else {
                nullCnt++;
            }
        }
        groupCnt -= nullCnt;
        if (0 > Util.linearSearch(cnt, groupCnt)) {
            throw new ParseError(ErrorType.eGroupCnt);
        }
        acceptGroup(0);
        return true;
    }

    /**
     * Accept to cover matcher group.
     *
     * @param group
     */
    private void acceptGroup(int group) {
        int n = m_matcher.end(group);
        if (0 <= n) {
            if (0 < n) {
                accept(n);
            }
        }
    }

    /**
     * Match line remainder contents against pattern. If >0 group(s) matched,
     * save them. If all groups matched, then return true.
     *
     * @param patt pattern to match.
     * @param cnt number of groups expected.
     * @return true if all groups matched; false if 0.
     * @throws ParseError if less than cnt group(s) matched.
     */
    public boolean matchSaveAccept(final Pattern patt, final int cnt) throws ParseError {
        return matchSaveAccept(m_remainder, patt, new Integer[]{cnt});
    }

    public boolean matchSaveAccept(final Pattern patt, final int... cnt) throws ParseError {
        return matchSaveAccept(m_remainder, patt, 
                Arrays.stream(cnt).boxed().toArray( Integer[]::new ));
    }

    public int getMatchedGroupCnt() {
        return m_matcher.groupCount();
    }

    /**
     * Match line remainder contents against pattern, starting at beginning of
     * line.
     *
     * @param patt pattern to match.
     * @return true on match.
     */
    public boolean matches(final Pattern patt) {
        return matches(patt, m_remainder);
    }

    /**
     * Match string contents against pattern, starting at beginning of line.
     *
     * @param patt pattern to match.
     * @param str string to match.
     * @return true on match.
     */
    public boolean matches(final Pattern patt, final String str) {
        setMatcher(patt, str);
        //match begin of pattern to begin of line (not require entire region).
        return m_matcher.lookingAt();
    }

    private void setMatcher(final Pattern patt, final String str) {
        m_matcher = patt.matcher(str);
        getMatched().clear();
    }

    public void saveMatch(final int group, final boolean save) {
        if (save) {
            getMatched().add(new Util.Pair<>(getFileLocation(), m_matcher.group(group)));
        }
    }

    public void saveMatch(final int group) {
        saveMatch(group, true);
    }

    /**
     * Match pattern to remainder and accept groups onto stack.
     *
     * @param patt pattern to matches against remainder.
     * @param save set true to save matching groups.
     * @return true on matches.
     */
    public boolean matchSaveAccept(final Pattern patt, final boolean save) {
        boolean match = matches(patt);
        if (match) {
            final int n = getMatchedGroupCnt();
            for (int i = 1; i <= n; i++) {
                saveMatch(i, save);
            }
            acceptGroup(0);
        }
        return match;
    }

    public boolean matchSaveAccept(final Pattern patt) {
        return matchSaveAccept(patt, true);
    }

    public boolean matchAccept(final Pattern patt) {
        return matchSaveAccept(patt, false);
    }

    public boolean matchSaveAccept(final boolean save, final Pattern... patts) {
        for (final Pattern patt : patts) {
            if (matchSaveAccept(patt, save)) {
                return true;
            }
        }
        return false;
    }

    public boolean matchSaveAccept(final Pattern... patts) {
        return matchSaveAccept(true, patts);
    }

    public boolean matchAccept(final Pattern... patts) {
        return matchSaveAccept(false, patts);
    }

    public int[] getStartMark() {
        return getLineColNum();
    }

    public int[] getSpan(final int grp) {
        return new int[]{m_matcher.start(grp), m_matcher.end(grp)};
    }

    public Queue<Util.Pair<FileLocation, String>> getMatched() {
        return m_matched;
    }

    public String saveGet(final int grp) {
        assert (grp < getMatchedGroupCnt());
        final String s = getMatched(grp);
        saveMatch(grp);
        return s;
    }

    public String getMatched(final int grp) {
        return m_matcher.group(grp);
    }

    public static enum ErrorType {

        eNestedBlockComment, eUnexpectedEOF, eGroupCnt
    }

    public class ParseError extends Exception {

        public ParseError(final ErrorType type) {
            m_doing = getMatched(0);
            m_type = type;
            acceptGroup(0); //skip over what we liked, before grab location
            m_loc = new FileLocation(getFile(), getLineNum(), getColNum());
            getMatched().clear();
        }

        public ErrorType getType() {
            return m_type;
        }

        public FileLocation getLocation() {
            return m_loc;
        }

        public String getDoing() {
            return m_doing;
        }

        private final String m_doing;
        private final ErrorType m_type;
        private final FileLocation m_loc;
    }

    private String m_remainder = null;
    private int m_lnum = 0;
    private final File m_file;
    private final BufferedReader m_ifs;
    private int m_pos = 0;
    private final StringBuilder m_line = new StringBuilder(stLineBufSize);
    private boolean m_eof = false;
    private Matcher m_matcher;
    private final Queue<Util.Pair<FileLocation, String>> m_matched = new LinkedList<>();

    private static final int stFileBufSize = 1 << 20;
    private static final int stLineBufSize = 1024;
}
