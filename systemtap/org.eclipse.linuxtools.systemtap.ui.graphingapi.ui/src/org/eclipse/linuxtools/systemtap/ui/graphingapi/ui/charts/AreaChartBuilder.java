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

/**
 * Builds bar chart.
 * 
 * @author Qi Liang
 */

public class AreaChartBuilder extends LineChartBuilder {

	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.charts.areachartbuilder";

    public AreaChartBuilder(Composite parent, int style, String title,IAdapter adapter) {
		super(parent, style, title, adapter);
    }
    
	@Override
	protected ISeries createChartISeries(int i) {
		ILineSeries series = (ILineSeries) super.createChartISeries(i);
		series.enableArea(true);
		return series;
	}
}
