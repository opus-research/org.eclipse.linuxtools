/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.xycharts;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.IAxisTick;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;
import org.swtchart.Range;

/**
 * Abstract line chart viewer class implementation. Series by default will be
 * displayed as a line. Each series appearance can be overridden when creating
 * it.
 *
 * @author - Geneviève Bastien
 * @since 3.0
 */
public abstract class TmfXYLinePlotViewer extends TmfXYChartViewer {

    private static final double DEFAULT_MAXY = 0.0;
    /* FIXME: The number of points should depend on the size of the viewer */
    private static final int POINTS = 800;

    private final Map<String, double[]> fSeriesValues = new LinkedHashMap<String, double[]>();
    private double[] fXValues;
    private double fMaxYValue = DEFAULT_MAXY;

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     * @param title
     *            The title of the viewer
     * @param xLabel
     *            The label of the xAxis
     * @param yLabel
     *            The label of the yAXIS
     */
    public TmfXYLinePlotViewer(Composite parent, String title, String xLabel, String yLabel) {
        super(parent, title, xLabel, yLabel, TmfXYChartViewer.DEFAULT_PROVIDERS);
        updateXValues();
    }

    @Override
    public void initialize(ITmfTrace trace) {
        super.initialize(trace);
        fSeriesValues.clear();
        Thread thread = new Thread() {
            @Override
            public void run() {
                initializeDataSource();
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (!fSwtChart.isDisposed()) {
                            /* Delete the old series */
                            clearView();
                            for (ISeries s : fSwtChart.getSeriesSet().getSeries()) {
                                fSwtChart.getSeriesSet().deleteSeries(s.getId());
                            }
                            createSeries();
                            updateContent();
                        }
                    }
                });
            }
        };
        thread.start();
    }

    /**
     * Initialize the source of the data for this viewer. This method is run in
     * a separate thread, so this is where for example one can execute an
     * analysis module and wait for its completion to initialize the series
     */
    protected void initializeDataSource() {

    }

    @Override
    protected void updateContent() {
        fMaxYValue = DEFAULT_MAXY;
        updateXValues();
        updateData(fXValues);
        updateDisplay();
    }

    /**
     * Update the values of the x axis for the given range
     *
     * FIXME: The number of points should depend on the size of the viewer and
     * should be computed here
     */
    protected void updateXValues() {
        fXValues = new double[POINTS];
        long ts = getWindowStartTime();
        long te = getWindowEndTime();
        double step = (double) (te - ts) / POINTS;
        for (int i = 0; i < POINTS; i++) {
            fXValues[i] = i * step + 1;
        }
        this.fXOffset = ts - 1;
    }

    /**
     * Update the series data because the time range has changed
     *
     * @param xvalues
     *            The array of absciss values we want for the series. The values
     *            in this array are relative to windows start time.
     */
    protected abstract void updateData(double[] xvalues);

    /**
     * Set the data for a given series of the graph. The series does not need to
     * be created before calling this, but it needs to have at least as many
     * values as the x axis.
     *
     * If the series does not exist, it will automatically be created at display
     * time, with the default values.
     *
     * @param seriesName
     *            The name of the series for which to set the values
     * @param seriesValues
     *            The array of values for the series
     * @param maxVal
     *            The maximum value in the serie
     */
    protected void setSeries(String seriesName, double[] seriesValues, double maxVal) {
        if (fXValues.length > seriesValues.length) {
            throw new IllegalStateException();
        }
        fMaxYValue = Math.max(fMaxYValue, maxVal);
        fSeriesValues.put(seriesName, seriesValues);
    }

    /**
     * Add a new series to the XY line chart. By default, it is a simple solid
     * line.
     *
     * TODO: This is where the color alternance and other defaults should be set
     *
     * @param seriesName
     *            The name of the series to create
     * @return The series so that the concrete viewer can modify its properties
     *         if required
     */
    protected ILineSeries addSeries(String seriesName) {
        ILineSeries series = (ILineSeries) fSwtChart.getSeriesSet().createSeries(SeriesType.LINE, seriesName);
        series.setVisible(true);
        series.enableArea(false);
        series.setLineStyle(LineStyle.SOLID);
        series.setSymbolType(PlotSymbolType.NONE);
        return series;
    }

    /**
     * Delete a series from the chart and its values from the viewer.
     *
     * @param seriesName
     *            Name of the series to delete
     */
    protected void deleteSeries(String seriesName) {
        ISeries series = fSwtChart.getSeriesSet().getSeries(seriesName);
        if (series != null) {
            fSwtChart.getSeriesSet().deleteSeries(series.getId());
        }
        fSeriesValues.remove(seriesName);
    }

    /**
     * Update the chart's values before refreshing the viewer
     */
    protected void updateDisplay() {
        Display.getDefault().asyncExec(new Runnable() {
            final TmfChartTimeStampFormat tmfChartTimeStampFormat = new TmfChartTimeStampFormat(getXOffset());

            @Override
            public void run() {
                if (!fSwtChart.isDisposed()) {
                    for (Entry<String, double[]> entry : fSeriesValues.entrySet()) {
                        ILineSeries series = (ILineSeries) fSwtChart.getSeriesSet().getSeries(entry.getKey());
                        if (series != null) {
                            fSwtChart.getSeriesSet().deleteSeries(entry.getKey());
                        }
                        series = addSeries(entry.getKey());
                        series.setXSeries(fXValues);
                        series.setYSeries(entry.getValue());
                    }

                    IAxisTick xTick = fSwtChart.getAxisSet().getXAxis(0).getTick();
                    xTick.setFormat(tmfChartTimeStampFormat);

                    final double start = fXValues[0];
                    double end = (start == fXValues[799]) ? start + 1 : fXValues[799];
                    fSwtChart.getAxisSet().getXAxis(0).setRange(new Range(start, end));
                    fSwtChart.getAxisSet().getXAxis(0).adjustRange();
                    if (fMaxYValue != 0.0) {
                        fSwtChart.getAxisSet().getYAxis(0).setRange(new Range(0.0, fMaxYValue));
                    }
                    fSwtChart.redraw();
                }
            }
        });
    }

    /**
     * Create the series once the initialization of the viewer's data source is
     * done. Series do not need to be created before setting their values, but
     * if their appearance needs to be customized, this method is a good place
     * to do so. It is called only once per trace.
     */
    protected void createSeries() {

    }

}
