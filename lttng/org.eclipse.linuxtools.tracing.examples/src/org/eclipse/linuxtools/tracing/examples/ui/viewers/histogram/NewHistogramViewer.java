/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Bernd Hufmann - Updated to new TMF chart framework
 *******************************************************************************/
package org.eclipse.linuxtools.tracing.examples.ui.viewers.histogram;

import java.util.Arrays;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.statistics.ITmfStatistics;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.viewers.xycharts.TmfBarChartViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.xycharts.TmfHistogramTooltipProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.IAxis;
import org.swtchart.ISeries;
import org.swtchart.LineStyle;

/**
 * Histogram Viewer implementation based on TmfBarChartViewer.
 * 
 * @author Alexandre Montplaisir
 * @author Bernd Hufmann
 */
public class NewHistogramViewer extends TmfBarChartViewer {

	public NewHistogramViewer(Composite parent) {
		super(parent, null, null, null, TmfXYChartViewer.DEFAULT_PROVIDERS,
				TmfBarChartViewer.MINIMUM_BAR_WIDTH);
		// Replace default tool tip provider
		setTooltipProvider(new TmfHistogramTooltipProvider(this));

		IAxis xAxis = fSwtChart.getAxisSet().getXAxis(0);
		IAxis yAxis = fSwtChart.getAxisSet().getYAxis(0);

		/* Hide the grid */
		xAxis.getGrid().setStyle(LineStyle.NONE);
		yAxis.getGrid().setStyle(LineStyle.NONE);

		/* Hide the legend */
		fSwtChart.getLegend().setVisible(false);

		addSeries("Number of events", Display.getDefault().getSystemColor( //$NON-NLS-1$
				SWT.COLOR_BLUE).getRGB());
	}

	@Override
	protected void readData(final ISeries series, final long start,
			final long end, final int nb) {
		if (fTrace != null) {
			final double y[] = new double[nb];

			Thread thread = new Thread("Histogram viewer update") { //$NON-NLS-1$
				@Override
				public void run() {
					double x[] = getXAxis(start, end, nb);
					final long yLong[] = new long[nb];
					Arrays.fill(y, 0.0);

					/* Add the values for each trace in the experiment */
					if (fTrace instanceof TmfExperiment) {
						final TmfExperiment exp = (TmfExperiment) fTrace;
						for (ITmfTrace trace : exp.getTraces()) {
							ITmfStatistics stats = trace.getStatistics();
							List<Long> values = stats.histogramQuery(start,
									end, nb);

							for (int i = 0; i < nb; i++) {
								yLong[i] += values.get(i);
							}
						}
					} else {
						ITmfStatistics stats = fTrace.getStatistics();
						List<Long> values = stats
								.histogramQuery(start, end, nb);

						for (int i = 0; i < nb; i++) {
							yLong[i] += values.get(i);
						}

					}
					fYOffset = 0;
					for (int i = 0; i < nb; i++) {
						y[i] += yLong[i] - fYOffset; /*
													 * casting from long to
													 * double
													 */
					}
					/* Update the viewer */
					drawChart(series, x, y);
				}
			};
			thread.start();
		}
		return;
	}
}
