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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A singleton message manager.
 *
 * @author karl
 */
public class MessageMgr {

    private static int stMessageLevel = 1;

    /**
     * Set new message level.
     *
     * @param lvl new message level.
     * @return previous message level.
     */
    public static int setMessageLevel(int lvl) {
        int was = stMessageLevel;
        stMessageLevel = lvl;
        return was;
    }

    /**
     * Get current message level.
     *
     * @return current message level.
     */
    public static int getMessageLevel() {
        return stMessageLevel;
    }

    /**
     * Encapsulate message.
     */
    public static class Message {

        /**
         * Create message.
         *
         * @param severity	one of 'I', 'W', 'E' (for Info, Warning, Error,
         * respectively).
         * @param code	a valid message code used to find message format.
         * @param args	arguments to pass to format.
         */
        public Message(char severity, String code, Object... args) {
            m_type = getTheOne().getMessenger().factory(severity);
            m_message = format(m_type, code, args);
        }

        public void print() {
            MessageMgr.print(this);
        }

        /**
         * Get the message type.
         *
         * @return message type.
         */
        public IMessenger.EType getType() {
            return m_type;
        }

        /**
         * Get formatted message.
         *
         * @return formatted message.
         */
        public String getMessage() {
            return m_message;
        }

        @Override
        public String toString() {
            return getMessage();
        }

        /**
         * Message type
         */
        private final IMessenger.EType m_type;
        /**
         * Formatted message.
         */
        private final String m_message;
    }

    /**
     * Format message.
     */
    private static String format(IMessenger.EType type, String code, final Object... args) {
        String fmt = getTheOne().getFormat(code);
        ArrayList<Object> nargs = new ArrayList<>(args.length);
        if (fmt.startsWith("%1$t") || fmt.startsWith("%t")) {
            //e.g.: String.format("The date: %1$tY-%1$tm-%1$td", date);
            nargs.add(Calendar.getInstance().getTime());
        }
        for (Object arg : args) {
            if (arg instanceof String) {
                nargs.add(gblib.Util.escape((String) arg));
            } else {
                nargs.add(arg);
            }
        }
        StringBuilder buf = new StringBuilder(type.getPfx());
        buf.append(": ");
        buf.append(String.format(fmt, nargs.toArray()));
        buf.append(String.format("  (%s)", code));
        return buf.toString();
    }

    /**
     * Conditionally display message.
     *
     * @param msgLvl minimum message level required to display message. A higher
     * number diminishes liklihood of message being displayed.
     * @param severity severity code.
     * @param code message code.
     * @param args arguments.
     */
    public static synchronized void message(int msgLvl, char severity, String code, Object... args) {
        if (msgLvl <= stMessageLevel) {
            Message msg = new Message(severity, code, args);
            print(msg);
        }
    }

    /**
     * Conditionally display message.
     */
    public static synchronized void message(boolean doMsg, char severity, String code, Object... args) {
        if (doMsg) {
            Message msg = new Message(severity, code, args);
            print(msg);
        }
    }

    public static synchronized void message(char severity, String code, Object... args) {
        Message msg = new Message(severity, code, args);
        print(msg);
    }

    public static void message(int msgLvl, String code, Object... args) {
        message(msgLvl, getSeverity(code), code, args);
    }

    public static void message(boolean doMsg, String code, Object... args) {
        message(doMsg, getSeverity(code), code, args);
    }

    public static void message(String code, Object... args) {
        message(getSeverity(code), code, args);
    }

    private static char getSeverity(final String code) {
        Character svr = getTheOne().m_severityByMsgCode.get(code);
        Util.invariant(null != svr);
        return svr;
    }

    public static synchronized void print(Message msg) {
        getTheOne().getMessenger().message(msg);
        getTheOne().m_msgCnts[msg.getType().getIx()]++;
    }

    public static int getErrorCnt() {
        MessageMgr t = getTheOne();
        return (null == t) ? 0 : t.m_msgCnts[2];
    }

    private static MessageMgr getTheOne() {
        return stTheOne;
    }

    /**
     * Creates a new instance of MessageMgr
     */
    private MessageMgr() {
    }

