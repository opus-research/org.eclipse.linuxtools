/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Matthew Khouzam - Improved validation
 *   Jean-Christian Kouamé - added cumulCpuUsage provider for LttngTrace
 ******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.trace;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Activator;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.LTTngCpuUsageStateProvider;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.CumulCpuUsageProvider;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.LttngKernelStateProvider;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemFactory;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;

/**
 * This is the specification of CtfTmfTrace for use with LTTng 2.x kernel
 * traces. It uses the CtfKernelStateInput to generate the state history.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class LttngKernelTrace extends CtfTmfTrace {

    private ITmfStateSystem cpuUsageSS;

    private ITmfStateSystem cumulCpuUsageSS;

    /**
     * The file name of the CPU Tree
     *
     * @since 3.0
     */
    public final static String CPU_TREE_FILE_NAME = "cpuHistory.ht"; //$NON-NLS-1$

    /**
     * The file name of the CPU Tree
     *
     * @since 3.0
     */
    public final static String CUMUL_CPU_TREE_FILE_NAME = "cumulCpuHistory.ht"; //$NON-NLS-1$

    /**
     * The file name of the History Tree
     */
    public final static String HISTORY_TREE_FILE_NAME = "stateHistory.ht"; //$NON-NLS-1$

    /**
     * ID of the state system we will build
     *
     * @since 2.0
     * */
    public static final String STATE_ID = "org.eclipse.linuxtools.lttng2.kernel"; //$NON-NLS-1$

    /**
     * ID of the state system we will build
     *
     * @since 3.0
     * */
    private static final String CPU_ID = "org.eclipse.linuxtools.lttng2.cpu"; //$NON-NLS-1$

    private static final String CUMUL_CPU_ID = "org.eclipse.linuxtool.lttng2.cumulCpu"; //$NON-NLS-1$

    /**
     * Default constructor
     */
    public LttngKernelTrace() {
        super();
    }

    /**
     * @since 2.0
     */
    @Override
    public IStatus validate(final IProject project, final String path) {
        CTFTrace temp;
        IStatus validStatus;
        /*
         * Make sure the trace is openable as a CTF trace. We do this here
         * instead of calling super.validate() to keep the reference to "temp".
         */
        try {
            temp = new CTFTrace(path);
        } catch (CTFReaderException e) {
            validStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString(), e);
            return validStatus;
        } catch (NullPointerException e) {
            validStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString(), e);
            return validStatus;
        }

        /* Make sure the domain is "kernel" in the trace's env vars */
        String dom = temp.getEnvironment().get("domain"); //$NON-NLS-1$
        temp.dispose();
        if (dom != null && dom.equals("\"kernel\"")) { //$NON-NLS-1$
            return Status.OK_STATUS;
        }
        validStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngKernelTrace_DomainError);
        return validStatus;
    }

    /**
     * @since 3.0
     */
    @Override
    protected IStatus buildStateSystem() {
        super.buildStateSystem();

        /* Build the state system specific to LTTng kernel traces */
        String directory = TmfTraceManager.getSupplementaryFileDir(this);
        final File htFile = new File(directory + HISTORY_TREE_FILE_NAME);
        final ITmfStateProvider htInput = new LttngKernelStateProvider(this);

        try {
            ITmfStateSystem ss = TmfStateSystemFactory.newFullHistory(htFile, htInput, false);
            fStateSystems.put(STATE_ID, ss);
        } catch (TmfTraceException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
        }

        final File cpuFile = new File(directory + CPU_TREE_FILE_NAME);
        final ITmfStateProvider cpuInput = new LTTngCpuUsageStateProvider(this);
        try {
            this.cpuUsageSS = TmfStateSystemFactory.newFullHistory(cpuFile, cpuInput, false);
            fStateSystems.put(CPU_ID, cpuUsageSS);
        } catch (TmfTraceException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
        }

        final File cumulCpuFile = new File(directory + CUMUL_CPU_TREE_FILE_NAME);
        final ITmfStateProvider cumulCpuProvider = new CumulCpuUsageProvider(this);
        try {
            this.cumulCpuUsageSS = TmfStateSystemFactory.newFullHistory(cumulCpuFile, cumulCpuProvider, false);
            fStateSystems.put(CUMUL_CPU_ID, cumulCpuUsageSS);
        } catch (TmfTraceException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
        }

        return Status.OK_STATUS;
    }

    /**
     *
     * @return the CPU usage state system
     * @since 3.0
     */
    public ITmfStateSystem getCPUUsageStateSystem() {
        return cpuUsageSS;
    }

    /**
     * @return the cumulative CPU usage stateSystem
     * @since 3.0
     */
    public ITmfStateSystem getCumulCPUUsageSS() {
        return cumulCpuUsageSS;
    }
}
