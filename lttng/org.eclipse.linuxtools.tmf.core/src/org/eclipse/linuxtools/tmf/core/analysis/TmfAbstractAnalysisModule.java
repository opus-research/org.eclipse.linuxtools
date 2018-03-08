/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.signal.TmfAnalysisCompletedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Base class that analysis modules main class may extend. It provides default
 * behavior to some methods of the analysis module
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public abstract class TmfAbstractAnalysisModule extends TmfComponent implements IAnalysisModule {

    private String fName, fId, fIcon, fTraceName;
    private boolean fAutomatic = false, fStarted = false;
    private ITmfTrace fTrace;
    private final Map<String, Object> fParameters = new HashMap<String, Object>();
    private final List<String> fParameterNames = new ArrayList<String>();
    private final List<IAnalysisOutput> fOutputs = new ArrayList<IAnalysisOutput>();
    private List<IAnalysisParameterProvider> fParameterProviders = new ArrayList<IAnalysisParameterProvider>();
    private Job fJob = null;

    private final Object syncObj = new Object();

    /*
     * This analysis is requested by someone, and should be executed as soon as
     * possible
     */
    private Boolean fToBeExecuted = false;

    /* Latch tracking if the analysis is completed or not */
    private CountDownLatch fFinishedLatch = new CountDownLatch(1);

    private boolean fAnalysisCancelled = false;

    @Override
    public boolean isAutomatic() {
        return fAutomatic;
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public void setName(String name) {
        fName = name;
    }

    @Override
    public void setId(String id) {
        fId = id;

    }

    @Override
    public String getId() {
        return fId;
    }

    @Override
    public void setAutomatic(boolean auto) {
        fAutomatic = auto;
    }

    @Override
    public void setIcon(String icon) {
        fIcon = icon;
    }

    @Override
    public String getIcon() {
        return fIcon;
    }

    @Override
    public void setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (trace.equals(fTrace)) {
            return;
        }
        /* Trace has changed, cancel current execution */
        cancel();

        /* Check that analysis can be executed */
        if (!appliesToTraceType(trace.getClass())) {
            throw new TmfAnalysisException(Messages.TmfAbstractAnalysisModule_AnalysisDoesNotApply);
        }
        if (!canExecute(trace)) {
            throw new TmfAnalysisException(Messages.TmfAbstractAnalysisModule_AnalysisCannotExecute);
        }
        fTrace = trace;
        traceChanged();

        /* Execute the analysis if it is set to be scheduled */
        synchronized (fToBeExecuted) {
            if (fToBeExecuted) {
                execute();
                fToBeExecuted = false;
            }
        }
    }

    @Override
    public void setTraceName(String traceName) {
        fTraceName = traceName;
    }

    /**
     * Reset all analysis data because the trace has changed
     */
    protected void traceChanged() {
        fFinishedLatch = new CountDownLatch(1);
        fStarted = false;
        /* Get the parameter providers for this trace */
        fParameterProviders = TmfAnalysisManager.getParameterProviders(this, getTrace());
        for (IAnalysisParameterProvider provider : fParameterProviders) {
            provider.registerModule(this);
        }
    }

    /**
     * Gets the trace
     *
     * @return The trace
     */
    protected ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public void addParameter(String name) {
        fParameterNames.add(name);
    }

    @Override
    public void setParameter(String name, Object value) throws RuntimeException {
        if (!fParameterNames.contains(name)) {
            throw new RuntimeException(String.format(Messages.TmfAbstractAnalysisModule_InvalidParameter, name, getName()));
        }
        synchronized (fParameters) {
            Object oldValue = fParameters.get(name);
            fParameters.put(name, value);
            if ((value != null) && !(value.equals(oldValue))) {
                parameterChanged(name);
            }
        }

    }



    @Override
    public void notifyParameterChanged(String name) {
        if (!fParameterNames.contains(name)) {
            throw new RuntimeException(String.format(Messages.TmfAbstractAnalysisModule_InvalidParameter, name, getName()));
        }
        synchronized (fParameters) {
            Object oldValue = fParameters.get(name);
            Object value = getParameter(name);
            if ((value != null) && !(value.equals(oldValue))) {
                parameterChanged(name);
            }
        }
    }

    /**
     * Used to indicate that a parameter value has been changed
     *
     * @param name
     *            The name of the modified parameter
     */
    protected void parameterChanged(String name) {

    }

    @Override
    public Object getParameter(String name) {
        Object paramValue = fParameters.get(name);
        /* The parameter is not set, maybe it can be provided by someone else */
        if ((paramValue == null) && (fTrace != null)) {
            for (IAnalysisParameterProvider provider : fParameterProviders) {
                paramValue = provider.getParameter(name);
                if (paramValue != null) {
                    break;
                }
            }
        }
        return paramValue;
    }

    @Override
    public List<IAnalysisModule> getExecutableModules() {
        List<IAnalysisModule> modules = new ArrayList<IAnalysisModule>();
        modules.add(this);
        return modules;
    }

    @Override
    public boolean appliesToTraceType(Class<? extends ITmfTrace> trace) {
        return true;
    }

    @Override
    public boolean canExecute(ITmfTrace trace) {
        return true;
    }

    /**
     * Actually executes the analysis itself
     *
     * @param monitor
     *            Progress monitor
     * @return Whether the analysis was completed successfully or not
     * @throws TmfAnalysisException
     *             Method may throw an analysis exception
     */
    protected abstract boolean executeAnalysis(final IProgressMonitor monitor) throws TmfAnalysisException;

    /**
     * Indicate the analysis has been canceled. It is abstract to force
     * implementing class to cleanup what they are running. This is called by
     * the job's canceling. It does not need to be called directly.
     */
    protected abstract void canceling();

    /**
     * Cancels the analysis if it is executing
     */
    public final void cancel() {
        synchronized (syncObj) {
            if (fJob != null) {
                fJob.cancel();
            }
            fStarted = false;
        }
    }

    private void execute() {

        synchronized (syncObj) {
            if (fStarted) {
                return;
            }
            fStarted = true;
        }

        /*
         * Actual analysis will be run on a separate thread
         */
        fJob = new Job(String.format("Running analysis %s", getName())) { //$NON-NLS-1$
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                try {
                    fAnalysisCancelled = !executeAnalysis(monitor);
                } catch (TmfAnalysisException e) {
                    Activator.logError("Error executing analysis with trace " + getTrace().getName(), e); //$NON-NLS-1$
                } finally {
                    synchronized (syncObj) {
                        monitor.done();
                        fStarted = false;
                        fJob = null;
                        fFinishedLatch.countDown();
                    }
                }
                if (!fAnalysisCancelled) {
                    broadcast(new TmfAnalysisCompletedSignal(TmfAbstractAnalysisModule.this, TmfAbstractAnalysisModule.this));
                    return Status.OK_STATUS;
                }
                return Status.CANCEL_STATUS;
            }

            @Override
            protected void canceling() {
                TmfAbstractAnalysisModule.this.canceling();
                fStarted = false;
            }

        };
        fJob.schedule();
    }

    @Override
    public IStatus schedule() {
        if ((fTrace == null) && (fTraceName == null)) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, String.format("No trace specified for analysis %s", getName())); //$NON-NLS-1$
        }

        /* We are waiting for a trace to be opened */
        synchronized (fToBeExecuted) {
            if (fTrace == null) {
                fToBeExecuted = true;
                return Status.OK_STATUS;
            }
        }
        execute();

        return Status.OK_STATUS;
    }

    @Override
    public List<IAnalysisOutput> getOutputs() {
        return Collections.unmodifiableList(fOutputs);
    }

    @Override
    public void registerOutput(IAnalysisOutput output) {
        fOutputs.add(output);
    }

    @Override
    public boolean waitForCompletion() {
        try {
            fFinishedLatch.await();
        } catch (InterruptedException e) {
            Activator.logError("Error while waiting for module completion", e); //$NON-NLS-1$
        }
        return !fAnalysisCancelled;
    }

    @Override
    public boolean waitForCompletion(IProgressMonitor monitor) {
        try {
            while (!fFinishedLatch.await(1, TimeUnit.MILLISECONDS)) {
                if (monitor.isCanceled()) {
                    fAnalysisCancelled = true;
                    return false;
                }
            }
        } catch (InterruptedException e) {
            Activator.logError("Error while waiting for module completion", e); //$NON-NLS-1$
        }
        return !fAnalysisCancelled;
    }

    /**
     * Signal handler for trace opening
     *
     * @param signal
     *            Trace opened signal
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        if (fTraceName == null) {
            return;
        }

        /* Is the opening trace the one that was requested? */
        if (signal.getTrace().getName().equals(fTraceName)) {
            try {
                setTrace(signal.getTrace());
            } catch (TmfAnalysisException e) {
                Activator.logError("Error setting the requested opened trace", e); //$NON-NLS-1$
            } finally {
                /* Cancel the request to trace opening */
                fTraceName = null;
            }
        }
    }

    /**
     * Returns a full help text to display
     *
     * @return Full help text for the module
     */
    protected String getFullHelpText() {
        return String.format(Messages.TmfAbstractAnalysisModule_AnalysisModule, getName());
    }

    /**
     * Gets a short help text, to display as header to other help text
     *
     * @param trace
     *            The trace to show help for
     *
     * @return Short help text describing the module
     */
    protected String getShortHelpText(ITmfTrace trace) {
        return String.format(Messages.TmfAbstractAnalysisModule_AnalysisForTrace, getName(), trace.getName());
    }

    /**
     * Gets the help text specific for a trace whose type does not apply to the
     * analysis
     *
     * @param trace
     *            The trace we tried to apply the analysis to
     * @return Help text
     */
    protected String getTraceNotApplyHelpText(ITmfTrace trace) {
        return Messages.TmfAbstractAnalysisModule_AnalysisDoesNotApply;
    }

    /**
     * Gets the help text specific for a trace who does not have required
     * characteristics for module to execute
     *
     * @param trace
     *            The trace to apply the analysis to
     * @return Help text
     */
    protected String getTraceCannotExecuteHelpText(ITmfTrace trace) {
        return Messages.TmfAbstractAnalysisModule_AnalysisCannotExecute;
    }

    @Override
    public String getHelpText() {
        return getFullHelpText();
    }

    @Override
    public String getHelpText(ITmfTrace trace) {
        if (trace == null) {
            return getHelpText();
        }
        String text = getShortHelpText(trace);
        if (!appliesToTraceType(trace.getClass())) {
            text = text + getTraceNotApplyHelpText(trace);
        }
        if (!canExecute(trace)) {
            text = text + getTraceCannotExecuteHelpText(trace);
        }
        return text;
    }

}
