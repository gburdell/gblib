package gblib;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gburdell
 */
public class ConfigTest {
    
    @Test
    public void testCreate() {
        final Config cfg = Config.create()
                .add("k1 v1")
                .add("k2 *I1234")
                .add("k3 *Btrue")
                .add(new String[]{"k4 *D-123.456e09", "k5 foobar"});
        int sz = cfg.size();
    }
    
}
