/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gblib;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author kpfalzer
 */
public class JarFileTest {

    static final String JARFILE = "dist/gblib.jar";

    static JarFile getJarFile() {
        try {
            return new JarFile(JARFILE);
        } catch (IOException ex) {
            ex.printStackTrace(ERR);
        }
        return null;
    }

    static final JarFile JF = getJarFile();

    static final PrintStream ERR = System.err;
    
    @Test
    public void testGetClassNamesInPackage() {
        List<String> clsNames = JF.getClassNamesInPackage("gblib.MessageMgr");
        assertTrue(clsNames.size() == 1); //no inner classes returned
        System.out.printf("num class names=%s\n", clsNames.size());
        //assertTrue(JF.getClassNames().size() == JF.getClassNamesInPackage("gblib.*").size());
    }

    @Test
    public void testGetClassNames() {
        List<String> clsNames = JF.getClassNames();
        assertTrue(!clsNames.isEmpty());
        System.out.printf("num class names=%s\n", clsNames.size());
    }

    @Test
    public void testGetClass() {
        try {
            for (String fqn : JF.getClassNames()) {
                Class cls = JarFile.getClass(fqn);
                System.out.printf("%s: %s\n", fqn, cls.toGenericString());
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace(ERR);
        }
    }

    @Test
    public void testGetImports() {
        try {
            Map<String, Class> map = JarFile.getImports(
                    Arrays.asList("gblib.*", "gblib.*", "gblib.JarFile.CLS_BY_NAME"),
                    Arrays.asList(JARFILE)
            );
            System.out.printf("testGetImports: %d\n", map.size());
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace(ERR);
        }
    }

     @Test
    public void testGetStaticNamesInPackage() throws Exception {
        Map<String, Field> names = JarFile.getStaticNamesInPackage("gblib.JarFile.Bogus.VAL");
        assertTrue(! names.isEmpty());
    }

}
