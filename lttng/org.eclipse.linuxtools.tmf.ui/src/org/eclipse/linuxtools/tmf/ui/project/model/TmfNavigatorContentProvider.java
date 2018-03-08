/*******************************************************************************
 * Copyright (c) 2010, 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

/**
 * The TMF project content provider for the tree viewer in the project explorer view.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfNavigatorContentProvider implements IPipelinedTreeContentProvider {

    // ------------------------------------------------------------------------
    // ICommonContentProvider
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof IProject) {
            IProject project = (IProject) element;
            return project.isAccessible();
        }
        if (element instanceof TmfTraceFolder) {
            TmfTraceFolder folder = (TmfTraceFolder) element;
            return folder.hasChildren();
        }
        if (element instanceof TmfExperimentFolder) {
            TmfExperimentFolder folder = (TmfExperimentFolder) element;
            return folder.hasChildren();
        }
        if (element instanceof TmfExperimentElement) {
            TmfExperimentElement folder = (TmfExperimentElement) element;
            return folder.hasChildren();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
     */
    @Override
    public void restoreState(IMemento aMemento) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
     */
    @Override
    public void saveState(IMemento aMemento) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.navigator.ICommonContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
     */
    @Override
    public void init(ICommonContentExtensionSite aConfig) {
    }

    // ------------------------------------------------------------------------
    // ICommonContentProvider - getChildren()
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    @Override
    public synchronized Object[] getChildren(Object parentElement) {

        // Tracing project level
        if (parentElement instanceof IProject) {
            return getProjectChildren((IProject) parentElement);
        }

        // Traces "folder" level
        if (parentElement instanceof TmfTraceFolder) {
            return getTraceFolderChildren((TmfTraceFolder) parentElement);
        }

        // Experiments "folder" level
        if (parentElement instanceof TmfExperimentFolder) {
            return getExperimentFolderChildren((TmfExperimentFolder) parentElement);
        }

        // Experiment
        if (parentElement instanceof TmfExperimentElement) {
            return getExperimentChildren((TmfExperimentElement) parentElement);
        }

        return new Object[0];
    }

    // ------------------------------------------------------------------------
    // Helper method
    // ------------------------------------------------------------------------
    private Object[] getProjectChildren(IProject project) {
        // The children structure
        List<Object> children = new ArrayList<Object>();

        // Get the children from the model
        Map<String, ITmfProjectModelElement> childrenMap = new HashMap<String, ITmfProjectModelElement>();
        TmfProjectElement tmfProject = TmfProjectRegistry.getProject(project, true);
        for (ITmfProjectModelElement element : tmfProject.getChildren()) {
            if (element instanceof TmfTraceFolder) {
                TmfTraceFolder child = (TmfTraceFolder) element;
                childrenMap.put(child.getResource().getName(), child);
            }
            if (element instanceof TmfExperimentFolder) {
                TmfExperimentFolder child = (TmfExperimentFolder) element;
                childrenMap.put(child.getResource().getName(), child);
            }
        }

        // Add the model folder if the corresponding resource exists and is not
        // accounted for
        IFolder folder = project.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME);
        if (folder != null) {
            String name = folder.getName();
            ITmfProjectModelElement element = childrenMap.get(name);
            if (element == null) {
                element = new TmfTraceFolder(TmfTraceFolder.TRACE_FOLDER_NAME, folder, tmfProject);
            }
            children.add(element);
            childrenMap.remove(name);
            getTraceFolderChildren((TmfTraceFolder) element);
        }

        // Add the model folder if the corresponding resource exists and is not
        // accounted for
        folder = project.getFolder(TmfExperimentFolder.EXPER_FOLDER_NAME);
        if (folder != null) {
            String name = folder.getName();
            ITmfProjectModelElement element = childrenMap.get(name);
            if (element == null) {
                element = new TmfExperimentFolder(TmfExperimentFolder.EXPER_FOLDER_NAME, folder, tmfProject);
            }
            children.add(element);
            childrenMap.remove(name);
            getExperimentFolderChildren((TmfExperimentFolder) element);
        }

        // Remove the leftovers (what was in the model but removed from the
        // project)
        cleanupModel(tmfProject, childrenMap);

        return children.toArray();
    }

    private Object[] getTraceFolderChildren(TmfTraceFolder tmfTraceFolder) {
        // The children structure
        List<Object> children = new ArrayList<Object>();

        // Get the children from the model
        Map<String, ITmfProjectModelElement> childrenMap = new HashMap<String, ITmfProjectModelElement>();
        for (ITmfProjectModelElement element : tmfTraceFolder.getChildren()) {
            if (element instanceof TmfTraceElement) {
                String name = element.getResource().getName();
                childrenMap.put(name, element);
            }
        }

        IFolder folder = tmfTraceFolder.getResource();
        try {
            IResource[] members = folder.members();
            for (IResource resource : members) {
                String name = resource.getName();
                ITmfProjectModelElement trace = childrenMap.get(name);
                if (trace == null) {
                    trace = new TmfTraceElement(name, resource, tmfTraceFolder);
                }
                children.add(trace);
                childrenMap.remove(name);
            }
        } catch (CoreException e) {
        }

        // Remove the leftovers (what was in the model but removed from the
        // project)
        cleanupModel(tmfTraceFolder, childrenMap);

        return children.toArray();
    }

    private Object[] getExperimentFolderChildren(TmfExperimentFolder tmfExperimentFolder) {
        // The children structure
        List<Object> children = new ArrayList<Object>();

        // Get the children from the model
        Map<String, ITmfProjectModelElement> childrenMap = new HashMap<String, ITmfProjectModelElement>();
        for (ITmfProjectModelElement element : tmfExperimentFolder.getChildren()) {
            if (element instanceof TmfExperimentElement) {
                String name = element.getResource().getName();
                childrenMap.put(name, element);
            }
        }

        IFolder folder = tmfExperimentFolder.getResource();
        try {
            IResource[] members = folder.members();
            for (IResource resource : members) {
                if (resource instanceof IFolder) {
                    IFolder expFolder = (IFolder) resource;
                    String name = resource.getName();
                    ITmfProjectModelElement experiment = childrenMap.get(name);
                    if (experiment == null) {
                        experiment = new TmfExperimentElement(name, expFolder, tmfExperimentFolder);
                    }
                    children.add(experiment);
                    childrenMap.remove(name);
                    getExperimentChildren((TmfExperimentElement) experiment);
                }
            }
        } catch (CoreException e) {
        }

        // Remove the leftovers (what was in the model but removed from the
        // project)
        cleanupModel(tmfExperimentFolder, childrenMap);

        return children.toArray();
    }

    private Object[] getExperimentChildren(TmfExperimentElement tmfExperiment) {
        // The children structure
        List<Object> children = new ArrayList<Object>();

        // Get the children from the model
        Map<String, ITmfProjectModelElement> childrenMap = new HashMap<String, ITmfProjectModelElement>();
        for (ITmfProjectModelElement element : tmfExperiment.getChildren()) {
            if (element instanceof TmfTraceElement) {
                String name = element.getResource().getName();
                childrenMap.put(name, element);
            }
        }

        IFolder folder = tmfExperiment.getResource();
        try {
            IResource[] members = folder.members();
            for (IResource resource : members) {
                String name = resource.getName();
                ITmfProjectModelElement trace = childrenMap.get(name);
                if (trace == null && !resource.isHidden()) {
                    // exclude hidden resources (e.g. bookmarks file)
                    trace = new TmfTraceElement(name, resource, tmfExperiment);
                }
                children.add(trace);
                childrenMap.remove(name);
            }
        } catch (CoreException e) {
        }

        // Remove the leftovers (what was in the model but removed from the
        // project)
        cleanupModel(tmfExperiment, childrenMap);

        return children.toArray();
    }

    private void cleanupModel(ITmfProjectModelElement parent, Map<String, ITmfProjectModelElement> danglingChildren) {
        if (parent != null) {
            for (ITmfProjectModelElement child : danglingChildren.values()) {
                Map<String, ITmfProjectModelElement> grandChildren = new HashMap<String, ITmfProjectModelElement>();
                for (ITmfProjectModelElement element : child.getChildren()) {
                    String name = element.getResource().getName();
                    grandChildren.put(name, element);
                }
                cleanupModel(child, grandChildren);
                parent.removeChild(child);
            }
        }
    }

    // ------------------------------------------------------------------------
    // IPipelinedTreeContentProvider
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedChildren(java.lang.Object, java.util.Set)
     */
    @Override
    public void getPipelinedChildren(Object parent, Set currentChildren) {
        customizeTmfElements(getChildren(parent), currentChildren);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedElements(java.lang.Object, java.util.Set)
     */
    @Override
    public void getPipelinedElements(Object input, Set currentElements) {
        customizeTmfElements(getElements(input), currentElements);
    }

    /**
     * Add/replace the ITmfProjectElement to the list of children
     *
     * @param elements
     *            the list returned by getChildren()
     * @param children
     *            the current children
     */
    private static void customizeTmfElements(Object[] elements,
            Set<Object> children) {
        if (elements != null && children != null) {
            for (Object element : elements) {
                if (element instanceof ITmfProjectModelElement) {
                    ITmfProjectModelElement tmfElement = (ITmfProjectModelElement) element;
                    IResource resource = tmfElement.getResource();
                    if (resource != null) {
                        children.remove(resource);
                    }
                    children.add(element);
                }
                else if (element != null) {
                    children.add(element);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedParent(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
        return aSuggestedParent;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptAdd(org.eclipse.ui.navigator.PipelinedShapeModification)
     */
    @Override
    public PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification) {
        return anAddModification;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptRemove(org.eclipse.ui.navigator.PipelinedShapeModification)
     */
    @Override
    public PipelinedShapeModification interceptRemove(PipelinedShapeModification aRemoveModification) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptRefresh(org.eclipse.ui.navigator.PipelinedViewerUpdate)
     */
    @Override
    public boolean interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization) {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptUpdate(org.eclipse.ui.navigator.PipelinedViewerUpdate)
     */
    @Override
    public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
        return false;
    }
}
