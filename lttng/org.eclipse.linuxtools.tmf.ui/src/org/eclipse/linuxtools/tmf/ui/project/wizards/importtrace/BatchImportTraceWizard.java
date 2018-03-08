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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.ui.project.wizards.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * Batch Import trace wizard
 *
 * @author Matthew Khouzam
 * @since 2.0
 *
 */
public class BatchImportTraceWizard extends ImportTraceWizard {

    private static final String DEFAULT_TRACE_ICON_PATH = "icons/elcl16/trace.gif"; //$NON-NLS-1$

    private IWizardPage fSelectDirectories;
    private IWizardPage fScanPage;
    private IWizardPage fSelectPage;
    private IWizardPage fImportOptions;

    private Set<FileAndName> fTraces = new TreeSet<FileAndName>();

    private List<String> fTracesToScan;
    private Set<String> fFilesToScan = new TreeSet<String>();

    private ImportTraceContentProvider fScannedTraces = new ImportTraceContentProvider();

    private Map<String, File> fSelectedFiles = new HashMap<String, File>();

    private boolean fReset = true;
    private IPath fResourcePath;
    private boolean fOverwrite = true;
    private boolean fLinked = true;

    /**
     * reset the scan
     */
    public void reset() {
        fReset = true;
    }

    /**
     * @return if the importer should restart or not
     */
    public boolean isReset() {
        return fReset;
    }

    public void set() {
        fReset = false;
    }

    /**
     * @return the ScannedTraces model
     */
    public ImportTraceContentProvider getScannedTraces() {
        return fScannedTraces;
    }

