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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.osgi.framework.Bundle;

/**
 * Interface that all analysis modules main class must implement
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public interface IAnalysisModule {

    // --------------------------------------------------------
    // Getters and setters
    // --------------------------------------------------------

    /**
     * Sets the name of the analysis module
     *
     * @param name
     *            name of the module
     */
    void setName(String name);

    /**
     * Gets the name of the analysis module
     *
     * @return Name of the module
     */
    String getName();

    /**
     * Sets the id of the module
     *
     * @param id
     *            id of the module
     */
    void setId(String id);

    /**
     * Gets the id of the analysis module
     *
     * @return The id of the module
     */
    String getId();

    /**
     * Sets whether this analysis should be run automatically at trace opening
     *
     * @param auto
     */
    void setAutomatic(boolean auto);

    /**
     * Gets whether the analysis should be run automatically at trace opening
     *
     * @return true if analysis is to be run automatically
     */
    boolean isAutomatic();

    /**
     * Sets the icon path for this analysis module
     *
     * @param icon
     *            The icon path
     */
    void setIcon(String icon);

    /**
     * Gets the icon path
     *
     * @return The icon path
     */
    String getIcon();

    /**
     * Sets the trace on which to run the analysis
     *
     * @param trace
     *            The trace to run the analysis on
     * @throws TmfAnalysisException
     */
    void setTrace(ITmfTrace trace) throws TmfAnalysisException;

    /**
     * If a trace is not opened yet, we can set a trace name and the analysis
     * will listen for this trace opening and do the setTrace at this time
     *
     * @param traceName
     *            Name of the trace to set
     */
    void setTraceName(String traceName);

    /**
     * Add a parameter to this module
     *
     * @param name
     *            Name of the parameter
     */
    void addParameter(String name);

    /**
     * Sets the value of a parameter
     *
     * @param name
     *            The name of the parameter
     * @param value
     *            The value (subclasses may type-check it)
     * @throws RuntimeException
     */
    void setParameter(String name, Object value) throws RuntimeException;

    /**
     * Gets the value of a parameter
     *
     * @param name
     *            Name of the parameter
     * @return The value of a parameter
     */
    Object getParameter(String name);

    /**
     * Get the actual instances of analysis modules provided by this module
     * (some modules may be [non-executable] entry points to a list of actual
     * modules)
     *
     * @return List of executable modules
     */
    List<IAnalysisModule> getExecutableModules();

    // -----------------------------------------------------
    // Functionnalities
    // -----------------------------------------------------

    /**
     * Does an analysis apply to a given trace type (otherwise, it is not shown)
     *
     * @param traceclass
     *            The trace to analyse
     * @return whether the analysis applies
     */
    boolean appliesToTraceType(Class<? extends ITmfTrace> traceclass);

    /**
     * Can an analysis be executed on a given trace (otherwise, it is shown
     * grayed out and a help message is available to see why it is not
     * applicable)
     *
     * @param trace
     *            The trace to analyse
     * @return Whether the analysis can be executed
     */
    boolean canExecute(ITmfTrace trace);

    /**
     * Schedule to analysis to be executed. If the trace is set and everything
     * is ok, the analysis will be executed, otherwise it should be set to be
     * executed once all pre-conditions are satisfied.
     *
     * @return An IStatus indicating if the execution of the analysis could be
     *         scheduled successfully or not.
     */
    IStatus schedule();

    /**
     * Gets a list of outputs
     *
     * @return The list of outputs
     */
    List<IAnalysisOutput> getOutputs();

    /**
     * Registers an output for this analysis
     *
     * @param output
     *            The output object
     */
    void registerOutput(IAnalysisOutput output);

    /**
     * Typically the output of an analysis will be available only after it is
     * completed. This method allows to wait until an analysis has been
     * completed.
     *
     * @return If the analysis was successfully completed. If false is returned,
     *         this either means there was a problem during the analysis, or it
     *         got cancelled before it could finished or it has not been
     *         scheduled to run at all. In all cases, the quality or
     *         availability of the output(s) and results is not guaranteed.
     */
    boolean waitForCompletion();

    /**
     * Same as {@link this#waitForCompletion()} but checks for cancellation
     *
     * @param monitor
     *            The progress monitor to check for cancellation
     * @return If the analysis was successfully completed. If false is returned,
     *         this either means there was a problem during the analysis, or it
     *         got cancelled before it could finished or it has not been
     *         scheduled to run at all. In all cases, the quality or
     *         availability of the output(s) and results is not guaranteed.
     */
    boolean waitForCompletion(IProgressMonitor monitor);

    // -----------------------------------------------------
    // Utilities
    // -----------------------------------------------------

    /**
     * Gets a generic help message/documentation for this analysis module
     *
     * This help text will be displayed to the user and may contain information
     * on what the module does, how to use it and how to correctly generate the
     * trace to make it available
     *
     * TODO: Help texts could be quite long. They should reside in their own
     * file and be accessed either with text, for a command line man page, or
     * through the eclipse help context.
     *
     * @return The generic help text
     */
    String getHelpText();

    /**
     * Gets a help text specific for a given trace
     *
     * For instance, it may explain why the analysis module cannot be executed
     * on a trace and how to correct this
     *
     * @param trace
     *            The trace to analyse
     * @return A help text with information on a specific trace
     */
    String getHelpText(ITmfTrace trace);

    /**
     * Gets the bundle this analysis module is part of
     *
     * @return The bundle
     */
    Bundle getBundle();

    /**
     * Notify the module that the value of a parameter has changed
     *
     * @param name
     *            The of the parameter that changed
     */
    void notifyParameterChanged(String name);

}
