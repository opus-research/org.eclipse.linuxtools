/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.linuxtools.internal.ctf.core.Activator;
import org.junit.Test;

/**
 * <b><u>CtfCorePluginTest</u></b>
 * <p>
 * Test the CTF core plug-in activator
 */
@SuppressWarnings("javadoc")
public class CtfCorePluginTest {

    private static final String TEMP_DIR_NAME = ".temp"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Plug-in instantiation
    private final static Activator fPlugin = Activator.getDefault();


    // ------------------------------------------------------------------------
    // Test cases
    // ------------------------------------------------------------------------

    @Test
    public void testCtfCorePluginId() {
        assertEquals(
                "Plugin ID", "org.eclipse.linuxtools.ctf", Activator.PLUGIN_ID);
    }

    @Test
    public void testGetDefault() {
        Activator plugin = Activator.getDefault();
        assertEquals("getDefault()", plugin, fPlugin);
    }

    /**
     * Get the temporary directory path. If there is an instance of Eclipse
     * running, the temporary directory will reside under the workspace.
     *
     * @return the temporary directory path suitable to be passed to the
     *         java.io.File constructor without a trailing separator
     */
    public static String getTemporaryDirPath() {
        String property = System.getProperty("osgi.instance.area"); //$NON-NLS-1$
        if (property != null) {
            try {
                File dir = new File(new URI(property));
                dir = new File(dir.getAbsolutePath() + File.separator + TEMP_DIR_NAME);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                return dir.getAbsolutePath();
            } catch (URISyntaxException e) {
                Activator.logError(e.getLocalizedMessage(), e);
            }
        }
        return System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
    }


    @Test
    public void testLog() {
        try {
            Activator.log("Some message");
        } catch (Exception e) {
            fail();
        }
    }

}
