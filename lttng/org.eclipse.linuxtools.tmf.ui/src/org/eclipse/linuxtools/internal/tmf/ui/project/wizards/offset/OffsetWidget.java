/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.offset;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Matthew Khouzam
 *
 */
public class OffsetWidget extends Composite {

        private final @NonNull String fTraceName;
    private @NonNull Long fOffset = new Long(0);
    private final OffsetWizardPage fParent;
    private final @NonNull ITmfTrace fTrace;

    private Text fOffsetText;
    private Label fTraceLabel;
    private Label fOffsetLabel;

    /**
     * Create an offset widget
     *
     * @param parent
     *            the parent
     * @param composite
     *            The composite that will house this widget
     * @param tmfTrace
     *            the trace
     */
    public OffsetWidget(@NonNull OffsetWizardPage parent, Composite composite, @NonNull ITmfTrace tmfTrace) {
        super(composite, SWT.None);
        fParent = parent;
        fTrace = tmfTrace;
        final String name = tmfTrace.getName();
        if (name == null) {
            throw new IllegalArgumentException();
        }
        fTraceName = name;
        final Long offset = fParent.getOffset(tmfTrace);
        if (offset != null) {
            fOffset = offset;
        }
    }

    /**
     * Create contents of the view part.
     *
     * @param parent
     *            the parent control
     */
    public void createControls(final Composite parent) {
        parent.setLayout(new GridLayout(5, false));

        fTraceLabel = new Label(parent, SWT.NONE);
        fTraceLabel.setText(fTraceName);
        new Label(parent, SWT.NONE);

        fOffsetText = new Text(parent, SWT.NONE);
        fOffsetText.setText(Long.toString(fOffset));
        fOffsetText.addModifyListener(new ModifyListener() {

            @SuppressWarnings("null")
            @Override
            public void modifyText(ModifyEvent e) {
                fOffset = Long.parseLong(fOffsetText.getText());
                fParent.setOffset(fTrace, fOffset);
                parent.pack();
            }
        });
        fOffsetLabel = new Label(parent, SWT.NONE);
        fOffsetLabel.setText("ns"); //$NON-NLS-1$
    }
}
