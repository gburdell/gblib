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

import static gblib.Util.invariant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manage program configuration.
 * @author gburdell
 */
public class Config extends HashMap<String, Object> {
    private Config() {
    }
    
    public static Config create() {
        return new Config();
    }
    
    /**
     * Add config entry.
     * @param keyVal space separated key and value.
     * value has optional prefix: *I, *B, *D to interpret subsequent value (substring)
     * as Integer, Boolean and Double, respectively.
     * Without format, value treated as String.
     * @return configuration.
     */
    public Config add(final String keyVal) {
        Matcher matcher = KV_REX.matcher(keyVal.trim());
        invariant(matcher.matches());
        String key = matcher.group(1);
        String val = matcher.group(2);
        Object valObj = val;
        String valx = val.substring(2);
        if (val.startsWith("*I")) {
            valObj = Integer.parseInt(valx);
        } else if (val.startsWith("*B")) {
            valObj = Boolean.parseBoolean(valx);
        } else if (val.startsWith("*D")) {
            valObj = Double.parseDouble(valx);
        }
        super.put(key, valObj);
        return this;
    }
    
    public Config add(final Collection<String> keyVals) {
        keyVals.forEach(this::add);
        return this;
    }
    
    public Config add(final String keyVals[]) {
        return add(Arrays.asList(keyVals));
    }

    public boolean hasKey(final String key) {
        return super.containsKey(key);
    }
    
    private static final Pattern KV_REX = Pattern.compile("(\\S+)\\s+(.*)");
}
