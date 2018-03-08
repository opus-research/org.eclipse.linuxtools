package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.statistics;

import org.eclipse.linuxtools.internal.lttng2.kernel.ui.viewers.statistics.StateSystemStatisticsViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.TmfStatisticsViewer;
import org.eclipse.linuxtools.tmf.ui.views.statistics.TmfStatisticsView;

/**
 * View that replaces the default statistics view from TMF. It uses as the
 * global tab the statistics viewer that uses the state system.
 *
 * @author Mathieu Denis
 */
public class StateSystemStatisticsView extends TmfStatisticsView {

    @Override
    protected Class<? extends TmfStatisticsViewer> getGlobalViewerClass() {
        return StateSystemStatisticsViewer.class;
    }
}
