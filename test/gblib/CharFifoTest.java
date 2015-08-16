package gblib;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gburdell
 */
public class CharFifoTest {

    CharFifo dut;

    /**
     * Test of push method, of class CharFifo.
     */
    @Test
    public void testPush() {
        dut = new CharFifo(4);
        dut.push('a').push('b');
        assertEquals(2, dut.size());
        assertEquals('a', dut.pop());
        assertEquals(1, dut.size());
        assertEquals('b', dut.pop());
        assertTrue(dut.isEmpty());
        try {
            dut.pop();
            assertFalse(false);
        } catch (RuntimeException ex) {
            assertTrue(true);
        }
        try {
            dut.peek();
            assertFalse(true);
        } catch (RuntimeException ex) {
            assertTrue(true);
        }
        dut.push('c').push('d').push('e');
        assertEquals('c', dut.peek());
        assertEquals('d', dut.peek(1));
        assertEquals('e', dut.peek(2));
        assertEquals(3, dut.size());
        try {
            dut.peek(dut.size());
            assertFalse(true);
        } catch (RuntimeException ex) {
            assertTrue(true);
        }
        assertEquals('e', dut.pop(3));
        dut.push('f').push('g');
        assertEquals(2, dut.size());
        assertEquals('g', dut.peek(1));
        assertEquals('f', dut.pop());
        assertEquals('h', dut.push('h').peek(1));
        assertEquals('h', dut.pop(2));
        assertTrue(dut.isEmpty());
    }

}
