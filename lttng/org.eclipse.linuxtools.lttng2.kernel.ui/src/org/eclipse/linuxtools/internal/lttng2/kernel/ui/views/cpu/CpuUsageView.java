/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2012 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Jean-Christian Kouamé - accelerate the view using a cumulCpuUsageProvider
 *                             and monitors
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.cpu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Activator;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.StateValues;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.LineStyle;
import org.swtchart.Range;

/**
 * CPU usage view. Shows tid/pids of the top offenders in terms of usage. It is
 * displayed using a stacked barchart with SWTChart
 *
 * @author Matthew Khouzam
 */
public class CpuUsageView extends TmfView {

    /**
     * The view id
     */
    public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.ui.views.cpu"; //$NON-NLS-1$

    private static final String CPU_USAGE_VISUALIZER_THREAD_NAME = "CPU usage visualizer construction"; //$NON-NLS-1$

    private static final String PERCENT_SYMBOL = "%"; //$NON-NLS-1$

    private static final String TIME = Messages.CpuUsageView_XAxis;

    private ISeriesSet fSeriesSet;
    private Chart fChart;
    private LttngKernelTrace fCurrentTrace;

    /* Threads */
    private TimeRangeUpdateThread updateRangeThread;

    /**
     * Constructor
     */
    public CpuUsageView() {
        super(ID);
        resetXY();
    }