    static private final String BATCH_IMPORT_WIZARD = "BatchImportTraceWizard"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public BatchImportTraceWizard() {
        IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(BATCH_IMPORT_WIZARD);
        if (section == null) {
            section = workbenchSettings.addNewSection(BATCH_IMPORT_WIZARD);
        }
        setDialogSettings(section);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {

        fSelectDirectories = new ImportTraceWizardPageSelectDirectories(workbench, selection);
        fScanPage = new ImportTraceWizardPageScan(workbench, selection);
        fSelectPage = new ImportTraceWizardPageSelectTraceType(workbench, selection);
        fImportOptions = new ImportTraceWizardPageOptions(workbench, selection);
        setNeedsProgressMonitor(true);

    }

    @Override
    public void addPages() {
        addPage(fSelectPage);
        addPage(fSelectDirectories);
        addPage(fScanPage);
        addPage(fImportOptions);

    }

    /**
     * Add a file to the list
     *
     * @param name
     *            the name of the file
     * @param file
     *            the file object
     */
    public void addFile(String name, File file) {
        fSelectedFiles.put(name, file);
        reset();
    }

    /**
     * remove files from selection
     *
     * @param name
     *            the name of the file to remove
     */
    public void removeFile(String name) {
        fSelectedFiles.remove(name);
        reset();
    }

    /**
     * @return the set of names of the selected files
     */
    public Set<String> getFileNames() {
        return fSelectedFiles.keySet();
    }

    /**
     * reset the trace list to import
     */
    public void clearTraces() {
        fTraces.clear();
    }

    @Override
    public boolean performFinish() {
        if (fTraces.isEmpty()) {
            return false;
        }
        boolean success = importTraces();
        return success;

    }

    private boolean importTraces() {
        boolean success = false;
        IOverwriteQuery overwriteQuery = new IOverwriteQuery() {
            @Override
            public String queryOverwrite(String file) {
                return fOverwrite ? IOverwriteQuery.ALL : IOverwriteQuery.NO_ALL;
            }
        };
        FileSystemStructureProvider fileSystemStructureProvider = FileSystemStructureProvider.INSTANCE;

        for (FileAndName traceToImport : fTraces) {
            try {
                if (fLinked) {
                    IFolder resourceFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(fResourcePath);
                    createLink(resourceFolder, Path.fromOSString(traceToImport.getFile().getAbsolutePath()), traceToImport.getName());
                    setTraceType(traceToImport);
                    success = true;
                }
                else {
                    List<File> subList = new ArrayList<File>();
                    subList.add(traceToImport.getFile());
                    ImportOperation operation = new ImportOperation(fResourcePath.append(traceToImport.getName()),
                            traceToImport.getFile().getParentFile(), fileSystemStructureProvider, overwriteQuery,
                            subList);
                    operation.setContext(getShell());
                    operation.setCreateLinks(fLinked);
                    if (executeImportOperation(operation)) {
                        setTraceType(traceToImport);
                        success = true;
                    }
                }
            } catch (Exception e) {
            }
        }
        return success;
    }

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

        }
    }

    private void setTraceType(FileAndName traceToImport) {
        IPath path = fResourcePath.append(traceToImport.getName());
        IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        if (resource != null) {
            try {
                // Set the trace properties for this resource
                IConfigurationElement ce = traceToImport.getConfigurationElement();
                boolean traceTypeOK = false;
                String traceBundle = null, traceTypeId = null, traceIcon = null;
                if (ce != null) {
                    traceTypeOK = true;
                    traceBundle = ce.getContributor().getName();
                    traceTypeId = ce.getAttribute(TmfTraceType.ID_ATTR);
                    traceIcon = ce.getAttribute(TmfTraceType.ICON_ATTR);
                }
                final String traceType = traceToImport.getTraceType().getName();
                if (!traceTypeOK &&
                        (traceType.startsWith(ImportUtils.CUSTOM_TXT_CATEGORY) || traceType.startsWith(ImportUtils.CUSTOM_XML_CATEGORY))) {
                    // do custom trace stuff here
                    traceTypeOK = true;
                    traceBundle = Activator.getDefault().getBundle().getSymbolicName();

                    traceTypeId = CustomTxtTrace.class.getCanonicalName() + ":" + traceType; //$NON-NLS-1$
                    traceIcon = DEFAULT_TRACE_ICON_PATH;
                }
                if (traceTypeOK) {
                    resource.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE, traceBundle);
                    resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, traceTypeId);
                    resource.setPersistentProperty(TmfCommonConstants.TRACEICON, traceIcon);
                }
                TmfProjectElement tmfProject = TmfProjectRegistry.getProject(resource.getProject());
                if (tmfProject != null) {
                    for (TmfTraceElement traceElement : tmfProject.getTracesFolder().getTraces()) {
                        if (traceElement.getName().equals(resource.getName())) {
                            traceElement.refreshTraceType();
                            break;
                        }
                    }
                }
            } catch (CoreException e) {
                Activator.getDefault().logError("Error importing trace resource " + resource.getName(), e); //$NON-NLS-1$
            }
        }
    }

    @Override
    public boolean canFinish() {
        return super.canFinish() && fTraces.size() > 0 && !hasConflicts();
    }

    /**
     * Reset the files to scan
     */
    public void clearFilesToScan() {
        fFilesToScan.clear();
    }

    /**
     * Add a file to scan
     * @param fileName the file to scan
     */
    public void AddFileToScan(String fileName) {
        fFilesToScan.add(fileName);
    }

    /**
     * get the files to scan
     * @return the files to scan
     */
    public Set<String> getFilesToScan() {
        return fFilesToScan;
    }

    /**
     * is this file already going to be scanned?
     * @param absolutePath the file to scan
     * @return true if yes, false, if no
     */
    public boolean HasFileToScan(String absolutePath) {
        return fFilesToScan.contains(absolutePath);
    }

    /**
     * Set the trace types to scan
     * @param tracesToScan a list of trace types to scan for
     */
    public void setTracesToScan(List<String> tracesToScan) {
        this.fTracesToScan = tracesToScan;
    }

    /**
     * get the trace types to scan
     * @return a list of traces to Scan for
     */
    public List<String> getTracesToScan() {
        return fTracesToScan;
    }

    /**
     * Add files to Import
     * @param element add the file and tracetype to import
     */
    public void addFileToImport(FileAndName element) {
        fTraces.add(element);
        updateConflicts();
    }

    /**
     * remove the file to scan
     * @param element
     */
    public void removeFileToImport(FileAndName element) {
        fTraces.remove(element);
        element.setConflictingName(false);
        updateConflicts();
    }

    /**
     * Updates the trace to see if there are conflicts.
     */
    public void updateConflicts() {
        final FileAndName[] fChildren = fTraces.toArray(new FileAndName[0]);
        for (int i = 0; i < fChildren.length; i++) {
            fChildren[i].setConflictingName(false);
        }
        for (int i = 1; i < fChildren.length; i++) {
            for (int j = 0; j < i; j++) {
                if (fChildren[i].getName().equals(fChildren[j].getName())) {
                    fChildren[i].setConflictingName(true);
                    fChildren[j].setConflictingName(true);
                }
            }
        }
        getContainer().updateButtons();
    }

    private boolean hasConflicts() {
        boolean conflict = false;
        for (FileAndName child : fTraces) {
            conflict |= child.isConflictingName();
        }
        return conflict;
    }

    private boolean executeImportOperation(ImportOperation op) {
        initializeOperation(op);

        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            System.out.println(e.getTargetException());
            return false;
        }

        IStatus status = op.getStatus();
        if (!status.isOK()) {
            ErrorDialog.openError(getContainer().getShell(), Messages.ImportTraceWizard_ImportProblem, null, status);
            return false;
        }

        return true;
    }

    private static void initializeOperation(ImportOperation op) {
        op.setCreateContainerStructure(false);
        op.setOverwriteResources(false);
        op.setCreateLinks(true);
        op.setVirtualFolders(false);
    }

    /**
     * set the resourcePath
     * @param resourcePath the resource path
     */
    public void setResourcePath(IPath resourcePath) {
        fResourcePath = resourcePath;

    }

    /**
     * override existing resources
     * @param selection true or false
     */
    public void setOverwrite(boolean selection) {
        fOverwrite = selection;
    }

    /**
     * is the trace linked?
     * @param isLink true or false
     */
    public void setLinked(boolean isLink) {
        fLinked = isLink;
    }

}
