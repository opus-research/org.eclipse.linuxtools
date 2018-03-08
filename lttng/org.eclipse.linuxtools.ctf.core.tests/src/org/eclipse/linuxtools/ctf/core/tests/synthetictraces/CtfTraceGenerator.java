/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.synthetictraces;

import java.io.File;

/**
 * Trace generator
 *
 * @author Matthew Khouzam
 */
public class CtfTraceGenerator {

    private static final String FILE_NAME = "synthetic-trace";
    private static final String PATH = System.getProperty("java.io.tmpdir") + File.separator + FILE_NAME;

    /**
     * Main, not always needed
     *
     * @param args
     *            args
     */
    public static void main(String[] args) {
        generateLttngKernelTrace();
    }

    /**
     * Gets the name of the trace (top directory name)
     *
     * @return the name of the trace
     */
    public static String getName() {
        return FILE_NAME;
    }

    /**
     * Get the path
     *
     * @return the path
     */
    public static String getPath() {
        return PATH;
    }

    /**
     * Generate a trace
     */
    public static void generateLttngKernelTrace() {
        final int cpus = 25;
        LttngKernelTraceGenerator gt = new LttngKernelTraceGenerator(2l * Integer.MAX_VALUE - 100, 500000, cpus);
        gt.writeTrace(PATH);
    }

}
