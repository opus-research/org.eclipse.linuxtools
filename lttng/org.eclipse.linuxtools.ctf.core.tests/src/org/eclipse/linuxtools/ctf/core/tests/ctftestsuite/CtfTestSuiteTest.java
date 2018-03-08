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
 *   Alexandre Montplaisir - Moved out of CTFTestTrace
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.ctftestsuite;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.junit.Test;

/**
 * Test class running the CTF Test Suite
 * (from https://github.com/efficios/ctf-testsuite).
 *
 * @author Matthew Khouzam
 */
public class CtfTestSuiteTest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final String TRACES_DIRECTORY = "../org.eclipse.linuxtools.ctf.core.tests/traces";
    private static final String METADATA_FILENAME = "metadata";

    private static final String CTF_SUITE_TEST_DIRECTORY = "ctf-testsuite/tests/1.8";

    // ------------------------------------------------------------------------
    // Test methods - Expected failures
    // ------------------------------------------------------------------------

    /**
     * Run tests in fuzzing/metadata/fail
     */
    @Test
    public void testFuzzingMetadataFail() {
        parseTracesInDirectory(getTestTracesSubDirectory(CTF_SUITE_TEST_DIRECTORY + "/fuzzing/metadata/fail"), true);
    }

    /**
     * Run tests in fuzzing/stream/fail
     */
    @Test
    public void testFuzzingStreamFail() {
        parseTracesInDirectory(getTestTracesSubDirectory(CTF_SUITE_TEST_DIRECTORY + "/fuzzing/stream/fail"), true);
    }

    /**
     * Run tests in regression/metadata/fail
     */
    @Test
    public void testRegressionMetadataFail() {
        parseTracesInDirectory(getTestTracesSubDirectory(CTF_SUITE_TEST_DIRECTORY + "/regression/metadata/fail"), true);
    }

    /**
     * Run tests in regression/stream/fail
     */
    @Test
    public void testRegressionStreamFail() {
        parseTracesInDirectory(getTestTracesSubDirectory(CTF_SUITE_TEST_DIRECTORY + "/regression/stream/fail"), true);
    }

    /**
     * Run tests in stress/metadata/fail
     */
    @Test
    public void testStressMetadataFail() {
        parseTracesInDirectory(getTestTracesSubDirectory(CTF_SUITE_TEST_DIRECTORY + "/stress/metadata/fail"), true);
    }

    /**
     * Run tests in stress/stream/fail
     */
    @Test
    public void testStressStreamFail() {
        parseTracesInDirectory(getTestTracesSubDirectory(CTF_SUITE_TEST_DIRECTORY + "/stress/stream/fail"), true);
    }

    // ------------------------------------------------------------------------
    // Test methods - Expected successes
    // ------------------------------------------------------------------------

    /**
     * Run tests on the known 'kernel' and 'trace2' traces. They should be
     * working!
     */
    @Test
    public void testKnownTracesPass() {
        parseTracesInDirectory(getTestTracesSubDirectory("kernel"), false);
        parseTracesInDirectory(getTestTracesSubDirectory("trace2"), false);
    }

    /**
     * Run tests in fuzzing/metadata/pass
     */
    @Test
    public void testFuzzingMetadataPass() {
        parseTracesInDirectory(getTestTracesSubDirectory(CTF_SUITE_TEST_DIRECTORY + "/fuzzing/metadata/pass"), false);
    }

    /**
     * Run tests in fuzzing/stream/pass
     */
    @Test
    public void testFuzzingStreamPass() {
        parseTracesInDirectory(getTestTracesSubDirectory(CTF_SUITE_TEST_DIRECTORY + "/fuzzing/stream/pass"), false);
    }

    /**
     * Run tests in regression/metadata/pass
     */
    @Test
    public void testRegressionMetadataPass() {
        parseTracesInDirectory(getTestTracesSubDirectory(CTF_SUITE_TEST_DIRECTORY + "/regression/metadata/pass"), false);
    }

    /**
     * Run tests in regression/stream/pass
     */
    @Test
    public void testRegressionStreamPass() {
        parseTracesInDirectory(getTestTracesSubDirectory(CTF_SUITE_TEST_DIRECTORY + "/regression/stream/pass"), false);
    }

    /**
     * Run tests in stress/metadata/pass
     */
    @Test
    public void testStressMetadataPass() {
        parseTracesInDirectory(getTestTracesSubDirectory(CTF_SUITE_TEST_DIRECTORY + "/stress/metadata/pass"), false);
    }

    /**
     * Run tests in stress/stream/pass
     */
    @Test
    public void testStressStreamPass() {
        parseTracesInDirectory(getTestTracesSubDirectory(CTF_SUITE_TEST_DIRECTORY + "/stress/stream/pass"), false);
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Get the File object for the subDir in the traces directory. If the sub
     * directory doesn't exist, the test is skipped.
     */
    private static File getTestTracesSubDirectory(String subDir) {
        File file = new File(TRACES_DIRECTORY + "/" + subDir);
        assumeTrue(file.isDirectory());
        return file;
    }

    /**
     * Parse the traces in given directory recursively
     *
     * @param directory
     *            The directory to search in
     * @param expectException
     *            Whether or not traces in this directory are expected to throw
     *            an exception when parsed
     * @throws CTFReaderException
     */
    private void parseTracesInDirectory(File directory, boolean expectException) {
        for (File file : directory.listFiles()) {
            if (file.getName().equals(METADATA_FILENAME)) {
                try {
                    new CTFTrace(directory);
                    if (expectException) {
                        fail("Trace was expected to fail parsing: " + directory);
                    }
                } catch (RuntimeException e) {
                    if (!expectException) {
                        e.printStackTrace();
                        fail("Failed parsing " + directory);
                    }
                } catch (CTFReaderException e) {
                    if (!expectException) {
                        e.printStackTrace();
                        fail("Failed parsing " + directory);
                    }
                }
                return;
            }

            if (file.isDirectory()) {
                parseTracesInDirectory(file, expectException);
            }
        }
    }
}
