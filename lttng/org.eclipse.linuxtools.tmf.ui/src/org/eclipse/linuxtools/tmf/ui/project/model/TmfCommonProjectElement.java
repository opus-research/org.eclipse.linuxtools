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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
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
 * @since 3.0
 */
public abstract class TmfCommonProjectElement extends TmfProjectModelElement {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // This trace type ID as defined in plugin.xml
    private String fTraceTypeId = null;

    private static final String BOOKMARKS_HIDDEN_FILE = ".bookmarks"; //$NON-NLS-1$

    /**
     * List of texts that can be requested, but can be different for each
     * child class
     */
    public enum textType {
        /** Text for an error opening the element */
        TEXT_ERROROPENING,
        /** Title text for message boxes when opening an element */
        TEXT_OPEN,
        /** Text for and error opening the element to be put in a sentence */
        TEXT_ERRORELEMENT,
        /** No trace type associated with this element */
        TEXT_NOTYPE,
    }

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
            Activator.getDefault().logError(Messages.TmfCommonProjectElement_ErrorRefreshingProperty + getName(), e);
        }
    }

    /**
     * Instantiate a <code>ITmfTrace</code> object based on the trace type and
     * the corresponding extension.
     *
     * @return the <code>ITmfTrace</code> or <code>null</code> for an error
     * @since 2.1
     */
    public abstract ITmfTrace instantiateTrace();


    /**
     * Return the resource name for this element
     *
     * @return The name of the resource for this element
     */
    protected String getResourceName() {
        return fResource.getName() + getSuffix();
    }

    /**
     * @return The suffix for resource names
     */
    protected String getSuffix() {
        return new String();
    }

    /**
     * Returns a list of TmfTraceElements contained in project element.
     * @return a list of TmfTraceElements, empty list if none
     */
    public List<TmfTraceElement> getTraces() {
        return new ArrayList<TmfTraceElement>();
    }

    /**
     * Returns the string corresponding to the requested text
     *
     * @param type
     *            The type of text requested
     * @return The text for this element
     */
    public abstract String getText(textType type);

    /**
     * Returns the file resource used to store bookmarks after creating it if
     * necessary. If the trace resource is a file, it is returned directly. If
     * the trace resource is a folder, a linked file is returned. The file will
     * be created if it does not exist.
     *
     * @return the bookmarks file
     * @throws CoreException
     *             if the bookmarks file cannot be created
     * @since 2.0
     */
    public abstract IFile createBookmarksFile() throws CoreException;

    /**
     * Actually returns the bookmark file or creates it in the project element's
     * folder
     *
     * @param bookmarksFolder
     *            Folder where to put the bookmark file
     * @param canonicalName
     *            The canonical name to set as tracetype
     * @return The bookmark file
     * @throws CoreException
     *             if the bookmarks file cannot be created
     */
    protected IFile createBookmarksFile(IFolder bookmarksFolder, String canonicalName) throws CoreException {
        IFile file = getBookmarksFile();
        if (!file.exists()) {
            final IFile bookmarksFile = bookmarksFolder.getFile(BOOKMARKS_HIDDEN_FILE);
            if (!bookmarksFile.exists()) {
                final InputStream source = new ByteArrayInputStream(new byte[0]);
                bookmarksFile.create(source, true, null);
            }
            bookmarksFile.setHidden(true);
            file.createLink(bookmarksFile.getLocation(), IResource.REPLACE, null);
            file.setHidden(true);
            file.setPersistentProperty(TmfCommonConstants.TRACETYPE, canonicalName);
        }
        return file;
    }

    /**
     * Returns the optional editor ID from the trace type extension.
     *
     * @return the editor ID or <code>null</code> if not defined.
     */
    public abstract String getEditorId();

    /**
     * Copy this model element
     *
     * @param newName
     *            The name of the new element
     * @param copySuppFiles
     *            Whether to copy supplementary files or not
     * @return the new Resource object
     */
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
                        Activator.getDefault().logError(Messages.TmfCommonProjectElement_ErrorClosingEditor + getName(), e);
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
    public void deleteSupplementaryFolder() {
        IFolder supplFolder = getTraceSupplementaryFolder(getResourceName());
        if (supplFolder.exists()) {
            try {
                supplFolder.delete(true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError(Messages.TmfCommonProjectElement_ErrorDeletingSF + supplFolder, e);
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
    public void renameSupplementaryFolder(String newName) {
        IFolder oldSupplFolder = getTraceSupplementaryFolder(getResourceName());
        IFolder newSupplFolder = getTraceSupplementaryFolder(newName + getSuffix());

        // Rename supplementary folder
        if (oldSupplFolder.exists()) {
            try {
                oldSupplFolder.move(newSupplFolder.getFullPath(), true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError(Messages.TmfCommonProjectElement_ErrorRenamingSF + oldSupplFolder, e);
            }
        }
    }

    /**
     * Copies the element specific supplementary folder to the new element name.
     *
     * @param newName
     *            The new element name
     */
    public void copySupplementaryFolder(String newName) {
        IFolder oldSupplFolder = getTraceSupplementaryFolder(getResourceName());
        IFolder newSupplFolder = getTraceSupplementaryFolder(newName + getSuffix());

        // copy supplementary folder
        if (oldSupplFolder.exists()) {
            try {
                oldSupplFolder.copy(newSupplFolder.getFullPath(), true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError(Messages.TmfCommonProjectElement_ErrorCopyingSF + oldSupplFolder, e);
            }
        }
    }

    /**
     * Copies the element specific supplementary folder a new folder.
     *
     * @param destination
     *            The destination folder to copy to.
     */
    public void copySupplementaryFolder(IFolder destination) {
        IFolder oldSupplFolder = getTraceSupplementaryFolder(getResourceName());

        // copy supplementary folder
        if (oldSupplFolder.exists()) {
            try {
                oldSupplFolder.copy(destination.getFullPath(), true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError(Messages.TmfCommonProjectElement_ErrorCopyingSF + oldSupplFolder, e);
            }
        }
    }

    /**
     * Refreshes the element specific supplementary folder information. It
     * creates the folder if not exists. It sets the persistence property of the
     * trace resource
     */
    public void refreshSupplementaryFolder() {
        createSupplementaryDirectory();
    }

    /**
     * Checks if supplementary resource exist or not.
     *
     * @return <code>true</code> if one or more files are under the element
     *         supplementary folder
     */
    public boolean hasSupplementaryResources() {
        IResource[] resources = getSupplementaryResources();
        return (resources.length > 0);
    }

    /**
     * Returns the supplementary resources under the trace supplementary folder.
     *
     * @return array of resources under the trace supplementary folder.
     */
    public IResource[] getSupplementaryResources() {
        IFolder supplFolder = getTraceSupplementaryFolder(getResourceName());
        if (supplFolder.exists()) {
            try {
                return supplFolder.members();
            } catch (CoreException e) {
                Activator.getDefault().logError(Messages.TmfCommonProjectElement_ErrorDeletingSF + supplFolder, e);
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
    public void deleteSupplementaryResources(IResource[] resources) {

        for (int i = 0; i < resources.length; i++) {
            try {
                resources[i].delete(true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError(Messages.TmfCommonProjectElement_ErrorDeletingSR + resources[i], e);
            }
        }
    }

    /**
     * Deletes all supplementary resources in the supplementary directory
     */
    public void deleteSupplementaryResources() {
        deleteSupplementaryResources(getSupplementaryResources());
    }

    private void createSupplementaryDirectory() {
        IFolder supplFolder = getTraceSupplementaryFolder(getResourceName());
        if (!supplFolder.exists()) {
            try {
                supplFolder.create(true, true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError(Messages.TmfCommonProjectElement_ErrorCreateSuppRes + supplFolder, e);
            }
        }

        try {
            fResource.setPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER, supplFolder.getLocationURI().getPath());
        } catch (CoreException e) {
            Activator.getDefault().logError(Messages.TmfCommonProjectElement_ErrorSettingProperty + TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER, e);
        }
    }
}
