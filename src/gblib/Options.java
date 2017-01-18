/*
 * The MIT License
 *
 * Copyright 2017 gburdell.
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Process command line options.
 * @author kpfalzer
 */
public class Options {
    private Options() {        
    }
    
    public static Options create() {
        Options opts = new Options();
        return opts;
    }
   
    public Options add(final String opt, final String description, final OnOption onOpt) {
        Matcher matcher = ADD_REX.matcher(opt.trim());
        if (false == matcher.matches()) {
            throw new InvalidOptSpec(opt);
        }
        String shortOpt = matcher.group(1);
        String longOpt = matcher.group(2);
        String argName = matcher.group(3);
        for (String opti : new String[]{shortOpt, longOpt}) {
            if (opti != null) {
                m_byOption.put(opti, new Pair(argName, onOpt));
            }
        }
        StringBuilder usage = new StringBuilder(shortOpt);
        if (null != longOpt) {
            usage.append('|').append(longOpt);
        }
        if (null != argName) {
            usage.append(' ').append(argName);
        }
        m_optDescriptions.add(new Pair(usage, description));
        return this;
    }
    
    /**
     * Process options and arguments.
     * @param argv options and arguments.
     * @return index of remaining non-option.
     */
    public int process(String argv[]) {
        int i = 0;
        while (i < argv.length) {
            String opt = argv[i];
            if ('-' == opt.charAt(0)) {
                Pair<String, OnOption> onOpt = m_byOption.get(opt);
                if (null != onOpt) {
                    i += 1;
                    if (null != onOpt.v1) {
                        if (i < argv.length) {
                            onOpt.v2.consume(argv[i++]);
                        } else {
                            throw new Usage(opt + ": missing argument '" + onOpt.v1 + "'");
                        }
                    } else {
                        onOpt.v2.consume(null);
                    }
                } else {
                    throw new Usage(opt + ": invalid option");
                }
            } else {
                break;
            }
        }
        return i;
    }
    
    @FunctionalInterface
    public interface OnOption {
        public void consume(String value);
    }
    
    public static class Usage extends RuntimeException {
        private Usage(final String msg) {
            super(msg);
        }
    }
    
    public static class InvalidOptSpec extends RuntimeException {
        private InvalidOptSpec(final String opt) {
            super("Invalid option: " + opt);
        }
    }
    
    /**
     * Map of pair: 1) arg name (or null); 2) consumer, by soft/long option.
     */
    private final Map<String, Pair<String, OnOption>> m_byOption = new HashMap<>();
    private final List<Pair<String, String>> m_optDescriptions = new LinkedList<>();
    
    private static final Pattern ADD_REX = Pattern.compile("(\\-[^\\|$\\s]+)(\\|\\-\\-[^$\\s]+)?(\\s+([\\S]+))?");
}
