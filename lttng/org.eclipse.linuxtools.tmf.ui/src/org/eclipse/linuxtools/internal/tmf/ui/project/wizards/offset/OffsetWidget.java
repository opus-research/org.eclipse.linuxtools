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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;

/**
 * @author Matthew Khouzam
 *
 */
public class OffsetWidget extends Composite {

    private static final long NANO_PER_MILLI = 1000000;
    private static final int SEC_PER_MIN = 60;
    private static final int MILLI_PER_SEC = 1000;
    private static final int MIN_PER_HOUR = 60;
    private static final int MILLI_PER_DAY = 24 * MILLI_PER_SEC * SEC_PER_MIN * MIN_PER_HOUR;
    private final @NonNull String fTraceName;
    private @NonNull Long fOffset = new Long(0);
    private final OffsetWizardPage fParent;
    private final @NonNull ITmfTrace fTrace;

    private Slider fOffsetSlider;
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
        parent.setLayout(new GridLayout(4, false));

        fTraceLabel = new Label(parent, SWT.NONE);
        fTraceLabel.setText(fTraceName);
        new Label(parent, SWT.NONE);

        fOffsetSlider = new Slider(parent, SWT.NONE);
        fOffsetSlider.setValues(offsetToSlider(), 0, 2 * MILLI_PER_DAY, MIN_PER_HOUR * MILLI_PER_SEC * SEC_PER_MIN, MILLI_PER_SEC, MILLI_PER_SEC * SEC_PER_MIN);
        fOffsetSlider.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                final int selection = fOffsetSlider.getSelection();
                fOffset = new Long((selection - MILLI_PER_DAY) * NANO_PER_MILLI);
                fOffsetLabel.setText(Long.toString(fOffset));
                fParent.setOffset(fTrace, fOffset);
                parent.pack();
            }
        });
        fOffsetLabel = new Label(parent, SWT.NONE);
        fOffsetLabel.setText(Long.toString(fOffset));
    }

    private int offsetToSlider() {
        return (int) (fOffset.longValue() / NANO_PER_MILLI) + MILLI_PER_DAY;
    }
}
