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

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Matthew Khouzam
 *
 */
public class OffsetWizardPage extends WizardPage {

    private static final String ID = "org.eclipse.linuxtools.internal.tmf.ui.project.wizards.OffsetWizardPage";

    private final Map<ITmfTrace, Long> traceMap;

    /**
     * Constructor
     *
     * @param result
     *            the map of traces to offset
     */
    public OffsetWizardPage(Map<ITmfTrace, Long> result) {
        super(ID);
        traceMap = result;
        setTitle("Offset traces");
        setDescription("Offset traces to make the time make more sense");
    }

    @Override
    public void createControl(Composite parent) {
        Composite c = new Composite(parent, SWT.NONE);
        c.setLayout(new GridLayout(1, true));
        for (Entry<ITmfTrace, Long> entry : traceMap.entrySet()) {
            @SuppressWarnings("null")
            OffsetWidget ow = new OffsetWidget(this, c, entry.getKey());
            ow.createControls(c);
        }
        c.pack();
        setControl(c);
    }

    Long getOffset(ITmfTrace t) {
        return traceMap.get(t);
    }

    void setOffset(ITmfTrace t, Long value) {
        traceMap.put(t, value);
    }
}
