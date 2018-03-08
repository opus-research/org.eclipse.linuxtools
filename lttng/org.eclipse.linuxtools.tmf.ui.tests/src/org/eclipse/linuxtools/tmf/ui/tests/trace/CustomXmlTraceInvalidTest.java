package org.eclipse.linuxtools.tmf.ui.tests.trace;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Invalid Xml files, random errors
 *
 * @author Matthew Khouzam
 *
 */
@RunWith(Parameterized.class)
public class CustomXmlTraceInvalidTest extends CustomXmlTraceTest{

    private final static String pathname = "tracesets/xml/invalid";

    /**
     * This should create the parameters to launch the project
     *
     * @return the path of the parameters
     */
    @Parameters(name = "{index}: path {0}")
    public static Collection<Object[]> getFiles() {
        File[] invalidFiles = (new File(pathname)).listFiles();
        Collection<Object[]> params = new ArrayList<Object[]>();
        for (File f : invalidFiles) {
            Object[] arr = new Object[] { f.getAbsolutePath() };
            params.add(arr);
        }
        return params;
    }

    /**
     * ctor
     *
     * @param filePath
     *            the path
     */
    public CustomXmlTraceInvalidTest(String filePath) {
        setPath(filePath);
    }

    /**
     * Test all the invalid xml files
     */
    @Test
    public void testInvalid() {
        IStatus invalid = getTrace().validate(null, getPath());
        if (IStatus.ERROR != invalid.getSeverity()) {
            fail(getPath());
        }
    }

}
