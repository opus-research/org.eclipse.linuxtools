/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Geneviève Bastien - Copied code to add/remove traces in this class
 *   Patrick Tasse - Close editors to release resources
 *   Geneviève Bastien - Experiment instantiated with trace type
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;

/**
 * Implementation of TMF Experiment Model Element.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 *
 */
public class TmfExperimentElement extends TmfCommonProjectElement implements IPropertySource2 {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // Property View stuff
    private static final String sfInfoCategory = "Info"; //$NON-NLS-1$
    private static final String sfName = "name"; //$NON-NLS-1$
    private static final String sfPath = "path"; //$NON-NLS-1$
    private static final String sfLocation = "location"; //$NON-NLS-1$
    private static final String sfFolderSuffix = "_exp"; //$NON-NLS-1$
    private static final String sfExperimentType = "type"; //$NON-NLS-1$

    private static final ReadOnlyTextPropertyDescriptor sfNameDescriptor = new ReadOnlyTextPropertyDescriptor(sfName, sfName);
    private static final ReadOnlyTextPropertyDescriptor sfPathDescriptor = new ReadOnlyTextPropertyDescriptor(sfPath, sfPath);
    private static final ReadOnlyTextPropertyDescriptor sfLocationDescriptor = new ReadOnlyTextPropertyDescriptor(sfLocation,
            sfLocation);
    private static final ReadOnlyTextPropertyDescriptor sfTypeDescriptor = new ReadOnlyTextPropertyDescriptor(sfExperimentType, sfExperimentType);

    private static final IPropertyDescriptor[] sfDescriptors = { sfNameDescriptor, sfPathDescriptor,
            sfLocationDescriptor, sfTypeDescriptor };

    static {
        sfNameDescriptor.setCategory(sfInfoCategory);
        sfPathDescriptor.setCategory(sfInfoCategory);
        sfLocationDescriptor.setCategory(sfInfoCategory);
        sfTypeDescriptor.setCategory(sfInfoCategory);
    }

    // The mapping of available trace type IDs to their corresponding
    // configuration element
    private static final Map<String, IConfigurationElement> sfTraceTypeAttributes = new HashMap<String, IConfigurationElement>();
    private static final Map<String, IConfigurationElement> sfTraceCategories = new HashMap<String, IConfigurationElement>();

    // ------------------------------------------------------------------------
    // Static initialization
    // ------------------------------------------------------------------------

