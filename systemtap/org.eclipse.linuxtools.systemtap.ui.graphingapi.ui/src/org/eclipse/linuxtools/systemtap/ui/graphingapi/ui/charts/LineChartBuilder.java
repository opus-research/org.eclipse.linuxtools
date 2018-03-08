/****************************************************************
 * Copyright (c) 2006-2013 IBM Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - initial API and implementation
 *
 ****************************************************************
 */
package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.adapters.IAdapter;

import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;

/**
 * Builds bar chart.
 * 
 * @author Qi Liang
 */

public class LineChartBuilder extends AbstractChartWithAxisBuilder {

	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.linechartbuilder";

    public LineChartBuilder(Composite parent, int style, String title,IAdapter adapter) {
		super(adapter, parent, style, title);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.examples.chart.widget.AbstractChartBuilder#buildChart()
     */
    @Override
	protected void createChart() {
		super.createChart();
    }

	@Override
	public void updateDataSet() {
		buildXSeries();
	}

	@Override
	protected ISeries createChartISeries(int i) {
		ILineSeries series = (ILineSeries) chart.getSeriesSet().
			createSeries(SeriesType.LINE, adapter.getLabels()[i+1]);
		series.setSymbolColor(COLORS[i % COLORS.length]);
		series.setLineColor(COLORS[i % COLORS.length]);
		series.setLineStyle(LineStyle.SOLID);
		return series;
	}
}
