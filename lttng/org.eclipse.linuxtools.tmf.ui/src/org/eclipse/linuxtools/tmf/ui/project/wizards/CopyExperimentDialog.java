/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Copied and adapted from NewFolderDialog
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

/**
 * Implementation of the copy experiement dialog box.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class CopyExperimentDialog extends SelectionStatusDialog {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

	private final TmfExperimentElement fExperiment;
	private Text fNewExperimentName;
    private IFolder fExperimentFolder;
    private TmfProjectElement fProject;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param shell The parent shell
     * @param experiment The TMF experiment model element
     */
    public CopyExperimentDialog(Shell shell, TmfExperimentElement experiment) {
        super(shell);
        fExperiment = experiment;
        TmfExperimentFolder folder = (TmfExperimentFolder) experiment.getParent();
        fExperimentFolder = folder.getResource();
        fProject = experiment.getProject();
        setTitle(Messages.CopyExperimentDialog_DialogTitle);
        setStatusLineAboveButtons(true);
    }

    // ------------------------------------------------------------------------
    // Dialog
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createNewExperimentNameGroup(composite);
        return composite;
    }

    private void createNewExperimentNameGroup(Composite parent) {
        Font font = parent.getFont();
        Composite folderGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        folderGroup.setLayout(layout);
        folderGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Old experiment name label
        Label oldExperimentLabel = new Label(folderGroup, SWT.NONE);
        oldExperimentLabel.setFont(font);
        oldExperimentLabel.setText(Messages.CopyExperimentDialog_ExperimentName);

        // Old experiment name field
        Text oldExperimentName = new Text(folderGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        oldExperimentName.setLayoutData(data);
        oldExperimentName.setFont(font);
        oldExperimentName.setText(fExperiment.getName());
        oldExperimentName.setEnabled(false);

        // New experiment name label
        Label newExperimentLabel = new Label(folderGroup, SWT.NONE);
        newExperimentLabel.setFont(font);
        newExperimentLabel.setText(Messages.CopyExperimentDialog_ExperimentNewName);

        // New experiment name entry field
        fNewExperimentName = new Text(folderGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        fNewExperimentName.setLayoutData(data);
        fNewExperimentName.setFont(font);
        fNewExperimentName.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
            	validateNewExperimentName();
            }
        });
    }

    private void validateNewExperimentName() {

    	String name = fNewExperimentName.getText();
        IWorkspace workspace = fExperimentFolder.getWorkspace();
        IStatus nameStatus = workspace.validateName(name, IResource.FOLDER);

        if ("".equals(name)) { //$NON-NLS-1$
        	updateStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, Messages.Dialog_EmptyNameError, null));
        	return;
        }

        if (!nameStatus.isOK()) {
        	updateStatus(nameStatus);
        	return;
        }

        IPath path = new Path(name);
        if (fExperimentFolder.getFolder(path).exists() || fExperimentFolder.getFile(path).exists()) {
            updateStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, Messages.Dialog_ExistingNameError, null));
            return;
        }

        updateStatus(new Status(IStatus.OK, Activator.PLUGIN_ID, "")); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // SelectionStatusDialog
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
     */
    @Override
    protected void computeResult() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.dialogs.SelectionStatusDialog#create()
     */
    @Override
    public void create() {
        super.create();
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.dialogs.SelectionStatusDialog#okPressed()
     */
    @Override
    protected void okPressed() {
        IFolder folder = copyExperiment(fNewExperimentName.getText());
        if (folder == null) {
            return;
        }
        setSelectionResult(new IFolder[] { folder });
        super.okPressed();

        if (fProject != null) {
            fProject.refresh();
        }
    }

    private IFolder copyExperiment(final String newName) {

    	IPath oldPath = fExperiment.getResource().getFullPath();
    	final IPath newPath = oldPath.append("../" + newName); //$NON-NLS-1$

    	WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws CoreException {
                try {
                    monitor.beginTask("", 1000); //$NON-NLS-1$
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    // Copy supplementary files first
                    fExperiment.copySupplementaryFolder(newName);
                    fExperiment.getResource().copy(newPath, IResource.FORCE | IResource.SHALLOW, null);
                    // Delete any bookmarks file found in copied experiment folder
                    IFolder folder = fExperimentFolder.getFolder(newName);
                    if (folder.exists()) {
                        for (IResource member : folder.members()) {
                            if (TmfExperiment.class.getCanonicalName().equals(member.getPersistentProperty(TmfCommonConstants.TRACETYPE))) {
                                member.delete(true, null);
                            }
                        }
                    }
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(operation);
        } catch (InterruptedException exception) {
            return null;
        } catch (InvocationTargetException exception) {
            MessageDialog.openError(getShell(), "", NLS.bind("", exception.getTargetException().getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        } catch (RuntimeException exception) {
            return null;
        }

        return fExperiment.getResource();
    }

}
