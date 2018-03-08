package org.eclipse.linuxtools.systemtap.graphingapi.ui.charts;

import java.text.MessageFormat;

import org.eclipse.linuxtools.dataviewers.piechart.PieChart;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.ISeries;

/**
 * @since 3.0
 */
public class PieChartMouseMoveListener extends ToolTipChartMouseMoveListener {
	public PieChartMouseMoveListener(PieChart chart, Composite parent) {
		super(chart, parent);
	}

	@Override
	public void mouseMove(MouseEvent e) {
		super.mouseMove(e);
		ISeries[] allSeries = chart.getSeriesSet().getSeries();
		int numPies = allSeries.length > 0 ? allSeries[0].getXSeries().length : 0;
		int pieIndex = 0, sliceIndex = -1;
		for (; pieIndex < numPies; pieIndex++) {
			sliceIndex = ((PieChart) chart).getSliceIndexFromPosition(pieIndex, e.x, e.y);
			if (sliceIndex != -1) {
				break;
			}
		}
		if (sliceIndex != -1) {
			setTextTip(MessageFormat.format(Messages.PieChartBuilder_ToolTipCoords,
					chart.getAxisSet().getXAxis(0).getTitle().getText(),
					allSeries[sliceIndex].getId(), allSeries[sliceIndex].getXSeries()[pieIndex],
					Math.round(((PieChart) chart).getSlicePercent(pieIndex, sliceIndex) * 100.0) / 100.0));
		} else {
			tipShell.setVisible(false);
		}
	}
}