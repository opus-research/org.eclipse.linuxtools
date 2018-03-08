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
package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor.headless;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemFactory;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemOperations;
import org.eclipse.linuxtools.tmf.core.tests.ctfadaptor.headless.helper.CtfMipMapProviderForTest;

/**
 * @author Jean-Christian Kouamé
 *
 */
public class CtfMipmapQueryBenchmark {
    private static final String ATTRIBUTE_ID = "Mipmap";
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static String EVENT_NAME = "test_attribute";
    private static int resolution = 16; /*
                                         * test for more resolutions, but 16 is
                                         * the right value
                                         */
    private static final int RESOLUTIONS[] = { Integer.MAX_VALUE, 2, 3, 4, 8, 16, 24, 36, 64, 96, 128, 256, 384, 640, 1024, 2048, 4096, 8192, 16384, 32768 };
    private static final int NUM_LOOPS = 10;
    private static final int NUM_MAX_REQUEST = 10000;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private static ITmfStateSystem ssq;
    private static PrintWriter printWriter = null;
    private static int tenThousand = 10000;
    private static int testRangeTime[] = { tenThousand / 10, tenThousand, 2 * tenThousand, 4 * tenThousand, 8 * tenThousand, 16 * tenThousand, 32 * tenThousand, 64 * tenThousand };

    private static File stateFile;
    private static File stateFileBenchmark;
    /* TODO: generate me */
    /* generate me */
    private static String path = "error";

    /* eogen */

    /**
     * Start the benchmark
     *
     * @param args
     *            The command-line arguments
     */
    public static void main(final String[] args) {
        printWriter = new PrintWriter(System.out, true);
        printWriter.println("---------------------------- Benchmark started --------------------------");
        printWriter.println("---------------------------- Build --------------------------------------");
        for (int res : RESOLUTIONS) {
            resolution = res;
            if (initializeMipmap() == Status.OK_STATUS) {
                // get file size here
                for (int index = 0; index < testRangeTime.length; index++) {
                    printWriter.println("-------------------------- range time: " + testRangeTime[index] + "-----------------------------------");
                    long step = testRangeTime[index];
                    benchmark(step);
                    printWriter.println("");
                }
            } else {
                printWriter.println("build error");
            }
        }
        printWriter.println("---------------------------- Benchmark ended -----------------------------");
    }

