package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.cpu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.StateValues;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.CtfKernelTrace;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
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
 * @author Matthew Khouzam
 *
 */
public class CPUUsage extends TmfView {
    /**
     * The id
     */
    public static final String ID = "org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.cpu"; //$NON-NLS-1$

    private ISeriesSet fSeriesSet;
    private Chart fChart;
    private TmfExperiment fExperiment;
    private ITmfStateSystem fKSS;

    /**
     *
     */
    public CPUUsage() {
        super(ID);
        resetXY();
    }

    private void resetXY() {
    }

    @Override
    public void createPartControl(Composite parent) {
        String seriesId = "CPU Usage"; //$NON-NLS-1$
        fChart = new Chart(parent, SWT.NONE);
        fChart.getTitle().setText(seriesId);
        fChart.getAxisSet().getXAxis(0).getTitle().setText("Time"); //$NON-NLS-1$
        fChart.getAxisSet().getYAxis(0).getTitle().setText("%"); //$NON-NLS-1$
        fChart.getAxisSet().getXAxis(0).getGrid().setStyle(LineStyle.NONE);
        fChart.getAxisSet().getYAxis(0).getGrid().setStyle(LineStyle.NONE);
        fSeriesSet = fChart.getSeriesSet();
        fExperiment = TmfExperiment.getCurrentExperiment();
        if (fExperiment == null) {
            return;
        }
        final ITmfTrace trace = fExperiment.getTraces()[0];
        if (trace instanceof CtfKernelTrace) {
            fKSS = trace.getStateSystem();
            fKSS.waitUntilBuilt();
        }
        else {
            return;
        }
        if (fKSS != null) {
            updateView(new TmfTimeRange(fExperiment.getStartTime(), new CtfTmfTimestamp(fExperiment.getStartTime().getValue() + 100000000)));
        }

    }

    @Override
    public void setFocus() {
    }

    Random rnd = new Random();

    /**
     * @return
     */
    private static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    /**
     * @param signal
     *            the selected experiment
     */
    @TmfSignalHandler
    public void experimentSelected(final TmfExperimentSelectedSignal signal) {
        if (signal.getExperiment().equals(fExperiment)) {
            return;
        }
        fExperiment = signal.getExperiment();

        final ITmfTrace trace = fExperiment.getTraces()[0];
        if (trace instanceof CtfKernelTrace) {
            fKSS = trace.getStateSystem();
            fKSS.waitUntilBuilt();
        }
        else {
            return;
        }
        updateView(new TmfTimeRange(fExperiment.getStartTime(), new CtfTmfTimestamp(fExperiment.getStartTime().getValue() + 100000000)));
    }

    /**
     * @param signal
     *            the time range of the experiment
     */
    @TmfSignalHandler
    public void timeRangeUpdated(TmfRangeSynchSignal signal) {
        TmfTimeRange value = signal.getCurrentRange();
        if (fKSS == null) {
            return;
        }
        updateView(value);
    }

