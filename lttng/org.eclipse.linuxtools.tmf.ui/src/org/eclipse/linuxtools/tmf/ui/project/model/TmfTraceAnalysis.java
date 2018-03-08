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
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.views.analysis.ITmfAnalysisViewable;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;
import org.osgi.framework.Bundle;

/**
 * @since 3.0
 */
public class TmfTraceAnalysis extends TmfProjectModelElement {

    private String fAnalysisId;
    private IAnalysisModule fModule;

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
     * @param module
     *            The actual analysis module
     */
    protected TmfTraceAnalysis(String name, IResource resource, ITmfProjectModelElement parent, String id, IAnalysisModule module) {
        super(name, resource, parent);
        fAnalysisId = id;
        fModule = module;
        refreshViews();
    }

    private void refreshViews() {
        List<TmfAnalysisView> views = getAvailableViews();

        /* Remove children */
        getChildren().clear();

        /* Add the children again */
        for (TmfAnalysisView module : views) {
            addChild(module);
        }

    }

    /**
     * Get the list of analysis elements
     *
     * @return Array of analysis elements
     * @since 3.0
     */
    public List<TmfAnalysisView> getAvailableViews() {
        List<TmfAnalysisView> list = new ArrayList<TmfAnalysisView>();

        if (fModule != null && (fModule instanceof ITmfAnalysisViewable)) {
            ITmfAnalysisViewable moduleWithViews = (ITmfAnalysisViewable) fModule;
            for (String viewId : moduleWithViews.getViews()) {

                IViewDescriptor descr = PlatformUI.getWorkbench().getViewRegistry().find(
                        viewId);
                String viewName = (descr != null) ? descr.getLabel() : viewId + Messages.TmfTraceAnalysis_ViewUnavailable;

                if (fResource instanceof IFolder) {
                    IFolder folder = (IFolder) fResource;
                    IPath path = folder.getFullPath().append(viewId);
                    IFolder newresource = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
                    TmfAnalysisView view = new TmfAnalysisView(viewName, newresource, this, viewId);
                    list.add(view);
                }
            }
        }

        return list;
    }

    @Override
    public TmfProjectElement getProject() {
        return (TmfProjectElement) getParent();
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
        if (getParent() instanceof TmfTraceElement) {
            TmfTraceElement traceElement = (TmfTraceElement) getParent();
            /*
             * TODO: this is not enough, and I don't want to copy-paste the full
             * trace instantiation thing. We would need to have a single method
             * to open a trace. And a singleton so as not to reinitialize it
             * each time we need it?
             */
            final ITmfTrace trace = traceElement.instantiateTrace();
            return fModule.getHelpText(trace);
        }
        /* TODO: when experiment types are supported, also add for experiments */
        return fModule.getHelpText();
    }

    /**
     * Gets the icon file name for the analysis
     *
     * @return The analysis icon file name
     */
    public String getIconFile() {
        return fModule.getIcon();
    }

    /**
     * Gets the bundle this analysis is from
     *
     * @return The analysis bundle
     */
    public Bundle getBundle() {
        return fModule.getBundle();
    }
}
