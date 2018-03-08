/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Christian Kouamé - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemFactory;

/**
 * @author Jean-Christian Kouamé
 *
 */
public class CtfMipmapBuildBenchMark {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final int NUM_LOOPS = 10;
    private static final int NANOSECONDS_IN_MICROSECONDS = 1000000;
    private static final int BYTES_IN_KILOBYTES = 1024;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    // private static StateSystem ssq;
    private static PrintWriter printWriter = null;
    private static int mipmapResolution[] = {Integer.MAX_VALUE, 2, 3, 4, 8, 16, 24, 36, 64, 96, 128, 256, 384, 640, 1024, 2048, 4096, 8192, 16384, 32768 };
    private static String mipmapTracePath[] = {
        /* TODO: generate me */
        /* Generate me */ /*random events with a fixed seed with one event type. The field = "value" (long)*/
            "/home/ekadkou/lttng-traces/500Thousands/ust/pid/hello-3848-20130817-134714", // 500000 events,
            "/home/ekadkou/lttng-traces/100Thousands/ust/pid/hello-13779-20130819-095240", // 100000 events
            "/home/ekadkou/lttng-traces/500Thousands/ust/pid/hello-3848-20130817-134714", // 500000 events
            "/home/ekadkou/lttng-traces/1Million/ust/pid/hello-13942-20130819-095859", // 1000000 events
            "/home/ekadkou/lttng-traces/5Millions/ust/pid/hello-14040-20130819-100432" }; // 5000000 events
        /* end of generate me */

    /**
     * Start the benchmark
     *
     * @param args
     *            The command-line arguments
     */
    public static void main(final String[] args) {
        printWriter = new PrintWriter(System.out, true);
        printWriter.println("------------ Benchmark started ----------");
        printWriter.println("------------ resolution ----------------------");
        for (int index = 0; index < mipmapResolution.length; index++) {
            int tracePathIndex = 0;
            buildMipmapStateSystem(mipmapTracePath[tracePathIndex], tracePathIndex, mipmapResolution[index]);
        }
        printWriter.println("------------- trace size ----------------");
        for (int index = 1; index < mipmapTracePath.length; index++) {
            int defaultResolution = 16;
            buildMipmapStateSystem(mipmapTracePath[index], index, defaultResolution);
        }
        printWriter.println("------------- Benchmark ended -----------");
        printWriter.close();
    }

    private static void buildMipmapStateSystem(String path, int index, int resolution) {
        long next;
        long delta = 0;
        long prev;
        int size = 0;
        prev = System.nanoTime();
        for (int i = 0; i < NUM_LOOPS; i++) {
            File stateFile = null;
            File stateFileBenchmark = null;
            try {
                stateFile = File.createTempFile("mipmap" + index + "_" + i, ".ht");
                stateFileBenchmark = File.createTempFile("mipmap" + index + "_" + i, ".ht.benchmark");
                buildMipmap(path, resolution, stateFile, stateFileBenchmark);
                size += stateFile.length();
                next = System.nanoTime();
                delta += next - prev;
                prev = next;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (stateFile != null) {
                    stateFile.deleteOnExit();
                }
                if (stateFileBenchmark != null) {
                    stateFileBenchmark.deleteOnExit();
                }
            }
        }
        printWriter.println(index + "\t" + resolution + "\t" + size / NUM_LOOPS / BYTES_IN_KILOBYTES + "kb" + "\t" + "\t" + delta / NUM_LOOPS / NANOSECONDS_IN_MICROSECONDS + " us");
    }

    /**
     * build the state system
     *
     * @param path
     *            The path of the trace
     * @param resolution
     *            The resolution of the mipmap
     * @param stateFile
     *            The target name of the history file we want to use
     * @param stateFileBenchmark
     *            The target name of the history file we want to use for the
     *            benchmark.
     * @return An IStatus indicating if the state system could be build
     *         successfully or not.
     */
    public static IStatus buildMipmap(String path, int resolution, File stateFile, File stateFileBenchmark) {

        try {

            CtfTmfTrace fixture = new CtfTmfTrace();
            fixture.initTrace((IResource) null, path, CtfTmfEvent.class);
            fixture.indexTrace(true);
            ITmfStateProvider input = new CtfMipMapProviderForTest(fixture, new int[] {resolution, resolution, resolution});
            TmfStateSystemFactory.newFullHistory(stateFile, input, true);
        } catch (TmfTraceException e) {
            return new Status(IStatus.ERROR, "mipmapQueryBenchmark", e.getMessage(), e);
        }
        return Status.OK_STATUS;
    }
}
