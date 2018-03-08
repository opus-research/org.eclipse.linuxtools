package org.eclipse.linuxtools.tmf.ui.baseviews;

import java.util.Arrays;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.statistics.ITmfStatistics;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.editors.ITmfTraceEditor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.swtchart.ISeries;

public class NewHistogramViewer extends TmfBarChartViewer {

    public NewHistogramViewer(Composite parent) {
        super(parent, null, null, null);
        addSeries("Number of events", new RGB(0, 255, 0));
    }

    @Override
    protected void readData(final ISeries series, final long start, final long end, final int nb) {
//        final TmfExperiment exp = TmfExperiment.getCurrentExperiment();

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return;
        }

        // Check if we are in the Project View
        IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            return;
        }

        IEditorPart editor = page.getActiveEditor();
        if (editor instanceof ITmfTraceEditor) {
            final ITmfTrace tmfTrace = ((ITmfTraceEditor) editor).getTrace();
            if (tmfTrace != null) {
                final double y[] = new double[nb];

                Thread thread = new Thread("Histogram viewer update") {
                    @Override
                    public void run() {
                        double x[] = getXAxis(start, end, nb);
                        Arrays.fill(y, 0.0);

                        /* Add the values for each trace in the experiment */
                        if (tmfTrace instanceof TmfExperiment) {
                            final TmfExperiment exp = (TmfExperiment)tmfTrace;
                            for (ITmfTrace trace : exp.getTraces()) {
                                ITmfStatistics stats = trace.getStatistics();
                                List<Long> values = stats.histogramQuery(start, end, nb);

                                /* Convert the long's to double's and put them in the returned array */
                                for (int i = 0; i < nb; i++) {
                                    y[i] += values.get(i); /* casting from long to double */
                                }
                            }
                        } else {
                            ITmfStatistics stats = tmfTrace.getStatistics();
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

            }
        }
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
