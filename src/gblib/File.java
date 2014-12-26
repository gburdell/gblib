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

import java.io.IOException;
import static java.nio.file.Files.isSameFile;
import java.nio.file.Path;


/**
 * Extend java.io.File except redefine equals to check if files point
 * to same physical location (not just path name),
 * @author gburdell
 */
public class File extends java.io.File {
    public static String getCanonicalPath(String fn) {
        return (new File(fn)).getCanonicalPath();
    }

    @Override
    public String getCanonicalPath() {
        try {
            return super.getCanonicalPath();
        } catch (IOException ex) {
            return getFilename();
        }
    }
    
    
    public File(String pathname) {
        super(pathname);
    }
    
    public File(String dirname, String childname) {
        super(dirname, childname);
    }

    public File(Path dir, String fname) {
        super(dir.toFile(), fname);
    }

    public File(java.io.File dir, String fname) {
        super(dir, fname);
    }

    public String getFilename() {
        return super.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        try {
            assert (obj instanceof java.io.File);
            final Path asPath = Util.<java.io.File>downCast(obj).toPath();
            return isSameFile(super.toPath(), asPath);
        } catch (IOException ex) {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        try {
            return super.getCanonicalFile().hashCode();
        } catch (IOException ex) {
            return 0;
        }
    }    
    
}