    /**
     * Add new messages.
     *
     * @param fname new messages.
     */
    public static void addMessages(String fname) {
        addMessages(new File(fname));
    }

    public static void addMessages(File f) {
        MessageMgr mgr = getTheOne();
        mgr.init(f);
    }

    /**
     * Add messages.  A message entry separates code from format using '|'.
     * @param msgs array of '|' separated code and format.
     */
    public static void addMessages(final String msgs[]) {
        for (String m : msgs) {
            int i = m.indexOf('|');
            String code = m.substring(0, i).trim();
            String fmt = m.substring(i + 1).trim();
            addMessage(code, fmt);
        }
    }
    
    public static void addMessage(final String code, final String msg) {
        getTheOne().m_msgs.put(code, msg);
    }

    public static MessageMgr addMessage(final char svr, final String code, final String msg) {
        getTheOne().m_msgs.put(code, msg);
        setSeverity(svr, new String[]{code});
        return getTheOne();
    }

    /**
     * Set message severity.
     *
     * @param svr one of [IWE]
     * @param messageCodes message codes to set severity.
     */
    public static void setSeverity(char svr, final String messageCodes[]) {
        Util.invariant(svr == 'I' || svr == 'W' || svr == 'E');
        for (final String code : messageCodes) {
            getTheOne().m_severityByMsgCode.put(code, svr);
        }
    }

    private final Map<String, Character> m_severityByMsgCode = new HashMap<>();

    private void init(File f) {
        try {
            f = new File(f.getCanonicalPath());
            LineNumberReader rdr = new LineNumberReader(new FileReader(f));
            String line;
            int mark;
            String msgCode;
            String msg;
            StringTokenizer toks;
            while (null != (line = rdr.readLine())) {
                if (0 <= (mark = line.indexOf("//"))) {
                    line = line.substring(0, mark);
                }
                if (1 > line.length()) {
                    continue;
                }
                line = line.trim();
                mark = line.indexOf(' ');
                msgCode = line.substring(0, mark);
                msg = line.substring(mark).trim().replace("\\n", "\n");
                m_msgs.put(msgCode, msg);
            }
        } catch (IOException ex) {
            Util.abnormalExit(ex);
        }
    }

    public static abstract class IMessenger {

        public static enum EType {

            eInfo(System.out, "Info ", 0),
            eWarn(System.out, "Warn ", 1),
            eError(System.err, "Error", 2);

            /**
             * Create An Enum based on code.
             */
            public static EType factory(char code) {
                return EType.values()[m_charMapToEnum.indexOf(code)];
            }

            public PrintStream getOstrm() {
                return m_os;
            }

            public String getPfx() {
                return m_pfx;
            }

            public int getIx() {
                return m_ix;
            }

            EType(PrintStream os, String pfx, int ix) {
                m_os = os;
                m_pfx = pfx;
                m_ix = ix;
            }
            /**
             * PrintStream for type/severity of message.
             *
             * @see java.io.PrintStream
             */
            private final PrintStream m_os;
            /**
             * Message prefix based on severity of message.
             */
            private final String m_pfx;
            /**
             * Index of code.
             */
            private final int m_ix;
            /**
             * The following in order as EType ordinals.
             *
             * @see java.lang.Enum
             */
            private final static String m_charMapToEnum = "IWE";
        }

        public EType factory(char code) {
            return EType.factory(code);
        }

        public abstract void message(Message msg);
    }

    public static class DefaultMessenger extends IMessenger {

        @Override
        public void message(Message msg) {
            PrintStream os = msg.getType().getOstrm();
            os.println(msg.getMessage());
            os.flush();
        }
    }

    public IMessenger getMessenger() {
        return m_messenger;
    }

    public String getFormat(String code) {
        if (!m_msgs.containsKey(code)) {
            throw new RuntimeException("No message detail/format for '" + code + "'");
        }
        return m_msgs.get(code);
    }

    private static final MessageMgr stTheOne = new MessageMgr();
    private final Map<String, String> m_msgs = new HashMap<>();
    private final IMessenger m_messenger = new DefaultMessenger();
    private final int m_msgCnts[] = new int[]{0, 0, 0};
}
