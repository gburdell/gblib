/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gblib;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Scan a jarfile. Create list of class/interface defined therein. NOTE: inner
 * classes have embedded $, as in:
 * <ul>
 * <li>laol/rt/LaolArray$1.class</li>
 * <li>laol/rt/LaolArray$IndexException.class</li>
 * <li>laol/rt/LaolArray$Slice$Iterator.class</li>
 * </ul>
 *
 * @author kpfalzer
 */
public class JarFile {

    /**
     * Parse .jar to extract class (names) defined therein. Thanx to stack
     * overflow for this code. We do not load any class at this point.
     *
     * @param fname
     * @throws FileNotFoundException
     * @throws IOException
     */
    public JarFile(String fname) throws FileNotFoundException, IOException {
        m_fname = fname;
        File jarf = new File(fname);
        if (CLZ_BY_JAR.containsKey(jarf)) {
            m_clsNames = CLZ_BY_JAR.get(jarf);
        } else {
            try (ZipInputStream zip = new ZipInputStream(new FileInputStream(m_fname));) {
                for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                        String className = entry.getName().replace('/', '.');
                        m_clsNames.add(className.substring(0, className.length() - ".class".length()));
                    }
                }
            }
            CLZ_BY_JAR.put(jarf, m_clsNames);
        }
    }

    /**
     * Get list of class/interface names found in package.
     *
     * @param hierName name (syntax) as would appear in "import ..." statement.
     * @return list of fully qualified class names.
     */
    public List<String> getClassNamesInPackage(String hierName) {
        return getClassNamesInPackage(hierName, m_clsNames);
    }

    public static List<String> getClassNamesInPackage(String hierName, List<String> clsNames) {
        // a hier class name will use '$' (in clsNames), but hierName does not.
        Stream<String> dottedNames = clsNames.stream().map(e -> e.replace('$', '.'));
        if (hierName.endsWith(".*")) {
            final String startsWith = hierName.substring(0, hierName.length() - 1); //drop '*'
            return dottedNames
                    .filter(e -> (e.startsWith(startsWith)
                    && !e.substring(startsWith.length()).contains(".")))
                    .collect(Collectors.toList());
        } else {
            return dottedNames
                    .filter(hierName::equals)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Get Field for static names found in package. Static elements (of class)
     * are:
     * <ul>
     * <li>enum value</li>
     * <li>method</li>
     * <li>member</li>
     * </ul>
     *
     * @param hierName name (syntax) as would appear in "import static ..."
     * statement.
     * @return map of Field by fully qualified name.
     * @throws ClassNotFoundException
     */
    public static Map<String, Field> getStaticNamesInPackage(String hierName) throws ClassNotFoundException {
        Map<String, Field> fldByName = new HashMap<>();
        final String clsName = hierName.substring(0, hierName.lastIndexOf('.'));
        final String memName = hierName.endsWith(".*") ? null : hierName.substring(hierName.lastIndexOf('.') + 1);
        Class cls = getClass(clsName);
        for (Field fld : cls.getFields()) {
            if (Modifier.isStatic(fld.getModifiers())) {
                if (Objects.isNull(memName) || fld.getName().equals(memName)) {
                    String fqn = clsName + "." + fld.getName();
                    fldByName.put(fqn, fld);
                }
            }
        }
        return fldByName;
    }

    /**
     * Get class/interface names (in order they appeared in jar file).
     *
     * @return class/interface names (without .class suffix).
     */
    public List<String> getClassNames() {
        return Collections.unmodifiableList(m_clsNames);
    }

    public static List<String> getClassNames(String fname) throws IOException {
        return new JarFile(fname).getClassNames();
    }

    public String getJarFilename() {
        return m_fname;
    }

    /**
     * Get Class (descriptor) for (fully-qualified) class name. 
     * TODO: the
     * current implementation will fail if fqn is an inner class referenced
     * using import syntax: 'package.b.inner', since actual is
     * 'package.b$inner'.
     *
     * @param fqn fully qualified name.
     * @return Class object.
     * @throws ClassNotFoundException
     */
    public static Class getClass(String fqn) throws ClassNotFoundException {
        Class cls = CLS_BY_NAME.get(fqn);
        if (Objects.isNull(cls)) {
            cls = Class.forName(fqn);
            CLS_BY_NAME.put(fqn, cls);
        }
        return cls;
    }

    /**
     * Get map of Class by class name.
     *
     * @param clsNames names of classes to get.
     * @return class by name.
     * @throws ClassNotFoundException
     */
    public static Map<String, Class> getClassByName(Collection<String> clsNames) throws ClassNotFoundException {
        Map<String, Class> clsByName = new HashMap<>();
        for (String clsName : clsNames) {
            clsByName.put(clsName, getClass(clsName));
        }
        return clsByName;
    }

    /**
     * Get map of Class by fully qualified name for import specifications. There
     * is no detection here if some imports are not found.
     *
     * @param imports as specified in "require ..." statements.
     * @param jars .jar files.
     * @return map of Class by fully qualified name.
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public static Map<String, Class> getImports(Collection<String> imports, Collection<String> jars) throws IOException, ClassNotFoundException {
        Map<String, Class> clsByName = new HashMap<>();
        for (String imp : imports) {
            for (String jarf : jars) {
                List<String> clsNms = getClassNames(jarf);
                List<String> inJar = getClassNamesInPackage(imp, clsNms);
                if (!inJar.isEmpty()) {
                    clsByName.putAll(getClassByName(inJar));
                    break; //for
                }
            }
        }
        return clsByName;
    }

    /**
     * Memoize Class by fqn.
     */
    private final static Map<String, Class> CLS_BY_NAME = new HashMap<>();
    /**
     * Memoize .jar already processed.
     */
    private final static Map<File, List<String>> CLZ_BY_JAR = new HashMap<>();

    private final String m_fname;
    private List<String> m_clsNames = new ArrayList<>();
}
