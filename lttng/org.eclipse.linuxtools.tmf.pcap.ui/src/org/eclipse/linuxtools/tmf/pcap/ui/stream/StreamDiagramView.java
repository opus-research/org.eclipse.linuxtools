/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.pcap.ui.stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * View that represents a packet stream as a sequence diagram.
 *
 * @author Vincent Perot
 */
public class StreamDiagramView extends TmfView {

    /**
     * The Stream Diagram View ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.pcap.ui.view.stream.diagram"; //$NON-NLS-1$

    private @Nullable Label fLabel;

    /**
     * Default constructor
     */
    public StreamDiagramView() {
        super(ID);
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        fLabel = new Label(parent, SWT.NONE);
        fLabel.setText("Coming in next version"); //$NON-NLS-1$
    }

    @Override
    public void setFocus() {
        if (fLabel != null) {
            fLabel.setFocus();
        }
    }

}
