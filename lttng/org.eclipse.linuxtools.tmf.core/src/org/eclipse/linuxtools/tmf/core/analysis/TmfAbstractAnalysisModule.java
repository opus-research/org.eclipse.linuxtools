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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Base class that analysis modules main class may extend. It provides default
 * behavior to some methods of the analysis module
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public abstract class TmfAbstractAnalysisModule implements IAnalysisModule {

    private String fName, fId, fIcon;
    private boolean fAutomatic = false;
    private ITmfTrace fTrace;
    private final Map<String, Object> fParameters = new HashMap<String, Object>();
    private final List<String> fParameterNames = new ArrayList<String>();
    private final List<IAnalysisOutput> fOutputs = new ArrayList<IAnalysisOutput>();

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
        if (!appliesToTraceType(trace.getClass())) {
            throw new TmfAnalysisException(Messages.TmfAbstractAnalysisModule_AnalysisDoesNotApply);
        }
        if (!canExecute(trace)) {
            throw new TmfAnalysisException(Messages.TmfAbstractAnalysisModule_AnalysisCannotExecute);
        }
        fTrace = trace;
        traceChanged();
    }

    /**
     * Reset all analysis data because the trace has changed
     */
    protected void traceChanged() {
        fFinishedLatch = new CountDownLatch(1);
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
        fParameters.put(name, value);
    }

    @Override
    public Object getParameter(String name) {
        return fParameters.get(name);
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
     * @return Whether the analysis was completed successfully or not
     */
    protected abstract boolean executeAnalysis();

    @Override
    public IStatus execute() {
        if (fTrace == null) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, String.format("No trace specified for analysis %s", getName())); //$NON-NLS-1$
        }
        /*
         * Actual analysis will be run on a separate thread
         */
        Thread analysisThread = new Thread("Analysis thread") { //$NON-NLS-1$
            @Override
            public void run() {
                fAnalysisCancelled = !executeAnalysis();
                fFinishedLatch.countDown();
            }
        };
        analysisThread.start();

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