    /**
     * build the state system
     *
     * @return An IStatus indicating if the state system could be build
     *         successfully or not.
     */
    public static IStatus initializeMipmap() {
        try {
            stateFile = File.createTempFile("mipmap", ".ht");
            stateFileBenchmark = File.createTempFile("mipmap", ".ht.benchmark");
            CtfTmfTrace fixture = new CtfTmfTrace();
            fixture.initTrace((IResource) null, path, CtfTmfEvent.class);
            fixture.indexTrace(true);
            ITmfStateProvider input = new CtfMipMapProviderForTest(fixture, new int[] { resolution, resolution, resolution });
            ssq = TmfStateSystemFactory.newFullHistory(stateFile, input, true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TmfTraceException e) {
            return new Status(IStatus.ERROR, "mipmapQueryBenchmark", e.getMessage(), e);
        } finally {
            stateFile.deleteOnExit();
            stateFileBenchmark.deleteOnExit();
        }
        return Status.OK_STATUS;
    }

    private static void benchmark(long step) {
        long startTime;
        long endTime;
        long localStep = step;
        int i = 0;
        printWriter.println("---------------------------- Maximum ------------------------------------");
        printWriter.println("Naive" + "\t" + "\t" + ATTRIBUTE_ID + "\t" + "\t" + "Naive" + "\t" + "\t" + "\t" + ATTRIBUTE_ID);
        int quark;
        try {
            quark = ssq.getQuarkAbsolute(ATTRIBUTE_ID, EVENT_NAME);
            for (startTime = ssq.getStartTime(); startTime <= ssq.getCurrentEndTime() - localStep && i < NUM_MAX_REQUEST; startTime += localStep) {
                if (startTime + localStep > ssq.getCurrentEndTime()) {
                    localStep = ssq.getCurrentEndTime() - startTime;
                }
                endTime = startTime + localStep;
                queryMaxBenchmark(startTime, endTime, quark);
                i++;
            }
            printWriter.println("");
            printWriter.println("---------------------------- Minimum ------------------------------------");
            printWriter.println("Naive" + "\t" + "\t" + ATTRIBUTE_ID + "\t" + "\t" + "Naive" + "\t" + "\t" + "\t" + ATTRIBUTE_ID);
            i = 0;
            localStep = step;
            for (startTime = ssq.getStartTime(); startTime <= ssq.getCurrentEndTime() - localStep && i < NUM_MAX_REQUEST; startTime += localStep) {
                if (startTime + localStep > ssq.getCurrentEndTime()) {
                    localStep = ssq.getCurrentEndTime() - startTime;
                }
                endTime = startTime + localStep;
                queryMinBenchmark(startTime, endTime, quark);
                i++;
            }
            System.out.println("");
            printWriter.println("");
            printWriter.println("---------------------------- Average ------------------------------------");
            printWriter.println("Naive" + "\t" + "\t" + ATTRIBUTE_ID + "\t" + "\t" + "Naive" + "\t" + "\t" + "\t" + ATTRIBUTE_ID);
            i = 0;
            localStep = step;
            for (startTime = ssq.getStartTime(); startTime <= ssq.getCurrentEndTime() - localStep && i < NUM_MAX_REQUEST; startTime += localStep) {
                if (startTime + localStep > ssq.getCurrentEndTime()) {
                    localStep = ssq.getCurrentEndTime() - startTime;
                }
                endTime = startTime + localStep;
                queryAvgBenchmark(startTime, endTime, quark);
                i++;
            }
            System.out.println("");
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void queryMaxBenchmark(long t1, long t2, int quark) {
        long next;
        long deltaNaive = 0;
        long deltaMipmap = 0;
        long prev;
        long naivemax = Long.MIN_VALUE;
        long mipmapMax = Long.MIN_VALUE;

        prev = System.nanoTime();
        for (int i = 0; i < NUM_LOOPS; i++) {
            naivemax = queryRangeMax(t1, t2);
            next = System.nanoTime();
            deltaNaive += next - prev;
            prev = next;
        }

        prev = System.nanoTime();
        try {
        for (int i = 0; i < NUM_LOOPS; i++) {
                mipmapMax = TmfStateSystemOperations.queryRangeMax(ssq, t1, t2, quark).unboxLong();
            next = System.nanoTime();
            deltaMipmap += next - prev;
            prev = next;
        }
        } catch (StateValueTypeException e) {
        } catch (AttributeNotFoundException e) {
        } catch (TimeRangeException e) {
        }
        printWriter.println(naivemax + "\t" + "\t" + mipmapMax + "\t" + "\t" + (deltaNaive / NUM_LOOPS) + " ns" + "\t" + "\t" + (deltaMipmap / NUM_LOOPS) + " ns");
    }

    private static void queryMinBenchmark(long t1, long t2, int quark) {
        long next;
        long naiveDelta = 0;
        long mipmapDelta = 0;
        long prev;
        long naiveMin = Long.MAX_VALUE;
        long mipMapMin = Long.MAX_VALUE;

        prev = System.nanoTime();
        for (int i = 0; i < NUM_LOOPS; i++) {
            naiveMin = queryRangeMin(t1, t2);
            next = System.nanoTime();
            naiveDelta += next - prev;
            prev = next;
        }

        prev = System.nanoTime();
        try {
        for (int i = 0; i < NUM_LOOPS; i++) {
                mipMapMin = (long) TmfStateSystemOperations.queryRangeAverage(ssq, t1, t2, quark);
            next = System.nanoTime();
            mipmapDelta += next - prev;
            prev = next;
        }
        } catch (AttributeNotFoundException e) {
        } catch (TimeRangeException e) {
        } catch (StateValueTypeException e) {
        }
        printWriter.println(naiveMin + "\t" + "\t" + mipMapMin + "\t" + "\t" + (naiveDelta / NUM_LOOPS) + " ns" + "\t" + "\t" + +(mipmapDelta / NUM_LOOPS) + " ns");
    }

    private static void queryAvgBenchmark(long t1, long t2, int quark) {
        long next;
        long naiveDelta = 0;
        long mipmapDelta = 0;
        long prev;
        long naiveAvg = 0;
        long mipmapAvg = 0;

        prev = System.nanoTime();
        for (int i = 0; i < NUM_LOOPS; i++) {
            naiveAvg = queryRangeAverage(t1, t2);
            next = System.nanoTime();
            naiveDelta += next - prev;
            prev = next;
        }

        prev = System.nanoTime();
        try {
            for (int i = 0; i < NUM_LOOPS; i++) {
                mipmapAvg = (long) TmfStateSystemOperations.queryRangeAverage(ssq, t1, t2, quark);
                next = System.nanoTime();
                mipmapDelta += next - prev;
                prev = next;
            }
        } catch (Exception e) {

        }

        printWriter.println(naiveAvg + "\t" + "\t" + mipmapAvg + "\t" + "\t" + (naiveDelta / NUM_LOOPS) + " ns" + "\t" + "\t" + (mipmapDelta / NUM_LOOPS) + " ns");

    }

    static long queryRangeMax(long t1, long t2) {
        long max = Long.MIN_VALUE;
        try {
            int attributeQuark = ssq.getQuarkAbsolute(ATTRIBUTE_ID, EVENT_NAME);
            List<ITmfStateInterval> intervals = ssq.queryHistoryRange(attributeQuark, t1, t2);
            for (ITmfStateInterval si : intervals) {
                max = Math.max(si.getStateValue().unboxLong(), max);
            }
        } catch (AttributeNotFoundException e) {
        } catch (TimeRangeException e) {
        } catch (StateSystemDisposedException e) {
        } catch (StateValueTypeException e) {
        }
        return max;
    }

    static long queryRangeMin(long t1, long t2) {
        long min = Long.MAX_VALUE;
        try {
            int attributeQuark = ssq.getQuarkAbsolute(ATTRIBUTE_ID, EVENT_NAME);
            List<ITmfStateInterval> intervals = ssq.queryHistoryRange(attributeQuark, t1, t2);
            for (ITmfStateInterval si : intervals) {
                min = Math.min(si.getStateValue().unboxLong(), min);
            }
        } catch (AttributeNotFoundException e) {
        } catch (TimeRangeException e) {
        } catch (StateSystemDisposedException e) {
        } catch (StateValueTypeException e) {
        }
        return min;
    }

    static long queryRangeAverage(long t1, long t2) {
        double avg = 0;
        try {
            int attributeQuark = ssq.getQuarkAbsolute(ATTRIBUTE_ID, EVENT_NAME);
            List<ITmfStateInterval> intervals = ssq.queryHistoryRange(attributeQuark, t1, t2);
            for (ITmfStateInterval si : intervals) {
                long startTime = Math.max(t1, si.getStartTime());
                long endTime = Math.min(t2, si.getEndTime());
                long delta = endTime - startTime + 1;
                avg += si.getStateValue().unboxLong() * ((double) delta / (double) (t2 - t1 + 1));
            }
        } catch (AttributeNotFoundException e) {
        } catch (TimeRangeException e) {
        } catch (StateSystemDisposedException e) {
        } catch (StateValueTypeException e) {
        }
        return (long) Math.ceil(avg);
    }
}