    @Override
    public void createPartControl(Composite parent) {
        fChart = new Chart(parent, SWT.NONE);
        fChart.getTitle().setVisible(false);
        fChart.getAxisSet().getXAxis(0).getTitle().setText(TIME);
        fChart.getAxisSet().getYAxis(0).getTitle().setText(PERCENT_SYMBOL);
        fChart.getAxisSet().getXAxis(0).getGrid().setStyle(LineStyle.NONE);
        fChart.getAxisSet().getYAxis(0).getGrid().setStyle(LineStyle.NONE);
        fSeriesSet = fChart.getSeriesSet();
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace instanceof LttngKernelTrace) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        } else {
            return;
        }
    }

    @Override
    public void setFocus() {
        fChart.setFocus();
    }

    @Override
    public void dispose() {

        if (updateRangeThread != null && updateRangeThread.isAlive()) {
            updateRangeThread.cancel();
            try {
                updateRangeThread.join();
            } catch (InterruptedException e) {
                Activator.getDefault().logWarning(e.getMessage());
            }
        }
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the trace opened signal.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        final ITmfTrace trace = signal.getTrace();
        openTrace(trace);
    }

    /**
     * The trace was selected
     *
     * @param signal
     *            the selected trace
     */
    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        final ITmfTrace trace = signal.getTrace();
        if (trace.equals(fCurrentTrace)) {
            return;
        }
        openTrace(trace);
    }

    private void openTrace(final ITmfTrace trace) {
        if (!loadTraceIfKernel(trace)) {
            if (trace instanceof TmfExperiment) {
                TmfExperiment exp = (TmfExperiment) trace;
                for (ITmfTrace child : exp.getTraces()) {
                    if (loadTraceIfKernel(child)) {
                        return;
                    }
                }
            }
        }
    }

    private boolean loadTraceIfKernel(final ITmfTrace trace) {
        if (trace instanceof LttngKernelTrace) {
            fCurrentTrace = (LttngKernelTrace) trace;
            loadTrace();
            return true;
        }
        return false;
    }

    /**
     * Trace is closed: clear the the view
     *
     * @param signal
     *            the signal received
     */
    @TmfSignalHandler
    public synchronized void traceClosed(final TmfTraceClosedSignal signal) {
        if (signal.getTrace() == fCurrentTrace) {
            if (updateRangeThread != null && updateRangeThread.isAlive()) {
                updateRangeThread.cancel();
            }
            getDisplay().asyncExec(new ViewClear());
        }
    }

    /**
     * The time range was updated
     *
     * @param signal
     *            the time range of the experiment
     */
    @TmfSignalHandler
    public synchronized void timeRangeUpdated(TmfRangeSynchSignal signal) {
        ITmfStateSystem cpuUsageSS = fCurrentTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);
        ITmfStateSystem cumulativeTimeSS = fCurrentTrace.getStateSystems().get(LttngKernelTrace.CUMUL_CPU_ID);
        if (cpuUsageSS == null || cumulativeTimeSS == null) {
            return;
        }
        if (updateRangeThread != null && updateRangeThread.isAlive()) {
            updateRangeThread.cancel();
        }

        updateRangeThread = new TimeRangeUpdateThread(CPU_USAGE_VISUALIZER_THREAD_NAME, signal.getCurrentRange());
        updateRangeThread.start();

    }

    /**
     * Gets the execution time spent in an interval (Wall clock)
     *
     * @param si
     *            the interval at the timestamp we're looking for
     * @param status
     *            the status at the timestamp we're looking for
     * @param ts
     *            the timestamp we're looking for
     * @return the real execution time at the timestamp we're looking for
     */
    private static long getRealSpentTime(ITmfStateInterval si, ITmfStateInterval status, long ts) {
        final long startTime = si.getStartTime();
        final long endTime = si.getEndTime();
        final long startTime2 = status.getStartTime();
        final long endTime2 = status.getEndTime();
        if (startTime > ts || endTime < ts ||
                startTime2 > ts || endTime2 < ts) {
            return 0L;
        }
        long timeSpent = 0L;
        int stateStatus;
        try {

            final ITmfStateValue stateValue = status.getStateValue();
            if (stateValue.isNull()) {
                return 0;
            }
            final ITmfStateValue stateValue2 = si.getStateValue();
            if (stateValue2.isNull()) {
                return 0;
            }
            stateStatus = stateValue.unboxInt();

            if (stateValue2.isNull()) {
                return 0;
            }
//            double val = (double) si.getStateValue().unboxLong() / (endTime - startTime);
//            System.out.println(val);
            timeSpent = stateValue2.unboxLong();
            if (stateStatus == StateValues.PROCESS_STATUS_RUN_USERMODE ||
                    stateStatus == (StateValues.PROCESS_STATUS_RUN_SYSCALL)) {
                timeSpent = Math.max(timeSpent - (endTime2 - ts), 0L);
            }
        } catch (StateValueTypeException e) {
        }
        return timeSpent;
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    private void resetXY() {
    }

    /**
     * Gets the current display or creates one if the current one is null
     *
     * @return the display
     */
    private static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    private synchronized void loadTrace() {
        updateRangeThread = new TimeRangeUpdateThread(CPU_USAGE_VISUALIZER_THREAD_NAME,
                new TmfTimeRange(
                        TmfTraceManager.getInstance().getActiveTrace().getStartTime(),
                        new CtfTmfTimestamp(TmfTraceManager.getInstance().getActiveTrace().getStartTime().getValue() + 100000000)));
        updateRangeThread.start();
    }

    /**
     * This function computes and updates the view
     *
     * @param signal
     *            the time range
     * @param monitor
     *            the monitor we use to know if the request is cancel or not. it
     *            allow to stop computation instead to spend time into some
     *            request
     */
    private void updateView(TmfTimeRange signal, IProgressMonitor monitor) {
        ITmfStateSystem cpuUsageSS = fCurrentTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);
        IProgressMonitor mon = (monitor == null) ? new NullProgressMonitor() : monitor;
        final long start = Math.max(cpuUsageSS.getStartTime(), signal.getStartTime().getValue());
        ITmfTimestamp timeEnd = signal.getEndTime();
        int tempNumCPU = 1;
        try {
            tempNumCPU = cpuUsageSS.getSubAttributes(cpuUsageSS.getQuarkAbsolute(Attributes.CPUS), false).size();
        } catch (AttributeNotFoundException e) {
        }
        final int numCPU = tempNumCPU;
        final int numRequests = 60;

        final long stateStartTime = Math.max(start, cpuUsageSS.getStartTime());
        long stateEndTime = Math.min(cpuUsageSS.getCurrentEndTime(), Math.max(timeEnd.getValue(), start));
        if (start != stateStartTime) {
            stateEndTime = stateStartTime + 100000000L;
        }
        final long end = stateEndTime;
        final long delta = end - start;

        final List<Integer> pidQuarks = getPids();

        final List<Process> names = getValidThreadNames(pidQuarks, start, end, mon);
        pidQuarks.clear();

        for (Process elem : names) {
            pidQuarks.add(elem.getPidQuark());
        }

        double xd[] = new double[numRequests];
        long x[] = new long[numRequests];
        final String xAxisLabel[] = new String[numRequests];
        long roundedDelta = getLargeInterval(delta);
        if (roundedDelta > numRequests) {
            setupXAxis(start, numRequests, stateStartTime, end, delta, xd, x, xAxisLabel, roundedDelta);

            /*
             * Put the top 9 threads here and the "rest"
             */

            final int listSize = Math.min(names.size(), 10);
            if (listSize >= 2) {
                List<Process> subList = names.subList(0, listSize - 1);

                Process[] croppedNames = subList.toArray(new Process[listSize - 1]);
                final String threadNames[] = new String[croppedNames.length + 1];
                for (int i = 0; i < croppedNames.length; i++) {
                    threadNames[i] = croppedNames[i].toString();
                }
                threadNames[croppedNames.length] = Messages.CpuUsageView_OtherProcess;
                final int numSeries = threadNames.length;

                /*
                 * Get CPU usage
                 */
                final double[][] y = fillYBar(numCPU, pidQuarks, start, end, numRequests, numSeries, mon);

                if (mon.isCanceled()) {
                    return;
                }

                if (getDisplay() != null && names.size() > 0) {
                    getDisplay().asyncExec(new Runnable() {

                        @Override
                        public void run() {

                            final IAxis xAxis = fChart.getAxisSet().getXAxis(0);
                            xAxis.enableCategory(true);
                            xAxis.setCategorySeries(xAxisLabel);
                            for (int i = 0; i < numSeries; i++) {

                                String data = threadNames[i];
                                IBarSeries bs = (IBarSeries) fSeriesSet.getSeries(data);
                                if (bs == null) {
                                    bs = initSeries(data);

                                }
                                bs.setYSeries(y[i]);
                            }
                            /* culling here */
                            for (ISeries series : fSeriesSet.getSeries()) {
                                String id = series.getId();
                                boolean found = false;
                                for (String tName : threadNames) {
                                    if (tName.equals(id)) {
                                        found = true;
                                    }
                                }
                                if (!found) {
                                    fSeriesSet.deleteSeries(id);
                                }
                            }

                            for (String seriesID : threadNames) {
                                fSeriesSet.bringToFront(seriesID);
                                fSeriesSet.getSeries(seriesID).enableStack(true);
                            }

                            fChart.getAxisSet().getYAxes()[0].setRange(new Range(0, 100));
                            fChart.getAxisSet().adjustRange();
                            String text = "Range = [" + new TmfTimestamp(start, ITmfTimestamp.NANOSECOND_SCALE).toString() + ',' + //$NON-NLS-1$
                                    new TmfTimestamp(end, ITmfTimestamp.NANOSECOND_SCALE).toString() + "]"; //$NON-NLS-1$
                            fChart.setToolTipText(text);
                            fChart.redraw();
                        }

                        private IBarSeries initSeries(String data) {
                            final int color = data.hashCode();
                            final int r = (color >> 16) & 0xff;
                            final int g = (color >> 8) & 0xff;
                            final int b = color & 0xff;
                            IBarSeries bs = (IBarSeries) fSeriesSet.createSeries(SeriesType.BAR, data);
                            bs.enableStack(true);
                            bs.setBarColor(new Color(Display.getDefault(), r, g, b));
                            bs.setBarPadding(0);
                            return bs;
                        }

                    });
                }
            }
        }
    }

    private static void setupXAxis(long start, int numRequests, long stateStartTime, long end, long delta, double[] xd, long[] x, final String[] xAxisLabel, long roundedDelta) {
        int numIntervals = 10;
        List<Long> positions = new ArrayList<Long>();
        long clippedStart = getFirstVal(start, roundedDelta, numIntervals);
        long roundedStep = Math.max(roundedDelta / numIntervals, 1);
        long stepValue = clippedStart;
        while ((roundedStep + stepValue) < start) {
            stepValue += roundedStep;
        }
        while (end > stepValue) {
            positions.add(stepValue);
            stepValue += roundedStep;
        }
        int curPos = 0;
        for (int req = 0; req < numRequests; req++) {
            xAxisLabel[req] = new String(""); //$NON-NLS-1$
            long trs = (long) (stateStartTime + (delta) * ((double) req / numRequests));
            long tre = (long) (stateStartTime + (delta) * ((double) (req + 1) / numRequests));
            x[req] = (trs / 2 + tre / 2);
            if (curPos < positions.size()) {
                long curVal = positions.get(curPos);
                while ((curVal < trs) && (curPos < positions.size())) {
                    curVal = positions.get(curPos);
                    curPos++;
                }
                if ((curVal > trs) && (curVal < tre)) {
                    String xlabel = new CtfTmfTimestamp(curVal).toString();
                    xlabel = xlabel.substring(0, xlabel.length() - 8);
                    while (xlabel.startsWith("0")) { //$NON-NLS-1$
                        xlabel = xlabel.substring(1);
                    }
                    xAxisLabel[req] = xlabel;
                }

            }
            xd[req] = x[req];
        }
    }

    private static long getLargeInterval(long delta) {
        final double values[] = { 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5 };
        long diffSize = delta;
        long precision = 1;
        while (diffSize >= 1) {
            diffSize /= 10;
            precision *= 10;
        }
        precision /= 10;
        for (int i = 0; i < values.length; i++) {
            final long retVal = (long) (values[i] * precision);
            if (retVal > delta) {
                return retVal;
            }
        }
        return precision;
    }

    private static long getFirstVal(long startVal, long roundedDelta, int numIntervals) {
        long retVal = (startVal / roundedDelta) * roundedDelta;
        long step = roundedDelta / numIntervals;
        while ((retVal + step) < startVal) {
            retVal += step;
        }
        return retVal;
    }

    private List<Process> getValidThreadNames(List<Integer> pidQuarks, long start, long end, IProgressMonitor monitor) {
        final List<Process> names = globalCPUUsage(pidQuarks, start, end, monitor);

        List<Process> elemsToRemove = new ArrayList<Process>();
        for (Process elem : names) {
            if ((elem.getExecTime() == 0.0) || (elem.getPid() == 0)) {
                elemsToRemove.add(elem);
            }
        }
        for (Process elem : elemsToRemove) {
            names.remove(elem);
        }
        Collections.sort(names);
        return names;
    }

    private List<Process> globalCPUUsage(List<Integer> pidQuarks, long start, long end, IProgressMonitor monitor) {
        List<Process> names = new ArrayList<CpuUsageView.Process>();
        ITmfStateSystem cpuUsageSS = fCurrentTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);
        for (int pidQuark : pidQuarks) {
            if (monitor.isCanceled()) {
                return names;
            }
            String pid = cpuUsageSS.getAttributeName(pidQuark);
            String execName = getExecName(start, end, cpuUsageSS, pidQuark);
            double factor = 1.0;
            long delta = getExectime(pid, pidQuark, start, end);
            double data[] = { delta * factor };
            names.add(new Process(execName, Integer.parseInt(pid), data, pidQuark));
        }
        return names;
    }

    private double[][] fillYBar(int numberOfCPUS, List<Integer> pidQuarkList, long startTime, long endTime, int numRequests, final int numSeries, IProgressMonitor monitor) {
        double y[][] = new double[numSeries][numRequests];
        final int otherRow = numSeries - 1;
        Arrays.fill(y[otherRow], 0.0);

        double fullY[][] = new double[pidQuarkList.size()][numRequests];
        List<Process> processes = new ArrayList<Process>();

        final double step = (endTime - startTime) / numRequests;
        final double factor = 100.0 / step / numberOfCPUS;
        int pids[] = new int[pidQuarkList.size()];
        ITmfStateSystem cpuUsageSS = fCurrentTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);

        for (int i = 0; i < pidQuarkList.size(); i++) {
            if (monitor.isCanceled()) {
                return y;
            }
            Arrays.fill(fullY[i], 0.0);
            int pidQuark = pidQuarkList.get(i);

            final String attributeName = cpuUsageSS.getAttributeName(pidQuark);
            if (!attributeName.equals("Unknown")) { //$NON-NLS-1$
                String execName = getExecName(startTime, endTime, cpuUsageSS, pidQuark);
                pids[i] = Integer.parseInt(attributeName);
                long iet = startTime;
                for (int req = 0; req < numRequests && (iet <= endTime - step); req++) {
                    if (monitor.isCanceled()) {
                        return y;
                    }
                    if (iet == 1373031049811245824L) {
                        new Object();
                    }
                    long ist = iet + (long) step - 1;
                    long delta = getExectime(attributeName, pidQuark, iet, ist);
                    final double fullYValue = delta * factor;
                    if (fullYValue > 100) {
                        new Object();
                    }
                    fullY[i][req] += fullYValue;
                    iet += step;
                }
                Process p = new Process(execName, pids[i], fullY[i], pidQuark);
                processes.add(p);
            }

        }
        List<Process> processesToRemove = new ArrayList<CpuUsageView.Process>();
        for (Process p : processes) {
            if (p.getPid() == 0) {
                processesToRemove.add(p);
            }
        }
        for (Process p : processesToRemove) {
            processes.remove(p);
        }
        processesToRemove.clear();
        Collections.sort(processes);

        for (int i = 0; i < numSeries; i++) {
            final boolean addToList = i < otherRow;
            final Process process = processes.get(i);
            boolean addToOther = !(process.pid == 0 || addToList);
            for (int req = 0; req < numRequests; req++) {
                final double valueToAdd = process.getExecTimeArray()[req];
                if (addToList) {
                    y[i][req] = valueToAdd;
                }
                else if (addToOther) {
                    y[otherRow][req] += valueToAdd;
                }
            }
        }
        return y;
    }

    private static String getExecName(long startTime, long endTime, ITmfStateSystem cpuUsageSS, int pidQuark) {
        final String unknown = Messages.CpuUsageView_UnknownProcess;
        int execNameQuark;
        try {
            execNameQuark = cpuUsageSS.getQuarkRelative(pidQuark, Attributes.EXEC_NAME);
            final ITmfStateValue startTimeStateValue = cpuUsageSS.querySingleState(startTime, execNameQuark).getStateValue();
            if (startTimeStateValue.isNull()) {
                final ITmfStateValue endTimeStateValue = cpuUsageSS.querySingleState(endTime, execNameQuark).getStateValue();
                if (endTimeStateValue.isNull()) {
                    return unknown;
                }
                return endTimeStateValue.unboxStr();
            }
            return startTimeStateValue.unboxStr();
        } catch (AttributeNotFoundException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        } catch (TimeRangeException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        } catch (StateSystemDisposedException e) {
        } catch (StateValueTypeException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }
        return unknown;

    }

    /**
     * Gets a list of PIDs (actually TIDs since we're on the kernel side)
     *
     * @return the PID list
     */
    private List<Integer> getPids() {
        List<Integer> children = new ArrayList<Integer>();
        try {
            ITmfStateSystem cpuUsageSS = fCurrentTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);
            int threadQuark = cpuUsageSS.getQuarkAbsolute(Attributes.THREADS);
            children = cpuUsageSS.getSubAttributes(threadQuark, false);
        } catch (AttributeNotFoundException e) {
            Activator.getDefault().logError(e.toString(), e);
        }
        return children;
    }

    private long getExectime(String pid, int pidQuark, long sTime, long eTime) {
        long toReturn = 0L;
        try {
            ITmfStateSystem cpuUsageSS = fCurrentTrace.getStateSystems().get(LttngKernelTrace.STATE_ID);
            ITmfStateSystem cumulativeCpuTimeSS = fCurrentTrace.getStateSystems().get(LttngKernelTrace.CUMUL_CPU_ID);
            /* get the exec time for this pid */
            int execTimeQuark = cumulativeCpuTimeSS.getQuarkAbsolute(Attributes.THREADS, pid, Attributes.EXEC_TIME);
            int statusQuark = cpuUsageSS.getQuarkRelative(pidQuark, Attributes.STATUS);
            /* get the Intervals this range */
            ITmfStateInterval status = cpuUsageSS.querySingleState(sTime, statusQuark);
            ITmfStateInterval execTime = cumulativeCpuTimeSS.querySingleState(sTime, execTimeQuark);

            long leftSpentTime = getRealSpentTime(execTime, status, sTime);

            ITmfStateInterval statusr = cpuUsageSS.querySingleState(eTime, statusQuark);
            ITmfStateInterval execTimer = cumulativeCpuTimeSS.querySingleState(eTime, execTimeQuark);
            long rightSpentTime = getRealSpentTime(execTimer, statusr, eTime);
            toReturn = Math.max(rightSpentTime - leftSpentTime, 0L);
        } catch (AttributeNotFoundException e) {
            Activator.getDefault().logError(e.toString(), e);
        } catch (TimeRangeException e) {
            Activator.getDefault().logError(e.toString(), e);
        } catch (StateSystemDisposedException e) {
        }
        return toReturn;
    }

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private class ViewClear implements Runnable {
        @Override
        public void run() {
            List<ISeries> toRemove = Arrays.asList(fSeriesSet.getSeries());
            for (ISeries series : toRemove) {
                fSeriesSet.deleteSeries(series.getId());
            }
            fChart.redraw();
        }
    }

    private class Process implements Comparable<Process> {
        final int pidQuark;
        final String procName;
        final int pid;
        final double execTime;
        final double execTimeArray[];

        public Process(String name, int pid_, double executionTime[], int pidQuark_) {
            procName = name;
            pid = pid_;
            execTimeArray = executionTime;
            pidQuark = pidQuark_;
            double acc = 0;
            for (double execTime_ : executionTime) {
                acc += execTime_;
            }
            execTime = acc;
        }

        public int getPid() {
            return pid;
        }

        public double[] getExecTimeArray() {
            return execTimeArray;
        }

        public double getExecTime() {
            return execTime;
        }

        public int getPidQuark() {
            return pidQuark;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return procName + ":" + pid; //$NON-NLS-1$
        }

        @Override
        public int compareTo(Process o) {
            if (execTime < o.execTime) {
                return 1;
            }
            if (execTime > o.execTime) {
                return -1;
            }
            return 0;
        }

    }

    private class TimeRangeUpdateThread extends Thread {
        private TmfTimeRange fTimeRange;
        private final IProgressMonitor fMonitor;

        public TimeRangeUpdateThread(String name, TmfTimeRange tr) {
            super(name);
            this.fTimeRange = tr;
            fMonitor = new NullProgressMonitor();
        }

        @Override
        public void run() {
            /* Clear the graph, in case a trace was previously using it */
            getDisplay().asyncExec(new ViewClear());
            fCurrentTrace.getStateSystems().get(LttngKernelTrace.STATE_ID).waitUntilBuilt();
            fCurrentTrace.getStateSystems().get(LttngKernelTrace.CUMUL_CPU_ID).waitUntilBuilt();
            updateView(fTimeRange, fMonitor);
        }

        public void cancel() {
            fMonitor.setCanceled(true);
        }
    }
}
