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

import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Base class that analysis modules main class may extend. It provides default
 * behavior to some methods of the analysis module
 *
 * @since 3.0
 */
public abstract class TmfAbstractAnalysisModule implements IAnalysisModule {

    private String fName, fId, fIcon;
    private boolean fAutomatic = false;
    private ITmfTrace fTrace;
    private final Map<String, Object> fParameters = new HashMap<String, Object>();
    private final List<String> fParameterNames = new ArrayList<String>();
    private final List<IAnalysisOutput> fOutputs = new ArrayList<IAnalysisOutput>();

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
            throw new TmfAnalysisException("Analysis does not apply to trace type"); //$NON-NLS-1$
        }
        if (!canExecute(trace)) {
            throw new TmfAnalysisException("Cannot perform analysis for the given trace, it is missing requirements"); //$NON-NLS-1$
        }
        fTrace = trace;
        traceChanged();
    }

    /**
     * Reset all analysis data because the trace has changed
     */
    protected void traceChanged() {

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
            throw new RuntimeException(String.format("Parameter %s is not valid for analysis module %s", name, getName())); //$NON-NLS-1$
        }
        fParameters.put(name, value);
    }

    @Override
    public Object getParameter(String name) {
        return fParameters.get(name);
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
     * @return The termination status
     */
    protected abstract boolean executeAnalysis();

    @Override
    public boolean execute() {
        if (fTrace == null) {
            return false;
        }
        return executeAnalysis();
    }


    @Override
    public List<IAnalysisOutput> getOutputs() {
        return Collections.unmodifiableList(fOutputs);
    }

    @Override
    public void registerOutput(IAnalysisOutput output) {
        fOutputs.add(output);
    }

    /**
     * Returns a full help text to display
     *
     * @return Full help text for the module
     */
    protected String getFullHelpText() {
        return "Analysis module: " + getName(); //$NON-NLS-1$
    }

    /**
     * Gets a short help text, to display as header to other help text
     * @param trace The trace to show help for
     *
     * @return Short help text describing the module
     */
    protected String getShortHelpText(ITmfTrace trace) {
        return "Analysis module: " + getName() + " for trace " + trace.getName(); //$NON-NLS-1$ //$NON-NLS-2$
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
        return "Cannot perform the analysis on this trace because the trace is of the wrong type"; //$NON-NLS-1$
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
        return "Cannot perform the analysis on this trace because the trace does not have the required characteristics"; //$NON-NLS-1$
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
