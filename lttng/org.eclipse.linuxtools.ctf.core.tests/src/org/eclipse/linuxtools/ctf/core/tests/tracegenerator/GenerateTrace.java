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

package org.eclipse.linuxtools.ctf.core.tests.tracegenerator;

import java.io.File;

/**
 * Trace generator
 *
 * @author Matthew Khouzam
 */
public class GenerateTrace {

    private static final String PATH = System.getProperty("java.io.tmpdir") + File.separator + "synthetic-trace";

    /**
     * Main, not always needed
     *
     * @param args
     *            args
     */
    public static void main(String[] args) {
        generateTrace();
    }

    /**
     * Get the path
     *
     * @return the path
     */
    public static String generateTraceAndPath() {
        generateTrace();
        return PATH;
    }

    /**
     * Generate a trace
     */
    public static void generateTrace() {
        final int cpus = 25;
        GenerateKernelTrace gt = new GenerateKernelTrace(2l * Integer.MAX_VALUE - 100, 500000, cpus);
        gt.writeTrace(PATH);
    }

}
