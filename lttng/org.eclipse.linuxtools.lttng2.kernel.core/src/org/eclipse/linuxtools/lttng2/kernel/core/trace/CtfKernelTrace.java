/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.trace;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.CtfKernelStateInput;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateChangeInput;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemQuerier;
import org.eclipse.linuxtools.tmf.core.statesystem.StateSystemManager;

/**
 * This is the specification of CtfTmfTrace for use with LTTng 2.x kernel
 * traces. It uses the CtfKernelStateInput to generate the state history.
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
public class CtfKernelTrace extends CtfTmfTrace {

    /**
     * State system ID
     * @since 2.0
     */
    public final static String stateID = "lttng-kernel"; //$NON-NLS-1$

    /**
     * The file name of the History Tree
     */
    public final static String HISTORY_TREE_FILE_NAME = "stateHistory.ht"; //$NON-NLS-1$

    /**
     * Direct reference to the kernel state system of this trace
     * @since 2.0
     */
    public IStateSystemQuerier kernelStateSystem = null;

    /**
     * Default constructor
     */
    public CtfKernelTrace() {
        super();
    }

    @Override
    public boolean validate(final IProject project, final String path) {
        CTFTrace temp;
        /*
         * Make sure the trace is openable as a CTF trace. We do this here
         * instead of calling super.validate() to keep the reference to "temp".
         */
        try {
            temp = new CTFTrace(path);
        } catch (CTFReaderException e) {
            return false;
        }

        /* Make sure the domain is "kernel" in the trace's env vars */
        String dom = temp.getEnvironment().get("domain"); //$NON-NLS-1$
        if (dom != null && dom.equals("\"kernel\"")) { //$NON-NLS-1$
            return true;
        }
        return false;
    }

    @Override
    protected void buildStateSystem() throws TmfTraceException {
        super.buildStateSystem();

        /* Set up the path to the history tree file we'll use */
        IResource resource = this.getResource();
        String supplDirectory = null;

        try {
            // get the directory where the history file will be stored.
            supplDirectory = resource.getPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER);
        } catch (CoreException e) {
            throw new TmfTraceException(e.toString(), e);
        }

        final File htFile = new File(supplDirectory + File.separator + HISTORY_TREE_FILE_NAME);
        final IStateChangeInput htInput = new CtfKernelStateInput(this);

        kernelStateSystem = StateSystemManager.loadStateHistory(htFile, htInput, false);
        stateSystems.put(stateID, kernelStateSystem);

    }

    /**
     * @return A direct reference to the kernel state system. Using this saves a
     *         hashmap lookup over doing .getStateSystem().get(stateID)
     * @since 2.0
     */
    public IStateSystemQuerier getKernelStateSystem() {
        return kernelStateSystem;
    }
}
