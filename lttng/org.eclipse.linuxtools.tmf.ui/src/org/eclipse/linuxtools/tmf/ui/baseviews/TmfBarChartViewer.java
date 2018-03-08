package org.eclipse.linuxtools.tmf.ui.baseviews;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.viewers.TmfViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;

public abstract class TmfBarChartViewer extends TmfViewer {

    /** Width of each histogram bar, in pixels */
    private static final int BAR_WIDTH = 1;

    private final List<String> seriesNames = new ArrayList<String>();
    private final List<RGB> colors = new ArrayList<RGB>();

    protected final Chart chart;

    /** Semaphore to ensure only one histogram query is run at the same time */
    private final Semaphore sem = new Semaphore(1);

    public TmfBarChartViewer(Composite parent, String title,
            String xLabel, String yLabel) {
        this.chart = new Chart(parent, SWT.NONE);

        IAxis xAxis = chart.getAxisSet().getXAxis(0);
        IAxis yAxis = chart.getAxisSet().getYAxis(0);

        /* Set the title/labels, or hide them if they are not provided */
        if (title == null) {
            chart.getTitle().setVisible(false);
        } else {
            chart.getTitle().setText(title);
        }
        if (xLabel == null) {
            xAxis.getTitle().setVisible(false);
        } else {
            xAxis.getTitle().setText(xLabel);
        }
        if (yLabel == null) {
            yAxis.getTitle().setVisible(false);
        } else {
            yAxis.getTitle().setText(yLabel);
        }

        /* Hide the grid */
        xAxis.getGrid().setStyle(LineStyle.NONE);
        yAxis.getGrid().setStyle(LineStyle.NONE);

        /* Hide the legend */
        chart.getLegend().setVisible(false);
    }

    private static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    /**
     * Signal handler for the range updated signal.
     *
     * @param signal
     *            The signal
     */
    @TmfSignalHandler
    public void timeRangeUpdated(TmfRangeSynchSignal signal) {
        final ITmfTimestamp timeStart = signal.getCurrentRange().getStartTime();
        final ITmfTimestamp timeEnd = signal.getCurrentRange().getEndTime();

        /*
         * It would probably be better to let the *latest* query go through,
         * instead of the earliest, but in practice the behaviour is mostly the
         * same. The semaphore here is mostly to protect against excessive range
         * updates, ie when the user scrolls the mouse-wheel like crazy on
         * purpose ;)
         */
        if (!sem.tryAcquire()) {
            return;
        }

        getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                long tStart = timeStart.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long tEnd = timeEnd.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                int numRequests = chart.getPlotArea().getBounds().width / BAR_WIDTH;

                if (numRequests == 0) {
                    return;
                }

                for (int i = 0; i < seriesNames.size(); i++) {
                    ISeries series = chart.getSeriesSet().getSeries(seriesNames.get(i));
                    if (series == null) {
                        series = initSeries(seriesNames.get(i), colors.get(i));
                    }
                    readData(series, tStart, tEnd, numRequests);
                }
            }

            private IBarSeries initSeries(String name, RGB color) {
                IBarSeries bs = (IBarSeries) chart.getSeriesSet().createSeries(SeriesType.BAR, name);
                bs.enableStack(true);
                bs.setBarColor(new Color(Display.getDefault(), color));
                bs.setBarPadding(0);
                return bs;
            }
        });
    }

    public void clearView() {
        //TODO
    }

    protected void addSeries(String name, RGB color) {
        seriesNames.add(name);
        colors.add(color);
    }

    protected void clearSeries() {
        seriesNames.clear();
        colors.clear();
    }

    /**
     * Draw the given series on the chart
     *
     * @param series
     *            The series to display
     * @param x
     *            The X values. Can be computed with
     *            {@link TmfBarChartViewer#getXAxis}
     * @param y
     *            The Y values that were computed by the extended class
     */
    protected void drawChart(final ISeries series, final double[]x, final double[] y) {
        getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                series.setXSeries(x);
                series.setYSeries(y);
                chart.getAxisSet().adjustRange();
                chart.redraw();
            }
        });
        sem.release();
    }

    /**
     * Convenience method to compute the X axis values for a given time range.
     *
     * @param start
     *            Start of the time range
     * @param end
     *            End of the range
     * @param nb
     *            Number of steps. This will be the size of the returned array.
     * @return The time values (converted to double) that match every step
     */
    protected final static double[] getXAxis(long start, long end, int nb) {
        double timestamps[] = new double[nb];
        long step = (end - start) / nb;

        long curTime = start;
        for (int i = 0; i < nb; i++) {
            timestamps[i] = curTime;
            curTime += step;
        }
        return timestamps;
    }

    /**
     * Load the data for the given series. This method should call
     * {@link TmfBarChartViewer#drawChart} to return the results when done.
     *
     * Careful, this method is called by a signal handler which also happens to
     * be in the main UI thread. This means any processing will block the UI! In
     * most cases it's probably better to start a separate Thread/Job to do the
     * processing, and that one can call drawChart() when done to update the
     * view.
     *
     * @param series
     *            Which series of the chart should the viewer update
     * @param start
     *            The start time (in nanoseconds) of the range to display
     * @param end
     *            The end time of the range to display.
     * @param nb
     *            The number of 'steps' in the bar chart (fewer steps means each
     *            bar is wider).
     */
    protected abstract void readData(ISeries series, long start, long end, int nb);
}