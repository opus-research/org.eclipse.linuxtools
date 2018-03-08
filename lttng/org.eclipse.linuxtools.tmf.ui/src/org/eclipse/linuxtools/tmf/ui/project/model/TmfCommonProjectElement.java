/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Added supplementary files handling (in class TmfTraceElement)
 *   Geneviève Bastien - Copied supplementary files handling from TmfTracElement
 *                 Moved to this class code to copy a model element
 *                 Added trace type in this class so experiments can use it
 *                 Renamed from TmfWithFolderElement to TmfCommonProjectElement
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Base class for project elements who will have folder elements under them to
 * store supplementary files.
 *
 * @author gbastien
 * @since 2.1
 * TODO: Replace extension of TmfWithFolderElement with TmfProjectModelElement
 * when API 3.0 is under development.  For now it extends TmfWithFolderElement
 * to avoid API breakage, but all functions of that class have been moved here
 */
@SuppressWarnings("deprecation")
public abstract class TmfCommonProjectElement extends TmfWithFolderElement {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // This trace type ID as defined in plugin.xml
    private String fTraceTypeId = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor. Creates model element.
     *
     * @param name
     *            The name of the element
     * @param resource
     *            The resource.
     * @param parent
     *            The parent element
     */
    public TmfCommonProjectElement(String name, IResource resource, TmfProjectModelElement parent) {
        super(name, resource, parent);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Returns the trace type ID.
     *
     * @return trace type ID.
     */
    public String getTraceType() {
        return fTraceTypeId;
    }

    /**
     * Refreshes the trace type filed by reading the trace type persistent
     * property of the resource referenece.
     */
    public void refreshTraceType() {
        try {
            fTraceTypeId = getResource().getPersistentProperty(TmfCommonConstants.TRACETYPE);
        } catch (CoreException e) {
            Activator.getDefault().logError("Error refreshing trace type pesistent property for trace " + getName(), e); //$NON-NLS-1$
        }
    }

    /**
     * Return the resource name for this element
     *
     * @return The name of the resource for this element
     */
    @Override
    protected String getResourceName() {
        return fResource.getName() + getSuffix();
    }

    /**
     * @return The suffix for resource names
     */
    @Override
    protected String getSuffix() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Copy this model element
     *
     * @param newName
     *            The name of the new element
     * @param copySuppFiles
     *            Whether to copy supplementary files or not
     * @return the new Resource object
     */
    @Override
    public IResource copy(final String newName, final boolean copySuppFiles) {

        final IPath newPath = getParent().getResource().getFullPath().addTrailingSeparator().append(newName);

        /* Copy supplementary files first, only if needed */
        if (copySuppFiles) {
            copySupplementaryFolder(newName);
        }
        /* Copy the trace */
        try {
            getResource().copy(newPath, IResource.FORCE | IResource.SHALLOW, null);

            /* Delete any bookmarks file found in copied trace folder */
            IFolder folder = ((IFolder) getParent().getResource()).getFolder(newName);
            if (folder.exists()) {
                for (IResource member : folder.members()) {
                    if (TmfTrace.class.getCanonicalName().equals(member.getPersistentProperty(TmfCommonConstants.TRACETYPE))) {
                        member.delete(true, null);
                    }
                    if (TmfExperiment.class.getCanonicalName().equals(member.getPersistentProperty(TmfCommonConstants.TRACETYPE))) {
                        member.delete(true, null);
                    }
                }
            }
            return folder;
        } catch (CoreException e) {

        }

        return null;
    }

    /**
     * Returns the file resource used to store bookmarks. The file may not
     * exist.
     *
     * @return the bookmarks file
     * @since 2.0
     */
    public IFile getBookmarksFile() {
        final IFolder folder = (IFolder) fResource;
        IFile file = folder.getFile(getName() + '_');
        return file;
    }

    /**
     * Close open editors associated with this experiment.
     *
     * @since 2.0
     */
    public void closeEditors() {
        IFile file = getBookmarksFile();
        FileEditorInput input = new FileEditorInput(file);
        IWorkbench wb = PlatformUI.getWorkbench();
        for (IWorkbenchWindow wbWindow : wb.getWorkbenchWindows()) {
            for (IWorkbenchPage wbPage : wbWindow.getPages()) {
                for (IEditorReference editorReference : wbPage.getEditorReferences()) {
                    try {
                        if (editorReference.getEditorInput().equals(input)) {
                            wbPage.closeEditor(editorReference.getEditor(false), false);
                        }
                    } catch (PartInitException e) {
                        Activator.getDefault().logError("Error closing editor for " + getName(), e); //$NON-NLS-1$
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Supplementary files operations
    // ------------------------------------------------------------------------

    /**
     * Deletes this element specific supplementary folder.
     */
    @Override
    public void deleteSupplementaryFolder() {
        IFolder supplFolder = getTraceSupplementaryFolder(getResourceName());
        if (supplFolder.exists()) {
            try {
                supplFolder.delete(true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error deleting supplementary folder " + supplFolder, e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Renames the element specific supplementary folder according to the new
     * element name.
     *
     * @param newName
     *            The new element name
     */
    @Override
    public void renameSupplementaryFolder(String newName) {
        IFolder oldSupplFolder = getTraceSupplementaryFolder(getResourceName());
        IFolder newSupplFolder = getTraceSupplementaryFolder(newName + getSuffix());

        // Rename supplementary folder
        if (oldSupplFolder.exists()) {
            try {
                oldSupplFolder.move(newSupplFolder.getFullPath(), true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error renaming supplementary folder " + oldSupplFolder, e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Copies the element specific supplementary folder to the new element name.
     *
     * @param newName
     *            The new element name
     */
    @Override
    public void copySupplementaryFolder(String newName) {
        IFolder oldSupplFolder = getTraceSupplementaryFolder(getResourceName());
        IFolder newSupplFolder = getTraceSupplementaryFolder(newName + getSuffix());

        // copy supplementary folder
        if (oldSupplFolder.exists()) {
            try {
                oldSupplFolder.copy(newSupplFolder.getFullPath(), true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error renaming supplementary folder " + oldSupplFolder, e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Copies the element specific supplementary folder a new folder.
     *
     * @param destination
     *            The destination folder to copy to.
     */
    @Override
    public void copySupplementaryFolder(IFolder destination) {
        IFolder oldSupplFolder = getTraceSupplementaryFolder(getResourceName());

        // copy supplementary folder
        if (oldSupplFolder.exists()) {
            try {
                oldSupplFolder.copy(destination.getFullPath(), true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error copying supplementary folder " + oldSupplFolder, e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Refreshes the element specific supplementary folder information. It
     * creates the folder if not exists. It sets the persistence property of the
     * trace resource
     */
    @Override
    public void refreshSupplementaryFolder() {
        createSupplementaryDirectory();
    }

    /**
     * Checks if supplementary resource exist or not.
     *
     * @return <code>true</code> if one or more files are under the element
     *         supplementary folder
     */
    @Override
    public boolean hasSupplementaryResources() {
        IResource[] resources = getSupplementaryResources();
        return (resources.length > 0);
    }

    /**
     * Returns the supplementary resources under the trace supplementary folder.
     *
     * @return array of resources under the trace supplementary folder.
     */
    @Override
    public IResource[] getSupplementaryResources() {
        IFolder supplFolder = getTraceSupplementaryFolder(getResourceName());
        if (supplFolder.exists()) {
            try {
                return supplFolder.members();
            } catch (CoreException e) {
                Activator.getDefault().logError("Error deleting supplementary folder " + supplFolder, e); //$NON-NLS-1$
            }
        }
        return new IResource[0];
    }

    /**
     * Deletes the given resources.
     *
     * @param resources
     *            array of resources to delete.
     */
    @Override
    public void deleteSupplementaryResources(IResource[] resources) {

        for (int i = 0; i < resources.length; i++) {
            try {
                resources[i].delete(true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error deleting supplementary resource " + resources[i], e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Deletes all supplementary resources in the supplementary directory
     */
    @Override
    public void deleteSupplementaryResources() {
        deleteSupplementaryResources(getSupplementaryResources());
    }

    private void createSupplementaryDirectory() {
        IFolder supplFolder = getTraceSupplementaryFolder(getResourceName());
        if (!supplFolder.exists()) {
            try {
                supplFolder.create(true, true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error creating resource supplementary file " + supplFolder, e); //$NON-NLS-1$
            }
        }

        try {
            fResource.setPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER, supplFolder.getLocationURI().getPath());
        } catch (CoreException e) {
            Activator.getDefault().logError("Error setting persistant property " + TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER, e); //$NON-NLS-1$
        }
    }
}
