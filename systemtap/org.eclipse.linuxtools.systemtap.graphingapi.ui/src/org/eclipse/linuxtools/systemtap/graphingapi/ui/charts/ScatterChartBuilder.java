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
package org.eclipse.linuxtools.systemtap.graphingapi.ui.charts;

import org.eclipse.linuxtools.systemtap.graphingapi.core.adapters.IAdapter;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.LineStyle;

/**
 * Builds bar chart.
 *
 * @author Qi Liang
 */

public class ScatterChartBuilder extends LineChartBuilder {
	public static final String ID = "org.eclipse.linuxtools.systemtap.graphingapi.ui.charts.scatterchartbuilder"; //$NON-NLS-1$

    public ScatterChartBuilder(Composite parent, int style, String title,IAdapter adapter) {
		super(parent, style, title, adapter);
    }

	@Override
	protected ISeries createChartISeries(int i) {
		ILineSeries series = (ILineSeries)super.createChartISeries(i);
		series.setSymbolColor(COLORS[i % COLORS.length]);
		series.setLineStyle(LineStyle.NONE);
		return series;
	}
}
