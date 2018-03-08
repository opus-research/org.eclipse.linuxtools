/*******************************************************************************
 * Copyright (c) 2013 Kalray
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xavier Raynaud - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.tmf.ui.graph.view;

import org.eclipse.linuxtools.internal.tmf.ui.graph.chart.InteractiveChart;
import org.eclipse.linuxtools.tmf.ui.graph.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.LineStyle;

/**
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class View extends ViewPart {

    public static final String ID = "org.eclipse.linuxtools.tmf.ui.graph.view"; //$NON-NLS-1$

    private Chart fChart;

    public View() {
    }

    @Override
    public void createPartControl(Composite parent) {
        fChart = new InteractiveChart(parent, SWT.NONE);
    }

    @Override
    public void setFocus() {
        fChart.setFocus();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        fChart.dispose();
    }

    public static void displayPlottingView(final String title, final String xAxisName, final String yAxisName,
            final int length, double[] xSerieData, double[] ySerieData) {
        final double[] _xSerieData = new double[length];
        final double[] _ySerieData = new double[length];
        System.arraycopy(xSerieData, 0, _xSerieData, 0, length);
        System.arraycopy(ySerieData, 0, _ySerieData, 0, length);
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                displayPlottingViewImpl(title, xAxisName, yAxisName, _xSerieData, _ySerieData);
            }
        });

    }

    private static void displayPlottingViewImpl(String title, String xAxisName, String yAxisName, double[] xSerieData,
            double[] ySerieData) {
        try {
            View v = (View) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ID);
            v.setSeries(title, xAxisName, yAxisName, xSerieData, ySerieData);
        } catch (PartInitException _) {
            Activator.getDefault().getLog().log(_.getStatus());
        }
    }

    private void setSeries(String title, String xAxisName, String yAxisName, double[] xSerieData, double[] ySerieData) {
        ISeriesSet seriesSet = fChart.getSeriesSet();
        fChart.getAxisSet().getXAxis(0).getTitle().setText(xAxisName);
        fChart.getAxisSet().getYAxis(0).getTitle().setText(yAxisName);
        fChart.getTitle().setText(title);
        for (ISeries s : seriesSet.getSeries()) {
            seriesSet.deleteSeries(s.getId());
        }
        ILineSeries scatterSeries = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, "scatter series"); //$NON-NLS-1$
        scatterSeries.setLineStyle(LineStyle.NONE);
        scatterSeries.setXSeries(xSerieData);
        scatterSeries.setYSeries(ySerieData);
        scatterSeries.setSymbolSize(1);
        fChart.redraw();
        fChart.getAxisSet().adjustRange();
    }
}