    /**
     * Initialize statically at startup by getting extensions from the platform
     * extension registry.
     * @since 2.1
     */
    public static void init() {
        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(TmfTraceType.TMF_TRACE_TYPE_ID);
        for (IConfigurationElement ce : config) {
            String elementName = ce.getName();
            if (elementName.equals(TmfTraceType.EXPERIMENT_ELEM)) {
                String traceTypeId = ce.getAttribute(TmfTraceType.ID_ATTR);
                sfTraceTypeAttributes.put(traceTypeId, ce);
            } else if (elementName.equals(TmfTraceType.CATEGORY_ELEM)) {
                String categoryId = ce.getAttribute(TmfTraceType.ID_ATTR);
                sfTraceCategories.put(categoryId, ce);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name The name of the experiment
     * @param folder The folder reference
     * @param parent The experiment folder reference.
     */
    public TmfExperimentElement(String name, IFolder folder, TmfExperimentFolder parent) {
        super(name, folder, parent);
        parent.addChild(this);
        refreshTraceType();
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    public IFolder getResource() {
        return (IFolder) fResource;
    }

    @Override
    public TmfProjectElement getProject() {
        return (TmfProjectElement) getParent().getParent();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Refreshes the trace type filed by reading the trace type persistent
     * property of the resource reference.
     *
     * If trace type is null after refresh, set it to the generic trace type
     * (for seamless upgrade)
     */
    @Override
    public void refreshTraceType() {
        super.refreshTraceType();
        if (getTraceType() == null) {
            IConfigurationElement ce = TmfTraceType.getInstance().getTraceAttributes(TmfTraceType.DEFAULT_EXPERIMENT_TYPE);
            if (ce != null) {
                try {
                    IFolder experimentFolder = getResource();
                    experimentFolder.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE, ce.getContributor().getName());
                    experimentFolder.setPersistentProperty(TmfCommonConstants.TRACETYPE, ce.getAttribute(TmfTraceType.ID_ATTR));
                    experimentFolder.setPersistentProperty(TmfCommonConstants.TRACEICON, ce.getAttribute(TmfTraceType.ICON_ATTR));
                    super.refreshTraceType();
                } catch (InvalidRegistryObjectException e) {

                } catch (CoreException e) {

                }
            }
        }
    }

    /**
     * Returns a list of TmfTraceElements contained in this experiment.
     * @return a list of TmfTraceElements
     */
    @Override
    public List<TmfTraceElement> getTraces() {
        List<ITmfProjectModelElement> children = getChildren();
        List<TmfTraceElement> traces = new ArrayList<TmfTraceElement>();
        for (ITmfProjectModelElement child : children) {
            if (child instanceof TmfTraceElement) {
                traces.add((TmfTraceElement) child);
            }
        }
        return traces;
    }

    /**
     * Adds a trace to the experiment
     *
     * @param trace The trace element to add
     * @since 2.0
     */
    public void addTrace(TmfTraceElement trace) {
        /**
         * Create a link to the actual trace and set the trace type
         */
        IFolder experiment = getResource();
        IResource resource = trace.getResource();
        IPath location = resource.getLocation();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            Map<QualifiedName, String> properties = trace.getResource().getPersistentProperties();
            String bundleName = properties.get(TmfCommonConstants.TRACEBUNDLE);
            String traceType = properties.get(TmfCommonConstants.TRACETYPE);
            String iconUrl = properties.get(TmfCommonConstants.TRACEICON);

            if (resource instanceof IFolder) {
                IFolder folder = experiment.getFolder(trace.getName());
                if (workspace.validateLinkLocation(folder, location).isOK()) {
                    folder.createLink(location, IResource.REPLACE, null);
                    setProperties(folder, bundleName, traceType, iconUrl);

                } else {
                    Activator.getDefault().logError("Error creating link. Invalid trace location " + location); //$NON-NLS-1$
                }
            } else {
                IFile file = experiment.getFile(trace.getName());
                if (workspace.validateLinkLocation(file, location).isOK()) {
                    file.createLink(location, IResource.REPLACE, null);
                    setProperties(file, bundleName, traceType, iconUrl);
                } else {
                    Activator.getDefault().logError("Error creating link. Invalid trace location " + location); //$NON-NLS-1$
                }
            }
        } catch (CoreException e) {
            Activator.getDefault().logError("Error creating link to location " + location, e); //$NON-NLS-1$
        }

    }

    /**
     * Removes a trace from an experiment
     *
     * @param trace The trace to remove
     * @throws CoreException exception
     * @since 2.0
     */
    public void removeTrace(TmfTraceElement trace) throws CoreException {

        // Close the experiment if open
        closeEditors();

        /* Finally, remove the trace from experiment*/
        removeChild(trace);
        trace.getResource().delete(true, null);

    }

    private static void setProperties(IResource resource, String bundleName,
            String traceType, String iconUrl) throws CoreException {
        resource.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE, bundleName);
        resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, traceType);
        resource.setPersistentProperty(TmfCommonConstants.TRACEICON, iconUrl);
    }

    @Override
    public IFile createBookmarksFile() throws CoreException {
        return createBookmarksFile(getProject().getExperimentsFolder().getResource(), TmfExperiment.class.getCanonicalName());
    }

    @Override
    public String getEditorId() {
        /* See if a default editor was set for this experiment type */
        if (getTraceType() != null) {
            IConfigurationElement ce = sfTraceTypeAttributes.get(getTraceType());
            IConfigurationElement[] defaultEditorCE = ce.getChildren(TmfTraceType.DEFAULT_EDITOR_ELEM);
            if (defaultEditorCE.length == 1) {
                return defaultEditorCE[0].getAttribute(TmfTraceType.ID_ATTR);
            }
        }

        /* No default editor, try to find a common editor for all traces */
        final List<TmfTraceElement> traceEntries = getTraces();
        String commonEditorId = null;

        for (TmfTraceElement element : traceEntries) {
            // If all traces use the same editorId, use it, otherwise use the
            // default
            final String editorId = element.getEditorId();
            if (commonEditorId == null) {
                commonEditorId = (editorId != null) ? editorId : TmfEventsEditor.ID;
            } else if (!commonEditorId.equals(editorId)) {
                commonEditorId = TmfEventsEditor.ID;
            }
        }
        return null;
    }

    /**
     * Instantiate a {@link TmfExperiment} object based on the experiment type
     * and the corresponding extension.
     *
     * @return the {@link TmfExperiment} or <code>null</code> for an error
     */
    @Override
    public TmfExperiment instantiateTrace() {
        try {

            // make sure that supplementary folder exists
            refreshSupplementaryFolder();

            if (getTraceType() != null) {

                IConfigurationElement ce = sfTraceTypeAttributes.get(getTraceType());
                if (ce == null) {
                    return null;
                }
                TmfExperiment experiment = (TmfExperiment) ce.createExecutableExtension(TmfTraceType.EXPERIMENT_TYPE_ATTR);
                return experiment;
            }
        } catch (CoreException e) {
            Activator.getDefault().logError(Messages.TmfExperimentElement_ErrorInstantiatingTrace + getName(), e);
        }
        return null;
    }

    @Override
    public String getText(textType type) {
        String text;
        switch(type) {
        case TEXT_ERROROPENING:
            text = Messages.TmfOpenTraceHelper_ErrorOpeningExperiment;
            break;
        case TEXT_OPEN:
            text = Messages.TmfOpenTraceHelper_OpenExperiment;
            break;
        case TEXT_ERRORELEMENT:
            text = Messages.TmfOpenTraceHelper_ErrorExperiment;
            break;
        case TEXT_NOTYPE:
            text = Messages.TmfOpenTraceHelper_NoExperimentType;
            break;
        default:
            text = new String();
            break;
        }
        return text;
    }

    // ------------------------------------------------------------------------
    // IPropertySource2
    // ------------------------------------------------------------------------

    @Override
    public Object getEditableValue() {
        return null;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return Arrays.copyOf(sfDescriptors, sfDescriptors.length);
    }

    @Override
    public Object getPropertyValue(Object id) {

        if (sfName.equals(id)) {
            return getName();
        }

        if (sfPath.equals(id)) {
            return getPath().toString();
        }

        if (sfLocation.equals(id)) {
            return getLocation().toString();
        }

        if (sfExperimentType.equals(id)) {
            if (getTraceType() != null) {
                IConfigurationElement ce = sfTraceTypeAttributes.get(getTraceType());
                return (ce != null) ? (getCategory(ce) + ':' + ce.getAttribute(TmfTraceType.NAME_ATTR)) : new String();
            }
        }

        return null;
    }

    private static String getCategory(IConfigurationElement ce) {
        String categoryId = ce.getAttribute(TmfTraceType.CATEGORY_ATTR);
        if (categoryId != null) {
            IConfigurationElement category = sfTraceCategories.get(categoryId);
            if (category != null) {
                return category.getAttribute(TmfTraceType.NAME_ATTR);
            }
        }
        return "[no category]"; //$NON-NLS-1$
    }

    @Override
    public void resetPropertyValue(Object id) {
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
    }

    @Override
    public boolean isPropertyResettable(Object id) {
        return false;
    }

    @Override
    public boolean isPropertySet(Object id) {
        return false;
    }

    /**
     * Return the suffix for resource names
     * @return The folder suffix
     */
    @Override
    public String getSuffix() {
        return sfFolderSuffix;
    }

}
