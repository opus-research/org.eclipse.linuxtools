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
import java.util.List;

import org.eclipse.linuxtools.internal.tmf.core.analysis.TmfAnalysisType;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

/**
 * Wrapper around {@link IAnalysisModule} to grant access to some methods, but
 * make sure it is not an executable module
 *
 * @author Geneviève Bastien
 */
public class TmfAnalysisModuleHelper {

    private final IAnalysisModule fModule;
    private List<TmfAnalysisModuleHelper> fExecutableModules = null;
    /**
     * If the module comes from an executable module instead of a configuration
     * element, the origin module is the parent of the executable one
     */
    private IAnalysisModule fOriginModule;

    /**
     * Constructor
     *
     * @param module
     *            The {@link IAnalysisModule} represented by this helper
     */
    public TmfAnalysisModuleHelper(IAnalysisModule module) {
        fModule = module;
    }

    // ----------------------------------------
    // Wrappers to {@link IAnalysisModule} methods
    // ----------------------------------------

    /**
     * Wraps the results of {@link IAnalysisModule#getExecutableModules()} into
     * module helpers
     *
     * @return List of executable module helpers
     */
    List<TmfAnalysisModuleHelper> getExecutableModules() {
        if (fExecutableModules == null) {
            fExecutableModules = new ArrayList<TmfAnalysisModuleHelper>();
            List<IAnalysisModule> modules = fModule.getExecutableModules();
            for (IAnalysisModule module : modules) {
                TmfAnalysisModuleHelper submodule = new TmfAnalysisModuleHelper(module);
                submodule.fOriginModule = fModule;
                fExecutableModules.add(submodule);
            }
        }
        return fExecutableModules;
    }

    /**
     * Gets the id of the analysis module
     *
     * @return The id of the module
     */
    public String getId() {
        return fModule.getId();
    }

    /**
     * Gets the name of the analysis module
     *
     * @return The id of the module
     */
    public String getName() {
        return fModule.getName();
    }

    /**
     * Gets whether the analysis should be run automatically at trace opening
     *
     * @return true if analysis is to be run automatically
     */
    public boolean isAutomatic() {
        return fModule.isAutomatic();
    }

    /**
     * Wrapper to the {@link IAnalysisModule#getHelpText(ITmfTrace)} method
     *
     * @param trace
     *            The trace this analysis applies to
     * @return The help text
     */
    public String getHelpText(ITmfTrace trace) {
        return fModule.getHelpText(trace);
    }

    /**
     * Wrapper to the {@link IAnalysisModule#getHelpText()} method
     *
     * @return The help text
     */
    public String getHelpText() {
        return fModule.getHelpText();
    }

    /**
     * Wrapper to the {@link IAnalysisModule#getIcon()} method
     *
     * @return The icon path
     */
    public String getIcon() {
        return fModule.getIcon();
    }

    /**
     * Wrapper to the {@link IAnalysisModule#getBundle()} method
     *
     * @return The bundle of this analysis
     */
    public Bundle getBundle() {
        return fModule.getBundle();
    }

    /**
     * Wrapper to the {@link IAnalysisModule#getOutputs()} method
     *
     * @return The list of analysis outputs
     */
    public List<IAnalysisOutput> getOutputs() {
        return fModule.getOutputs();
    }

    /**
     * Wrapper to the {@link IAnalysisModule#appliesToTraceType(Class)} method
     *
     * @param traceclass
     *            The trace to analyze
     * @return whether the analysis applies
     */
    public boolean appliesToTraceType(Class<? extends ITmfTrace> traceclass) {
        return fModule.appliesToTraceType(traceclass);
    }

    /**
     * Wrapper to the {@link IAnalysisModule#canExecute(ITmfTrace)} method
     *
     * @param trace
     *            The trace to analyze
     * @return Whether the analysis can be executed
     */
    public boolean canExecute(ITmfTrace trace) {
        return fModule.canExecute(trace);
    }

    /**
     * Returns the class of this {@link IAnalysisModule}
     *
     * @return The class of the module
     */
    public Class<? extends IAnalysisModule> getModuleClass() {
        return fModule.getClass();
    }

    // ---------------------------------------
    // Functionalities
    // ---------------------------------------

    /**
     * Creates a new instance of the {@link IAnalysisModule} represented by this
     * class
     *
     * @param trace
     *            The trace to be linked to the module
     * @return A new {@link IAnalysisModule} instance
     * @throws TmfAnalysisException
     *             Exceptions that occurred when setting trace
     */
    public IAnalysisModule newModule(ITmfTrace trace) throws TmfAnalysisException {
        IAnalysisModule module = TmfAnalysisType.getInstance().getAnalysisModule(fModule.getId());
        if (module == null && fOriginModule != null) {
            module = fOriginModule.getExecutableModule(fModule.getId());
        }
        if (module == null) {
            throw new TmfAnalysisException(NLS.bind(Messages.TmfAnalysisModuleHelper_ImpossibleToCreateModule, fModule.getId()));
        }
        module.setTrace(trace);
        return module;
    }

    /**
     * Sets the name of a trace for which this analysis was requested, but a new
     * instance could not be created typically because the trace was not opened
     * yet.
     *
     * @param traceName
     *            Name of the trace
     */
    public void setExecuteTraceName(String traceName) {
        // fExecuteOnTrace = traceName;
    }

}
