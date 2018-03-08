package org.eclipse.linuxtools.tmf.ui.baseviews;

import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.widgets.Composite;

public class NewHistogramView extends TmfView {

    public static final String ID = "org.eclipse.linuxtools.tmf.ui.baseviews.NewHistogramView"; //$NON-NLS-1$

    private TmfBarChartViewer chart;

    public NewHistogramView() {
        super(ID);
    }

    @Override
    public void createPartControl(Composite parent) {
        chart = new NewHistogramViewer(parent);
    }

    @Override
    public void setFocus() {

    }

    public TmfBarChartViewer getViewer() {
        return chart;
    }

}
