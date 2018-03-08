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

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisOutput;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.osgi.framework.Bundle;


/**
 * Class for project elements of type analysis modules
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfAnalysisElement extends TmfProjectModelElement {

    private String fAnalysisId;

    /**
     * Constructor
     *
     * @param name
     *            Name of the analysis
     * @param resource
     *            The resource
     * @param parent
     *            Parent of the analysis
     * @param id
     *            The analysis module id
     */
    protected TmfAnalysisElement(String name, IResource resource, ITmfProjectModelElement parent, String id) {
        super(name, resource, parent);
        fAnalysisId = id;
        refreshViews();
    }

    private void refreshViews() {
        List<TmfAnalysisOutputElement> views = getAvailableOutputs();

        /* Remove children */
        getChildren().clear();

        /* Add the children again */
        for (TmfAnalysisOutputElement module : views) {
            addChild(module);
        }

    }

    /**
     * Get the list of analysis elements
     *
     * @return Array of analysis elements
     * @since 3.0
     */
    public List<TmfAnalysisOutputElement> getAvailableOutputs() {
        List<TmfAnalysisOutputElement> list = new ArrayList<TmfAnalysisOutputElement>();

        IAnalysisModule module = TmfAnalysisManager.getAnalysisModule(fAnalysisId);
        if (module == null) {
            return list;
        }

        /** Get base path for resource */
        IPath path = getProject().getTracesFolder().getPath();
        if (fResource instanceof IFolder) {
            path = ((IFolder) fResource).getFullPath();
        }

        for (IAnalysisOutput output : module.getOutputs()) {
            if (fResource instanceof IFolder) {
                IFolder newresource = ResourcesPlugin.getWorkspace().getRoot().getFolder(path.append(output.getName()));
                TmfAnalysisOutputElement view = new TmfAnalysisOutputElement(output.getName(), newresource, this, output);
                list.add(view);
            }
        }

        return list;
    }

    @Override
    public TmfProjectElement getProject() {
        return getParent().getProject();
    }

    /**
     * Gets the analysis id of this module
     *
     * @return The analysis id
     */
    public String getAnalysisId() {
        return fAnalysisId;
    }

    /**
     * Gets the help message for this analysis
     *
     * @return The help message
     */
    public String getHelpMessage() {
        IAnalysisModule module = TmfAnalysisManager.getAnalysisModule(fAnalysisId);
        if (module == null) {
            return new String();
        }

        if (getParent() instanceof TmfTraceElement) {
            TmfTraceElement traceElement = (TmfTraceElement) getParent();

            ITmfTrace trace = traceElement.getTrace();
            return module.getHelpText(trace);
        }
        /* TODO: when experiment types are supported, also add for experiments */
        return module.getHelpText();
    }

    /**
     * Gets the icon file name for the analysis
     *
     * @return The analysis icon file name
     */
    public String getIconFile() {
        IAnalysisModule module = TmfAnalysisManager.getAnalysisModule(fAnalysisId);
        if (module == null) {
            return null;
        }
        return module.getIcon();
    }

    /**
     * Gets the bundle this analysis is from
     *
     * @return The analysis bundle
     */
    public Bundle getBundle() {
        IAnalysisModule module = TmfAnalysisManager.getAnalysisModule(fAnalysisId);
        if (module == null) {
            return null;
        }
        return module.getBundle();
    }

    /**
     * Instantiates an analysis module by setting the trace
     *
     * @return The analysis module
     */
    public IAnalysisModule instantiateAnalysis() {
        ITmfProjectModelElement parent = getParent();
        if (parent instanceof TmfTraceElement) {
            TmfTraceElement traceElement = (TmfTraceElement) parent;
            final ITmfTrace trace = traceElement.getTrace();
            if (trace == null) {
                /*
                 * The trace is not opened, we trigger trace opening and set the
                 * trace name of the module
                 */
                TmfOpenTraceHelper.openTraceFromElement(traceElement);
                IAnalysisModule module = TmfAnalysisManager.getAnalysisModule(fAnalysisId);
                if (module == null) {
                    return null;
                }
                module.setTraceName(traceElement.getName());
                return module;
            }
            try {
                /*
                 * TODO: here we should make sure that the trace is the
                 * currently selected one, but there is not [yet] the
                 * functionality to select it
                 */
                IAnalysisModule module = TmfAnalysisManager.getAnalysisModule(trace, fAnalysisId);
                if (module == null) {
                    return null;
                }
                module.setTrace(trace);
                return module;
            } catch (TmfAnalysisException e) {
                TraceUtils.displayErrorMsg(Messages.TmfAnalysisElement_InstantiateAnalysis, e.getMessage());
            }
        }
        return null;
    }
}
