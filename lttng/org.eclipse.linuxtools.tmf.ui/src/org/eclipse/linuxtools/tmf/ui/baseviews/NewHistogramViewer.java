package org.eclipse.linuxtools.tmf.ui.baseviews;

import java.util.Arrays;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.statistics.ITmfStatistics;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.swtchart.ISeries;

public class NewHistogramViewer extends TmfBarChartViewer {

    public NewHistogramViewer(Composite parent) {
        super(parent, null, null, null);
        addSeries("Number of events", new RGB(0, 255, 0));
    }

    @Override
    protected void readData(final ISeries series, final long start, final long end, final int nb) {
        final TmfExperiment exp = TmfExperiment.getCurrentExperiment();
        final double y[] = new double[nb];

        Thread thread = new Thread("Histogram viewer update") {
            @Override
            public void run() {
                double x[] = getXAxis(start, end, nb);
                Arrays.fill(y, 0.0);

                /* Add the values for each trace in the experiment */
                for (ITmfTrace trace : exp.getTraces()) {
                    ITmfStatistics stats = trace.getStatistics();
                    List<Long> values = stats.histogramQuery(start, end, nb);

                    /* Convert the long's to double's and put them in the returned array */
                    for (int i = 0; i < nb; i++) {
                        y[i] += values.get(i); /* casting from long to double */
                    }
                }
                /* Update the viewer */
                drawChart(series, x, y);
            }
        };
        thread.start();
        return;
    }

    @Override
    public Control getControl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub
    }

}
