package org.eclipse.linuxtools.tmf.pcap.ui.graph;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * View that allows the user to graph network-related graphs.
 *
 * @author Vincent Perot
 */
public class NetworkGraphView extends TmfView {

    /**
     * The network graph view ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.pcap.ui.view.graph"; //$NON-NLS-1$

    private @Nullable Label fLabel;

    /**
     * Default constructor.
     */
    public NetworkGraphView() {
        super(ID);
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        fLabel = new Label(parent, SWT.NONE);
        fLabel.setText("Coming in next version... Sorry Martin!"); //$NON-NLS-1$
    }

    @Override
    public void setFocus() {
        if (fLabel != null) {
            fLabel.setFocus();
        }
    }

}