    /**
     * @param signal
     */
    private void updateView(TmfTimeRange signal) {
        final long start = Math.max(fKSS.getStartTime(), signal.getStartTime().getValue());
        ITmfTimestamp timeEnd = signal.getEndTime();
        int tempNumCPU = 1;
        try {
            tempNumCPU = fKSS.getSubAttributes(fKSS.getQuarkAbsolute(Attributes.CPUS), false).size();
        } catch (AttributeNotFoundException e) {
        }
        final int numCPU = tempNumCPU;
        final int numRequests = 60;

        final long stateStartTime = Math.max(start, fKSS.getStartTime());
        long stateEndTime = Math.min(fKSS.getCurrentEndTime(), Math.max(timeEnd.getValue(), start));
        if (start != stateStartTime) {
            stateEndTime = stateStartTime + 100000000L;
        }
        final long end = stateEndTime;
        final long delta = end - start;

        final List<Integer> pidQuarks = getPids();

        final List<Process> names = getValidThreadNames(pidQuarks, start, end);
        pidQuarks.clear();

        for(Process elem: names){
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
            List<Process> subList = names.subList(0, listSize - 1);

            Process[] croppedNames = subList.toArray(new Process[listSize - 1]);
            final String threadNames[] = new String[croppedNames.length + 1];
            for (int i = 0; i < croppedNames.length; i++) {
                threadNames[i] = croppedNames[i].toString();
            }
            threadNames[croppedNames.length] = "other"; //$NON-NLS-1$
            final int numSeries = threadNames.length;

            /*
             * Alex put the CPU usage here please
             */
            final double[][] y = fillYBar(numCPU, pidQuarks, start, end, numRequests, numSeries);

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
                        fChart.redraw();
                    }

                    /**
                     * @param data
                     * @return
                     */
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


    private static void setupXAxis(final long start, final int numRequests, final long stateStartTime, final long end, final long delta, double[] xd, long[] x, final String[] xAxisLabel, long roundedDelta) {
        int numIntervals = 8;
        ArrayList<Long> positions = new ArrayList<Long>();
        long clippedStart = getFirstVal(start, roundedDelta,numIntervals);
        long roundedStep = roundedDelta/numIntervals;
        long stepValue = clippedStart;
        while( (roundedStep + stepValue ) < start) {
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
                    xAxisLabel[req] = new CtfTmfTimestamp(curVal).toString();
                }

            }
            xd[req] = x[req];
        }
    }


    private static long getLargeInterval(final long delta) {
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

    private static long getFirstVal(final long startVal, final long roundedDelta, int numIntervals) {
        long retVal = (startVal / roundedDelta) * roundedDelta;
        long step = roundedDelta / numIntervals;
        while ((retVal + step) < startVal) {
            retVal += step;
        }
        return retVal;
    }


    /**
     * @param pidQuarks
     * @param start
     * @param end
     * @return
     */
    private List<Process> getValidThreadNames(final List<Integer> pidQuarks, final long start, final long end) {
        final List<Process> names = globalCPUUsage(pidQuarks, start, end);

        // new ArrayList<Process> = (pidQuarks, stateStartTime, stateEndTime);
        ArrayList<Process> elemsToRemove = new ArrayList<Process>();
        for (Process elem : names) {
            if(( elem.getExecTime() == 0.0 )|| (elem.getPid() == 0)) {
                elemsToRemove.add(elem);
            }
        }
        for (Process elem : elemsToRemove) {
            names.remove(elem);
        }
        Collections.sort(names);
        return names;
    }

    private List<Process> globalCPUUsage(final List<Integer> pidQuarks, final long start, final long end) {
        List<Process> names = new ArrayList<CPUUsage.Process>();
        for (int pidQuark : pidQuarks) {
            try {
                String pid = fKSS.getAttributeName(pidQuark);
                int execNameQuark = fKSS.getQuarkRelative(pidQuark, Attributes.EXEC_NAME);
                String execName = fKSS.querySingleState(start, execNameQuark).getStateValue().unboxStr();
                final String NULL_STRING = "nullValue"; //$NON-NLS-1$
                if (execName.equals(NULL_STRING)) {
                    execName = fKSS.querySingleState(end, execNameQuark).getStateValue().unboxStr();
                }
                if (execName.equals(NULL_STRING)) {
                    execName = "UNKNOWN"; //$NON-NLS-1$
                }
                int statusQuark = fKSS.getQuarkRelative(pidQuark, Attributes.STATUS);
                List<ITmfStateInterval> intervals = fKSS.queryHistoryRange(statusQuark, start, end);
                double factor = 1.0;
                double data[] = cpuUsageToDouble(start, end, intervals, factor);
                names.add(new Process(execName, Integer.parseInt(pid), data, pidQuark));
            } catch (AttributeNotFoundException e) {
            } catch (TimeRangeException e) {
            } catch (StateSystemDisposedException e) {
            } catch (StateValueTypeException e) {
            }
        }
        return names;
    }

    private double[][] fillYBar(final int numberOfCPUS, final List<Integer> pidQuarkList, long startTime, long endTime, final int numRequests, final int numSeries) {
        double y[][] = new double[numSeries][numRequests];
        final int otherRow = numSeries - 1;
        Arrays.fill(y[otherRow], 0.0);

        double fullY[][] = new double[pidQuarkList.size()][numRequests];
        List<Process> processes = new ArrayList<Process>();

        final double step = (endTime - startTime) / numRequests;
        final double factor = 100.0 / step / numberOfCPUS;
        int pids[] = new int[pidQuarkList.size()];

        for (int i = 0; i < pidQuarkList.size(); i++) {
            Arrays.fill(fullY[i], 0.0);
            int pidQuark = pidQuarkList.get(i);
            int statusQuark;

            try {
                final String attributeName = fKSS.getAttributeName(pidQuark);
                if (!attributeName.equals("Unknown")) { //$NON-NLS-1$
                    int execNameQuark = fKSS.getQuarkRelative(pidQuark, Attributes.EXEC_NAME);
                    String execName = fKSS.querySingleState(startTime, execNameQuark).getStateValue().unboxStr();
                    if (execName.equals("nullValue")) { //$NON-NLS-1$
                        execName = fKSS.querySingleState(endTime, execNameQuark).getStateValue().unboxStr();
                    }
                    pids[i] = Integer.parseInt(attributeName);
                    statusQuark = fKSS.getQuarkRelative(pidQuark, Attributes.STATUS);
                    long ts=System.nanoTime();
                    List<ITmfStateInterval> execTimes = fKSS.queryHistoryRange(statusQuark, startTime, endTime);
                    long te=System.nanoTime();
                    Long l = new Long(te - ts);
                    for (ITmfStateInterval interval : execTimes) {
                        final int stateStatus = interval.getStateValue().unboxInt();
                        if (stateStatus == StateValues.PROCESS_STATUS_RUN_USERMODE ||
                                stateStatus == (StateValues.PROCESS_STATUS_RUN_SYSCALL))
                        {
                            long currTime = startTime;
                            final long iet = interval.getEndTime();
                            final long ist = interval.getStartTime();

                            for (int req = 0; req < numRequests && (currTime < iet); req++) {
                                long newStart = Math.max(currTime, ist);
                                long newEnd = Math.min(currTime + (long) step, iet);
                                currTime = startTime + (long) (step * (req + 1.0));
                                newStart = Math.min(newEnd, newStart);
                                final long delta = newEnd - newStart;
                                fullY[i][req] += delta * factor;
                            }
                        }
                    }
                    Process p = new Process(execName, pids[i], fullY[i], pidQuark);
                    processes.add(p);
                }

            } catch (AttributeNotFoundException e) {
            } catch (TimeRangeException e) {
            } catch (StateSystemDisposedException e) {
            } catch (StateValueTypeException e) {
            }
        }
        List<Process> processesToRemove = new ArrayList<CPUUsage.Process>();
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
                if (valueToAdd > 70) {
                    new Object();
                }
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

    private static double[] cpuUsageToDouble(final long startTime, final long endTime, List<ITmfStateInterval> intervals, double factor) throws StateValueTypeException {
        int i = 0;
        double data[] = new double[intervals.size()];
        for (ITmfStateInterval interval : intervals) {
            int stateStatus = interval.getStateValue().unboxInt();

            if (stateStatus == StateValues.PROCESS_STATUS_RUN_USERMODE ||
                    stateStatus == (StateValues.PROCESS_STATUS_RUN_SYSCALL))
            {
                long newStart = Math.max(startTime, interval.getStartTime());
                long newEnd = Math.min(endTime, interval.getEndTime());
                newStart = Math.min(newEnd, newStart);
                final long delta = newEnd - newStart;
                data[i] += delta * factor;
            }
            i++;

        }
        return data;
    }

    /**
     * @return
     */
    private List<Integer> getPids() {
        List<Integer> children = new ArrayList<Integer>();
        try {
            int threadQuark = fKSS.getQuarkAbsolute(Attributes.THREADS);
            children = fKSS.getSubAttributes(threadQuark, false);
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        }
        return children;
    }

    class Process implements Comparable<Process> {
        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return procName + ":" + pid; //$NON-NLS-1$
        }

        /**
         * @return the procName
         */
        public String getProcName() {
            return procName;
        }

        /**
         * @return the pid
         */
        public int getPid() {
            return pid;
        }

        public double[] getExecTimeArray() {
            return execTimeArray;
        }

        /**
         * @return the execTime
         */
        public double getExecTime() {
            return execTime;
        }

        /**
         * @return the pidQuark
         */
        public int getPidQuark() {
            return pidQuark;
        }

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


        final int pidQuark;
        final String procName;
        final int pid;
        final double execTime;
        final double execTimeArray[];

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
}
