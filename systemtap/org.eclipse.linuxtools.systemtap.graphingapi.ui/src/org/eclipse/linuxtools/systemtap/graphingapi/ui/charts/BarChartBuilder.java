/****************************************************************
 * Copyright (c) 2006-2013 IBM Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - initial API and implementation
 *     Red Hat - ongoing maintenance
 *
 ****************************************************************
 */
package org.eclipse.linuxtools.systemtap.graphingapi.ui.charts;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.linuxtools.systemtap.graphingapi.core.adapters.IAdapter;

import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;

/**
 * Builds bar chart.
 * 
 * @author Qi Liang
 */

public class BarChartBuilder extends AbstractChartWithAxisBuilder {
	public static final String ID = "org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.barchartbuilder"; //$NON-NLS-1$

    public BarChartBuilder(Composite parent, int style, String title, IAdapter adapter) {
		super(adapter, parent, style, title);
    }
    
	@Override
	protected ISeries createChartISeries(int i) {
		IBarSeries series = (IBarSeries)chart.getSeriesSet().
			createSeries(SeriesType.BAR, adapter.getLabels()[i+1]);
		series.setBarColor(COLORS[i % COLORS.length]);
		return series;
	}

	@Override
	protected void buildXAxis() {
		super.buildXAxis();
		chart.getAxisSet().getXAxis(0).enableCategory(true);
	}

	@Override
	protected void buildXSeries() {
		Object data[][] = adapter.getData();
		if (data == null || data.length == 0)
			return;

		int start = 0, len = Math.min(this.maxItems, data.length), leny = data[0].length-1;
		if (this.maxItems < data.length) {
			start = data.length - this.maxItems;
		}

		String[] all_valx = new String[len];
		Double[][] all_valy = new Double[leny][len];

		ISeries allSeries[] = chart.getSeriesSet().getSeries();
		for (int i = 0; i < len; i++) {
			Object label = data[start + i][0];
			if (label != null) {
				all_valx[i] = label.toString();
				for (int j = 1; j < leny + 1; j++) {
					Double val = getDoubleValue(data[start + i][j]);
					if (val != null) {
						all_valy[j-1][i] = val;
					} else {
						all_valy[j-1][i] = 0.0;
					}
				}
			}
		}

		String valx[] = new String[len];
		int len_trim = 0;
		for (int i = 0; i < len; i++) {
			if (all_valx[i] != null) {
				valx[len_trim] = all_valx[i];
				len_trim++;
			}
		}
		String[] valx_trim = new String[len_trim];
		for (int i = 0; i < len_trim; i++) {
			valx_trim[i] = valx[i];
		}

		for (int i = 0; i < leny; i++) {
			ISeries series;
			if (i >= allSeries.length) {
				series = createChartISeries(i);
			} else {
				series = chart.getSeriesSet().getSeries()[i];
			}

			double[] valy = new double[len];
			int leny_trim = 0;
			for (int j = 0; j < len; j++) {
				if (all_valy[i][j] != null) {
					valy[leny_trim] = all_valy[i][j].doubleValue();
					leny_trim++;
				}
			}
			double[] valy_trim = new double[leny_trim];
			for (int j = 0; j < leny_trim; j++) {
				valy_trim[j] = valy[j];
			}

			series.setYSeries(valy_trim);
		}

		giveUniqueNames(valx_trim, len_trim);
		chart.getAxisSet().getXAxis(0).setCategorySeries(valx_trim);

		chart.getAxisSet().adjustRange();
		chart.redraw();
	}

	@Override
	public void updateDataSet() {
		buildXSeries();
	}
}
