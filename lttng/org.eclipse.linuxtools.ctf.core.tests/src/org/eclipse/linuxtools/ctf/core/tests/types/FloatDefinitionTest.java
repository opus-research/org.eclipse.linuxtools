package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.types.FloatDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.FloatDefinition;
import org.eclipse.linuxtools.internal.ctf.core.event.io.BitBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;



/**
 * The class <code>IntegerDefinitionTest</code> contains tests for the class
 * <code>{@link IntegerDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class FloatDefinitionTest {
    private FloatDefinition fixture;
    private FloatDefinition singleFixture;
    private FloatDefinition doubleFixture; //all the way.
    private FloatDeclaration parent;
    private static final String fieldName = "float"; //$NON-NLS-1$
    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(IntegerDefinitionTest.class);
    }

    /**
     * Perform pre-test initialization. We know the structDef won't be null (or
     * else the tests will fail), so we can safely suppress the warning.
     *
     * @throws CTFReaderException
     */
    @Before
    public void setUp(){
        testFloat248();
        testFloat5311();
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    @Test
    public void testFloat248() {
        parent = new FloatDeclaration(8, 24, ByteOrder.nativeOrder(), 0);
        singleFixture = parent.createDefinition(null, fieldName);
        assertNotNull(singleFixture);
    }



    @Test
    public void testFloat5311() {
        parent = new FloatDeclaration(11, 53, ByteOrder.nativeOrder(), 0);
        doubleFixture = parent.createDefinition(null, fieldName);
        assertNotNull(doubleFixture);
    }

    @Test
    public void testFloat32Bit(){
        for(int i = 1; i < 31 ; i++)
        {
            parent = new FloatDeclaration(i, 32-i, ByteOrder.nativeOrder(), 0);
            fixture = parent.createDefinition(null, fieldName);
            assertNotNull(fixture);
            fixture.setValue(2.0);
            assertTrue(fixture.toString().contains("2")); //$NON-NLS-1$
        }
    }

    @Test
    public void testFloat64Bit(){
        for(int i = 1; i < 63 ; i++)
        {
            parent = new FloatDeclaration(i, 64-i, ByteOrder.nativeOrder(), 0);
            fixture = parent.createDefinition(null, fieldName);
            assertNotNull(fixture);
            BitBuffer input = new BitBuffer(java.nio.ByteBuffer.allocateDirect(128));
            fixture.read(input);
            fixture.setValue(2.0);
            assertTrue(fixture.toString().contains("2")); //$NON-NLS-1$
        }
    }

    @Test
    public void testFloat48Bit(){
        parent = new FloatDeclaration(12, 32, ByteOrder.nativeOrder(), 0);
        fixture = parent.createDefinition(null, fieldName);
        assertNotNull(fixture);
        BitBuffer input = new BitBuffer(java.nio.ByteBuffer.allocateDirect(128));
        fixture.read(input);

        assertEquals(Double.NaN ,fixture.getValue(),0.1);
    }
    /**
     * Run the IntegerDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        singleFixture.setValue(2.0);
        FloatDeclaration result = singleFixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the long getValue() method test.
     */
    @Test
    public void testGetValue() {
        singleFixture.setValue(2.0);
        double result = singleFixture.getValue();
        assertEquals(2.0, result,0.1);
    }

    /**
     * Run the void read(BitBuffer) method test.
     */
    @Test
    public void testRead() {
        singleFixture.setValue(2.0);
        BitBuffer input = new BitBuffer(java.nio.ByteBuffer.allocateDirect(128));
        singleFixture.read(input);

    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        singleFixture.setValue(222.22);
        String result = singleFixture.toString();
        assertNotNull(result);
    }
}
