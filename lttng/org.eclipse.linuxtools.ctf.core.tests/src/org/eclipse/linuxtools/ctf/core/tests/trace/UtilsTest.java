package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.eclipse.linuxtools.ctf.core.trace.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>UtilsTest</code> contains tests for the class
 * {@link Utils}.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class UtilsTest {

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(UtilsTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        // add additional set up code here
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the Utils() constructor test.
     */
    @Test
    public void testUtils() {
        Utils result = new Utils();
        assertNotNull(result);
    }

    /**
     * Run the UUID makeUUID(byte[]) method test.
     */
    @Test
    public void testMakeUUID() {
        int byteSize = 32;
        byte[] bytes = new byte[byteSize];
        for (int i = 0; i < byteSize; i++) {
            bytes[i] = (byte) (i);
        }

        UUID result = Utils.makeUUID(bytes);
        assertNotNull(result);
    }

    /**
     * Run the UUID makeUUID(byte[]) method test.
     */
    @Test
    public void testMakeUUID_2() {
        byte[] bytes = new byte[] { (byte) 1, (byte) 1, (byte) 0, (byte) 0,
                (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 1,
                (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };

        UUID result = Utils.makeUUID(bytes);

        assertNotNull(result);
        assertEquals(72339069014638592L, result.getLeastSignificantBits());
        assertEquals(72339069014638592L, result.getMostSignificantBits());
        assertEquals("01010000-0000-0000-0101-000000000000", result.toString()); //$NON-NLS-1$
        assertEquals(0, result.variant());
        assertEquals(0, result.version());
    }

    /**
     * Run the UUID makeUUID(byte[]) method test.
     */
    @Test
    public void testMakeUUID_3() {
        byte[] bytes = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };

        UUID result = Utils.makeUUID(bytes);

        assertNotNull(result);
        assertEquals(0L, result.getLeastSignificantBits());
        assertEquals(0L, result.getMostSignificantBits());
        assertEquals("00000000-0000-0000-0000-000000000000", result.toString()); //$NON-NLS-1$
        assertEquals(0, result.variant());
        assertEquals(0, result.version());
    }

    /**
     * Run the int unsignedCompare(long,long) method test.
     */
    @Test
    public void testUnsignedCompare() {
        long a = 1L;
        long b = 1L;

        int result = Utils.unsignedCompare(a, b);
        assertEquals(0, result);
    }
}