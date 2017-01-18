/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gblib;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author kpfalzer
 */
public class OptionsTest {

    final Map<String, Object> m_config = new HashMap<>();

    @Test
    public void testCreate() {
        Options opts = Options.create().add("-s|--short", "my description",
                (t) -> {
                    m_config.put("s", t);
                })
                .add("-x|--long value", "another description",
                        (t) -> {
                            m_config.put("x", t);
                        });
        Queue<String> argv = opts.process(new String[]{"-s", "-x", "xvalue"});
        String usage = opts.getUsage();
        System.out.print(usage);
    }

    @Test
    public void testAdd() {
    }

    @Test
    public void testProcess() {
    }

    @Test
    public void testGetUsage() {
    }

}
