/*******************************************************************************
* Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Patrick Tasse - Add support for DROP_LINK and rename prompt on name clash
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * Drop adapter assistant for project explorer
 */
public class DropAdapterAssistant extends CommonDropAdapterAssistant {

    /**
     * Default constructor
     */
    public DropAdapterAssistant() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#isSupportedType(org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public boolean isSupportedType(TransferData aTransferType) {
        return super.isSupportedType(aTransferType) || FileTransfer.getInstance().isSupportedType(aTransferType);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public IStatus validateDrop(Object target, int operation, TransferData transferType) {
        if (target instanceof TmfTraceFolder) {
            return Status.OK_STATUS;
        }
        if (target instanceof TmfExperimentElement) {
            return Status.OK_STATUS;
        }
        if (target instanceof TmfTraceElement) {
            ITmfProjectModelElement parent = ((TmfTraceElement) target).getParent();
            if (parent instanceof TmfTraceFolder) {
                return Status.OK_STATUS;
            }
            if (parent instanceof TmfExperimentElement) {
                return Status.OK_STATUS;
            }
        }
        if (target instanceof IProject) {
            return Status.OK_STATUS;
        }
        return Status.CANCEL_STATUS;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#handleDrop(org.eclipse.ui.navigator.CommonDropAdapter, org.eclipse.swt.dnd.DropTargetEvent, java.lang.Object)
     */
    @Override
    public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent, Object aTarget) {
        boolean ok = false;

        // Use local variable to avoid parameter assignment
        Object targetToUse = aTarget;

        int operation = aDropTargetEvent.detail;
        if (operation != DND.DROP_LINK) {
            operation = DND.DROP_COPY;
        }

        // If target is a trace, use its parent (either trace folder or experiment)
        if (targetToUse instanceof TmfTraceElement) {
            targetToUse = ((TmfTraceElement) targetToUse).getParent();
        }

        // If target is a project, use its trace folder
        if (targetToUse instanceof IProject) {
            TmfProjectElement projectElement = TmfProjectRegistry.getProject((IProject) targetToUse);
            if (projectElement != null) {
                targetToUse = projectElement.getTracesFolder();
            }
        }

        if (aDropTargetEvent.data instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) aDropTargetEvent.data;
            for (Object source : selection.toArray()) {
                if (source instanceof IResource) {
                    // If source resource is a trace, use the trace element
                    IResource sourceResource = (IResource) source;
                    TmfProjectElement projectElement = TmfProjectRegistry.getProject(sourceResource.getProject());
                    if (projectElement != null && projectElement.getTracesFolder() != null) {
                        for (TmfTraceElement trace : projectElement.getTracesFolder().getTraces()) {
                            if (trace.getResource().equals(sourceResource)) {
                                source = trace;
                                break;
                            }
                        }
                    }
                }
                if (source instanceof TmfTraceElement) {
                    TmfTraceElement sourceTrace = (TmfTraceElement) source;
                    // If source trace is under an experiment, use the original trace from the traces folder
                    sourceTrace = sourceTrace.getElementUnderTraceFolder();
                    if (targetToUse instanceof TmfExperimentElement) {
                        TmfExperimentElement targetExperiment = (TmfExperimentElement) targetToUse;
                        ok |= drop(sourceTrace, targetExperiment, operation);
                    } else if (targetToUse instanceof TmfTraceFolder) {
                        TmfTraceFolder traceFolder = (TmfTraceFolder) targetToUse;
                        ok |= drop(sourceTrace, traceFolder, operation);
                    }
                } else if (source instanceof IResource) {
                    IResource sourceResource = (IResource) source;
                    if (sourceResource.getType() != IResource.FILE && sourceResource.getType() != IResource.FOLDER) {
                        continue;
                    }
                    if (targetToUse instanceof TmfExperimentElement) {
                        TmfExperimentElement targetExperiment = (TmfExperimentElement) targetToUse;
                        ok |= (drop(sourceResource, targetExperiment, operation) != null);
                    } else if (targetToUse instanceof TmfTraceFolder) {
                        TmfTraceFolder traceFolder = (TmfTraceFolder) targetToUse;
                        ok |= (drop(sourceResource, traceFolder, operation) != null);
                    }
                }
            }
        } else if (aDropTargetEvent.data instanceof String[]) {
            String[] sources = (String[]) aDropTargetEvent.data;
            for (String source : sources) {
                Path path = new Path(source);
                if (targetToUse instanceof TmfExperimentElement) {
                    TmfExperimentElement targetExperiment = (TmfExperimentElement) targetToUse;
                    ok |= drop(path, targetExperiment, operation);
                } else if (targetToUse instanceof TmfTraceFolder) {
                    TmfTraceFolder traceFolder = (TmfTraceFolder) targetToUse;
                    ok |= drop(path, traceFolder, operation);
                }
            }
        }
        return (ok ? Status.OK_STATUS : Status.CANCEL_STATUS);
    }


    /**
     * Drop a trace by copying/linking a trace element in a target experiment
     *
     * @param sourceTrace the source trace element to copy
     * @param targetExperiment the target experiment
     * @param operation the drop operation (DND.DROP_COPY | DND.DROP_LINK)
     * @return true if successful
     */
    private static boolean drop(TmfTraceElement sourceTrace,
            TmfExperimentElement targetExperiment,
            int operation) {

        IResource sourceResource = sourceTrace.getResource();
        IResource targetResource = drop(sourceResource, targetExperiment, operation);

        if (targetResource != null) {
            IFolder destinationSupplementaryFolder = targetExperiment.getTraceSupplementaryFolder(targetResource.getName());
            sourceTrace.copySupplementaryFolder(destinationSupplementaryFolder);
            return true;
        }
        return false;
    }

    /**
     * Drop a trace by copying/linking a resource in a target experiment
     *
     * @param sourceResource the source resource
     * @param targetExperiment the target experiment
     * @param operation the drop operation (DND.DROP_COPY | DND.DROP_LINK)
     * @return the target resource or null if unsuccessful
     */
    private static IResource drop(IResource sourceResource,
            TmfExperimentElement targetExperiment,
            int operation) {

        IResource traceResource = sourceResource;

        TmfProjectElement projectElement = TmfProjectRegistry.getProject(sourceResource.getProject());
        for (TmfTraceElement trace : targetExperiment.getTraces()) {
            if (trace.getName().equals(sourceResource.getName()) && targetExperiment.getProject().equals(projectElement)) {
                return null;
            }
        }
        if (!targetExperiment.getProject().equals(projectElement)) {
            String targetName = sourceResource.getName();
            for (TmfTraceElement trace : targetExperiment.getProject().getTracesFolder().getTraces()) {
                if (trace.getName().equals(targetName)) {
                    targetName = promptRename(trace);
                    if (targetName == null) {
                        return null;
                    }
                    break;
                }
            }
            try {
                if (operation == DND.DROP_COPY) {
                    IPath destination = targetExperiment.getProject().getTracesFolder().getResource().getFullPath().addTrailingSeparator().append(targetName);
                    sourceResource.copy(destination, false, null);
                    cleanupBookmarks(destination);
                } else {
                    createLink(targetExperiment.getProject().getTracesFolder().getResource(), sourceResource, targetName);
                }
                // use the copied resource for the experiment
                if (sourceResource.getType() == IResource.FILE) {
                    traceResource = targetExperiment.getProject().getTracesFolder().getResource().getFile(targetName);
                } else if (sourceResource.getType() == IResource.FOLDER) {
                    traceResource = targetExperiment.getProject().getTracesFolder().getResource().getFolder(targetName);
                }
            } catch (CoreException e) {
                displayException(e);
                return null;
            }
        }
        if (traceResource != null && traceResource.exists()) {
            createLink(targetExperiment.getResource(), traceResource, traceResource.getName());
            return traceResource;
        }
        return null;
    }

    /**
     * Drop a trace by copying/linking a trace element in a trace folder
     *
     * @param sourceTrace the source trace
     * @param traceFolder the target trace folder
     * @param operation the drop operation (DND.DROP_COPY | DND.DROP_LINK)
     * @return true if successful
     */
    private static boolean drop(TmfTraceElement sourceTrace,
            TmfTraceFolder traceFolder,
            int operation) {

        IResource sourceResource = sourceTrace.getResource();
        IResource targetResource = drop(sourceResource, traceFolder, operation);

        if (targetResource != null) {
            IFolder destinationSupplementaryFolder = traceFolder.getTraceSupplementaryFolder(targetResource.getName());
            sourceTrace.copySupplementaryFolder(destinationSupplementaryFolder);
            return true;
        }
        return false;
    }

    /**
     * Drop a trace by copying/linking a resource in a trace folder
     *
     * @param sourceResource the source resource
     * @param traceFolder the target trace folder
     * @param operation the drop operation (DND.DROP_COPY | DND.DROP_LINK)
     * @return the target resource or null if unsuccessful
     */
    private static IResource drop(IResource sourceResource,
            TmfTraceFolder traceFolder,
            int operation) {

        if (sourceResource.getProject().equals(traceFolder.getResource().getProject())) {
            return null;
        }
        String targetName = sourceResource.getName();
        for (TmfTraceElement trace : traceFolder.getTraces()) {
            if (trace.getName().equals(targetName)) {
                targetName = promptRename(trace);
                if (targetName == null) {
                    return null;
                }
                break;
            }
        }
        try {
            if (operation == DND.DROP_COPY) {
                IPath destination = traceFolder.getResource().getFullPath().addTrailingSeparator().append(targetName);
                sourceResource.copy(destination, false, null);
                cleanupBookmarks(destination);
            } else {
                createLink(traceFolder.getResource(), sourceResource, targetName);
            }
            return traceFolder.getResource().findMember(targetName);
        } catch (CoreException e) {
            displayException(e);
        }
        return null;
    }

    /**
     * Drop a trace by importing/linking a path in a target experiment
     *
     * @param path the source path
     * @param targetExperiment the target experiment
     * @param operation the drop operation (DND.DROP_COPY | DND.DROP_LINK)
     * @return true if successful
     */
    private static boolean drop(Path path,
            TmfExperimentElement targetExperiment,
            int operation) {

        // Use local variable to avoid parameter assignment
        Path pathToUse = path;

        for (TmfTraceElement trace : targetExperiment.getTraces()) {
            if (trace.getName().equals(pathToUse.lastSegment()) && pathToUse.toString().startsWith(targetExperiment.getProject().getResource().getLocation().toString())) {
                return false;
            }
        }
        if (!pathToUse.toString().startsWith(targetExperiment.getProject().getResource().getLocation().toString())) {
            String targetName = pathToUse.lastSegment();
            for (TmfTraceElement trace : targetExperiment.getProject().getTracesFolder().getTraces()) {
                if (trace.getName().equals(targetName)) {
                    targetName = promptRename(trace);
                    if (targetName == null) {
                        return false;
                    }
                    break;
                }
            }
            if (operation == DND.DROP_COPY) {
                importTrace(targetExperiment.getProject().getTracesFolder().getResource(), pathToUse, targetName);
            } else {
                createLink(targetExperiment.getProject().getTracesFolder().getResource(), pathToUse, targetName);
            }
            // use the copied resource for the experiment
            IResource resource = null;
            File file = new File(pathToUse.toString());
            if (file.exists() && file.isFile()) {
                resource = targetExperiment.getProject().getTracesFolder().getResource().getFile(targetName);
            } else if (file.exists() && file.isDirectory()) {
                resource = targetExperiment.getProject().getTracesFolder().getResource().getFolder(targetName);
            }
            if (resource != null && resource.exists()) {
                createLink(targetExperiment.getResource(), resource, resource.getName());
                return true;
            }
        }
        return false;
    }

    /**
     * Drop a trace by importing/linking a path in a trace folder
     *
     * @param path the source path
     * @param traceFolder the target trace folder
     * @param operation the drop operation (DND.DROP_COPY | DND.DROP_LINK)
     * @return true if successful
     */
    private static boolean drop(Path path,
            TmfTraceFolder traceFolder,
            int operation) {

        String targetName = path.lastSegment();
        for (TmfTraceElement trace : traceFolder.getTraces()) {
            if (trace.getName().equals(targetName)) {
                targetName = promptRename(trace);
                if (targetName == null) {
                    return false;
                }
                break;
            }
        }
        if (operation == DND.DROP_COPY) {
            importTrace(traceFolder.getResource(), path, targetName);
        } else {
            createLink(traceFolder.getResource(), path, targetName);
        }
        return true;
    }

    /**
     * Import a trace to the trace folder
     *
     * @param folder the trace folder resource
     * @param path the path to the trace to import
     * @param targetName the target name
     */
    private static void importTrace(final IFolder folder, final Path path, final String targetName) {
        final File source = new File(path.toString());
        if (source.isDirectory()) {
            IPath containerPath = folder.getFullPath().addTrailingSeparator().append(targetName);
            IOverwriteQuery overwriteImplementor = new IOverwriteQuery() {
                @Override
                public String queryOverwrite(String pathString) {
                    return IOverwriteQuery.NO_ALL;
                }
            };
            List<File> filesToImport = Arrays.asList(source.listFiles());
            ImportOperation operation = new ImportOperation(
                    containerPath,
                    source,
                    FileSystemStructureProvider.INSTANCE,
                    overwriteImplementor,
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

    /**
     * Create a link to the actual trace and set the trace type
     *
     * @param parentFolder the parent folder
     * @param resource the resource
     * @param targetName the target name
     */
    private static void createLink(IFolder parentFolder, IResource resource, String targetName) {
        IPath location = resource.getLocation();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            Map<QualifiedName, String> properties = resource.getPersistentProperties();
            String bundleName = properties.get(TmfCommonConstants.TRACEBUNDLE);
            String traceType = properties.get(TmfCommonConstants.TRACETYPE);
            String iconUrl = properties.get(TmfCommonConstants.TRACEICON);
            String supplFolder = properties.get(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER);

            if (resource instanceof IFolder) {
                IFolder folder = parentFolder.getFolder(targetName);
                if (workspace.validateLinkLocation(folder, location).isOK()) {
                    folder.createLink(location, IResource.REPLACE, null);
                    setProperties(folder, bundleName, traceType, iconUrl, supplFolder);

                } else {
                    Activator.getDefault().logError("Invalid Trace Location"); //$NON-NLS-1$
                }
            } else {
                IFile file = parentFolder.getFile(targetName);
                if (workspace.validateLinkLocation(file, location).isOK()) {
                    file.createLink(location, IResource.REPLACE, null);
                    setProperties(file, bundleName, traceType, iconUrl, supplFolder);
                } else {
                    Activator.getDefault().logError("Invalid Trace Location"); //$NON-NLS-1$
                }
            }
        } catch (CoreException e) {
            displayException(e);
        }
    }

    /**
     * Create a link to a file or folder
     *
     * @param parentFolder the parent folder
     * @param source the file or folder
     * @param targetName the target name
     */
    private static void createLink(IFolder parentFolder, IPath location, String targetName) {
        File source = new File(location.toString());
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {

            if (source.isDirectory()) {
                IFolder folder = parentFolder.getFolder(targetName);
                if (workspace.validateLinkLocation(folder, location).isOK()) {
                    folder.createLink(location, IResource.REPLACE, null);
                } else {
                    Activator.getDefault().logError("Invalid Trace Location"); //$NON-NLS-1$
                }
            } else {
                IFile file = parentFolder.getFile(targetName);
                if (workspace.validateLinkLocation(file, location).isOK()) {
                    file.createLink(location, IResource.REPLACE, null);
                } else {
                    Activator.getDefault().logError("Invalid Trace Location"); //$NON-NLS-1$
                }
            }
        } catch (CoreException e) {
            displayException(e);
        }
    }

    /**
     * Prompts the user to rename a trace
     *
     * @param trace the existing trace
     * @return the new name to use or null if rename is canceled
     */
    private static String promptRename(TmfTraceElement trace) {
        MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_QUESTION | SWT.CANCEL | SWT.OK);
        mb.setText(Messages.DropAdapterAssistant_RenameTraceTitle);
        mb.setMessage(NLS.bind(Messages.DropAdapterAssistant_RenameTraceMessage, trace.getName()));
        if (mb.open() != SWT.OK) {
            return null;
        }
        IFolder folder = trace.getProject().getTracesFolder().getResource();
        int i = 2;
        while (true) {
            String name = trace.getName() + '-' + Integer.toString(i++);
            IResource resource = folder.findMember(name);
            if (resource == null) {
                return name;
            }
        }
    }

    /**
     * Cleanup bookmarks file in copied trace
     */
    private static void cleanupBookmarks(IPath path) {
        IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
        if (folder.exists()) {
            try {
                for (IResource member : folder.members()) {
                    if (TmfTrace.class.getCanonicalName().equals(member.getPersistentProperty(TmfCommonConstants.TRACETYPE))) {
                        member.delete(true, null);
                    }
                }
            } catch (CoreException e) {
                displayException(e);
            }
        }
    }

    /**
     * Set the trace persistent properties
     *
     * @param resource the trace resource
     * @param bundleName the bundle name
     * @param traceType the trace type
     * @param iconUrl the icon URL
     * @param supplFolder the directory of the directory for supplementary information or null to ignore the property
     * @throws CoreException
     */
    private static void setProperties(IResource resource, String bundleName,
            String traceType, String iconUrl, String supplFolder)
            throws CoreException {
        resource.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE, bundleName);
        resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, traceType);
        resource.setPersistentProperty(TmfCommonConstants.TRACEICON, iconUrl);
        resource.setPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER, supplFolder);
    }

    /**
     * Display an exception in a message box
     *
     * @param e the exception
     */
    private static void displayException(Exception e) {
        MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        mb.setText(e.getClass().getName());
        mb.setMessage(e.getMessage());
        mb.open();
    }

}
