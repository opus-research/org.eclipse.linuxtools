/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * Import utils, for importing and setting types.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class TmfProjectUtils {

    // default trace icon
    private static final String DEFAULT_TRACE_ICON_PATH = "icons/elcl16/trace.gif"; //$NON-NLS-1$

    private static TmfProjectUtils tmfImportUtils;

    private TmfProjectUtils() {

    }

    /**
     * Get an instance of the tmf import utils
     *
     * @return an instance of TmfImportUtils
     */
    public static TmfProjectUtils getInstance() {
        if (tmfImportUtils == null) {
            tmfImportUtils = new TmfProjectUtils();
        }
        return tmfImportUtils;
    }

    /**
     * Gets the root of the project
     *
     * @return the root
     */
    private static IWorkspaceRoot getProjectsRoot() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    /**
     * Gets the projects in the workspace
     *
     * @return the projects
     */
    public List<IProject> getProjects() {
        List<IProject> retVal = new ArrayList<IProject>();
        IWorkspaceRoot root = getProjectsRoot();
        final IProject[] projects = root.getProjects();
        for (IProject child : projects) {
            IFolder traceElem = getTraceFolder(child);
            if (traceElem != null) {
                retVal.add(child);
            }
        }
        return retVal;
    }

    /**
     * Import trace, adds it to a resource
     *
     * @param traceName
     *            the name of the trace to display
     * @param tracePath
     *            the path of the file to add
     * @param project
     *            the project to import it to. must be the root project like
     *            <b>/tracing project</b> and not /tracing project/Traces, it
     *            will append the "Traces" part on its own
     * @param isLinked
     *            Is the project Linked (not copied?)
     * @param overwrite
     *            Should the project overwrite file already there?
     * @return if the import was successful
     * @throws CoreException
     *             import operations
     */
    public boolean importTrace(final String traceName, final File tracePath, final IProject project, final boolean isLinked, final boolean overwrite) throws CoreException {
        if (isLinked) {
            return importTraceLink(traceName, tracePath, project, overwrite);
        }
        return importTraceCopy(traceName, tracePath, project, overwrite);
    }

    private static boolean importTraceLink(final String traceName, final File tracePath, final IProject project, final boolean overwrite) throws CoreException {
        int overwriteMode = 0;
        if (overwrite) {
            overwriteMode = IResource.REPLACE;
        }
        if (isValidProject(project)) {
            IPath location = Path.fromOSString(tracePath.getAbsolutePath());
            IWorkspace workspace = project.getWorkspace();
            IFolder importFolder = getTraceFolder(project);

            if (tracePath.isDirectory()) {
                IFolder folder = importFolder.getFolder(traceName);
                IStatus result = workspace.validateLinkLocation(folder, location);
                if (result.isOK()) {
                    folder.createLink(location, overwriteMode, null);
                    return true;
                }
                Activator.getDefault().logError(result.getMessage());
            } else {
                IFile file = importFolder.getFile(traceName);
                IStatus result = workspace.validateLinkLocation(file, location);
                if (result.isOK()) {
                    file.createLink(location, overwriteMode, null);
                    return true;
                }
                Activator.getDefault().logError(result.getMessage());
            }

        }
        return false;
    }

    private static boolean importTraceCopy(final String traceName, final File tracePath, final IProject project, final boolean overwrite) {
        try {
            IOverwriteQuery overwriteQuery = new IOverwriteQuery() {
                @Override
                public String queryOverwrite(String file) {
                    return overwrite ? IOverwriteQuery.ALL : IOverwriteQuery.NO_ALL;
                }
            };
            IFolder folder = getTraceFolder(project);
            importTrace(folder, tracePath, traceName, overwriteQuery);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private static IFolder getTraceFolder(final IProject project) {
        return (IFolder) project.findMember("Traces"); //$NON-NLS-1$
    }

    private static boolean isValidProject(final IProject project) {
        return (getTraceFolder(project) != null);
    }

    /**
     * Import a trace to the trace folder
     *
     * @param folder
     *            the trace folder resource
     * @param path
     *            the path to the trace to import
     * @param targetName
     *            the target name
     */
    private static void importTrace(final IFolder folder, final File source, final String targetName, final IOverwriteQuery overwriteQuery) {
        if (source.isDirectory()) {
            IPath containerPath = folder.getFullPath().addTrailingSeparator().append(targetName);

            List<File> filesToImport = Arrays.asList(source.listFiles());
            ImportOperation operation = new ImportOperation(
                    containerPath,
                    source,
                    FileSystemStructureProvider.INSTANCE,
                    overwriteQuery,
                    filesToImport);
            operation.setCreateContainerStructure(false);
            try {
                operation.run(new NullProgressMonitor());
            } catch (InvocationTargetException e) {
                displayException(e);
            } catch (InterruptedException e) {
                displayException(e);
            }
        } else {
            IRunnableWithProgress runnable = new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        InputStream inputStream = new FileInputStream(source);
                        IFile targetFile = folder.getFile(targetName);
                        targetFile.create(inputStream, IResource.NONE, monitor);
                    } catch (CoreException e) {
                        displayException(e);
                    } catch (FileNotFoundException e) {
                        displayException(e);
                    }
                }
            };
            WorkspaceModifyDelegatingOperation operation = new WorkspaceModifyDelegatingOperation(runnable);
            try {
                operation.run(new NullProgressMonitor());
            } catch (InvocationTargetException e) {
                displayException(e);
            } catch (InterruptedException e) {
                displayException(e);
            }
        }
    }

    private static void displayException(Exception e) {
        IStatus error = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString(), e);
        ErrorDialog ed = new ErrorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), e.getClass().getName(), Messages.ImportTraceWizard_ImportProblem, error, 0);
        ed.open();
    }

    /**
     * Sets the type to a trace resource
     *
     * @param traceTypeId
     *            The trace type, like org.eclipse.linuxtools.tmf.ctf
     * @param traceResource
     *            the trace resource like L/project/trace/
     * @return true if it succeeded.
     */
    public boolean setTraceType(final String traceTypeId, final IResource traceResource) {
        boolean retVal = false;
        if (traceResource != null) {
            try {
                // Set the trace properties for this resource
                boolean traceTypeOK = false;
                String traceBundle = null, traceIcon = null;
                IConfigurationElement ce = TmfTraceType.getInstance().getTraceAttributes(traceTypeId);
                if ((ce != null) && (ce.getContributor() != null)) {
                    traceTypeOK = true;
                    traceBundle = ce.getContributor().getName();
                    traceIcon = ce.getAttribute(TmfTraceType.ICON_ATTR);
                }
                final String traceType = traceTypeId;
                if (!traceTypeOK &&
                        (traceType.startsWith(TmfTraceType.CUSTOM_TXT_CATEGORY) ||
                        traceType.startsWith(TmfTraceType.CUSTOM_XML_CATEGORY))) {
                    // do custom trace stuff here
                    traceTypeOK = true;
                    traceBundle =
                            Activator.getDefault().getBundle().getSymbolicName();
                    traceIcon = DEFAULT_TRACE_ICON_PATH;
                }
                if (traceTypeOK) {
                    traceResource.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE,
                            traceBundle);
                    traceResource.setPersistentProperty(TmfCommonConstants.TRACETYPE,
                            traceTypeId);
                    traceResource.setPersistentProperty(TmfCommonConstants.TRACEICON,
                            traceIcon);
                }
                TmfProjectElement tmfProject =
                        TmfProjectRegistry.getProject(traceResource.getProject());
                if (tmfProject != null) {
                    for (TmfTraceElement traceElement : tmfProject.getTracesFolder().getTraces()) {
                        if (traceElement.getName().equals(traceResource.getName())) {
                            traceElement.refreshTraceType();
                            break;
                        }
                    }
                }
                retVal = true;
            } catch (CoreException e) {
                Activator.getDefault().logError("Error importing trace resource " + traceResource.getName(), e); //$NON-NLS-1$
                retVal = false;
            }
        }
        return retVal;
    }

}
