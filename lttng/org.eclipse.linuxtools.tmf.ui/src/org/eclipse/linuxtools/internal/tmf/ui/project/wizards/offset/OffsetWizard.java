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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Offset dialog
 *
 * @author Matthew Khouzam
 *
 */
public class OffsetWizard extends Wizard {

    private final Map<ITmfTrace, Long> fResults;
    private final Map<ITmfTrace, Long> fResultsTemp;
    private OffsetWizardPage fPage;

    /**
     * Create the dialog.
     * @param results The results to populate
     */
    public OffsetWizard( Map<ITmfTrace, Long> results) {
        super();
        setNeedsProgressMonitor(false);
        fResults = results;
        fResultsTemp  = new HashMap<>();
        fResultsTemp.putAll(fResults);
    }

    @Override
    public void addPages() {
        fPage = new OffsetWizardPage(fResultsTemp);
        addPage(fPage);
    }

    @Override
    public boolean performFinish() {
        fResults.putAll(fResultsTemp);
        return true;
    }

    /**
     * Get an offset for a trace
     * @param tmfTrace the trace
     * @return the offset
     */
    public Long getOffset(ITmfTrace tmfTrace) {
        return fResultsTemp.get(tmfTrace);
    }

    /**
     * Set the offset for a trace
     * @param trace the trace
     * @param fOffset the offset
     */
    public void setOffset(@NonNull ITmfTrace trace, @NonNull Long fOffset) {
        fResultsTemp.put(trace, fOffset);

    }

}
