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

package org.eclipse.linuxtools.internal.tmf.ui.project.dialogs;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Offset dialog
 *
 * @author Matthew Khouzam
 *
 */
public class OffsetDialog extends Dialog {

    private Map<ITmfTrace, Long> result;
    private Shell shell;

    /**
     * Create the dialog.
     *
     * @param parent the parent shell
     * @param style the style
     */
    public OffsetDialog(Shell parent, int style) {
        super(parent, style);
        setText("Set Offsets"); //$NON-NLS-1$
    }

    /**
     * Open the dialog.
     *
     * @param traceMap
     *            the traces and their constant offsets
     *
     * @return the result
     */
    public Map<ITmfTrace, Long> open(Map<ITmfTrace, Long> traceMap) {
        createContents();
        Composite c = new Composite(shell, SWT.NONE);
        c.setLayout(new GridLayout(1, true));
        result = traceMap;
        for (Entry<ITmfTrace, Long> entry : traceMap.entrySet()) {
            @SuppressWarnings("null")
            OffsetWidget ow = new OffsetWidget(this, entry.getKey());
            ow.createControls(c);
        }
        c.pack();
        shell.open();
        shell.layout();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        shell = new Shell(getParent(), getStyle());
        shell.setSize(450, 300);
        shell.setText(getText());

    }

    /**
     * Get an offset for a trace
     * @param tmfTrace the trace
     * @return the offset
     */
    public Long getOffset(ITmfTrace tmfTrace) {
        return result.get(tmfTrace);
    }

    /**
     * Set the offset for a trace
     * @param trace the trace
     * @param fOffset the offset
     */
    public void setOffset(@NonNull ITmfTrace trace, @NonNull Long fOffset) {
        result.put(trace, fOffset);

    }

}
