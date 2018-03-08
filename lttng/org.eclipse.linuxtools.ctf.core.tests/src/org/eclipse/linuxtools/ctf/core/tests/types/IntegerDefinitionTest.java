package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
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
public class IntegerDefinitionTest {

    private IntegerDefinition fixture;
    String name = "testInt"; //$NON-NLS-1$
    String clockName = "clock"; //$NON-NLS-1$
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
     */
    @Before
    public void setUp() {

//        StructDefinition structDef = null;
//        boolean found = false;
        IntegerDeclaration id = new IntegerDeclaration( 1, true, 1, ByteOrder.BIG_ENDIAN, Encoding.NONE, clockName, 8);
        fixture = id.createDefinition(null, name);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the IntegerDefinition(IntegerDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testIntegerDefinition() {
        IntegerDeclaration declaration = new IntegerDeclaration(1, true, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII, null, 8);
        IDefinitionScope definitionScope = null;
        String fieldName = ""; //$NON-NLS-1$

        IntegerDefinition result = new IntegerDefinition(declaration,
                definitionScope, fieldName);
        assertNotNull(result);
    }

    /**
     * Run the IntegerDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        fixture.setValue(1L);

        IntegerDeclaration result = fixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the long getValue() method test.
     */
    @Test
    public void testGetValue() {
        fixture.setValue(1L);

        long result = fixture.getValue();
        assertEquals(1L, result);
    }

    /**
     * Run the void read(BitBuffer) method test.
     */
    @Test
    public void testRead() {
        fixture.setValue(1L);
        BitBuffer input = new BitBuffer(java.nio.ByteBuffer.allocateDirect(128));

        fixture.read(input);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        fixture.setValue(1L);

        String result = fixture.toString();
        assertNotNull(result);
    }
}
