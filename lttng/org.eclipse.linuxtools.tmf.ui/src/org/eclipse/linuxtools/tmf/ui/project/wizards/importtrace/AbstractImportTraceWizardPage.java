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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.linuxtools.tmf.core.TmfProjectNature;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.wizards.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardResourceImportPage;

/**
 * @author ematkho
 * @since 2.0
 */
public abstract class AbstractImportTraceWizardPage extends WizardResourceImportPage {

    /**
     * Import String
     */
    protected static final String BATCH_IMPORT_WIZARD_PAGE = "BatchImportWizardPage"; //$NON-NLS-1$
    protected IFolder fTargetFolder;
    protected IProject fProject;
    private BatchImportTraceWizard bitw;

    /**
     * @param name
     * @param selection
     */
    protected AbstractImportTraceWizardPage(String name, IStructuredSelection selection) {
        super(name, selection);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructor
     *
     * @param workbench
     *            The workbench reference.
     * @param selection
     *            The current selection
     */
    public AbstractImportTraceWizardPage(IWorkbench workbench, IStructuredSelection selection) {
        this(BATCH_IMPORT_WIZARD_PAGE, selection);
        setTitle(Messages.ImportTraceWizard_FileSystemTitle);
        setDescription(Messages.ImportTraceWizard_DialogTitle);

        // Locate the target trace folder
        IFolder traceFolder = null;
        Object element = selection.getFirstElement();

        if (element instanceof TmfTraceFolder) {
            TmfTraceFolder tmfTraceFolder = (TmfTraceFolder) element;
            fProject = (tmfTraceFolder.getProject().getResource());
            traceFolder = tmfTraceFolder.getResource();
        } else if (element instanceof IProject) {
            IProject project = (IProject) element;
            try {
                if (project.hasNature(TmfProjectNature.ID)) {
                    traceFolder = (IFolder) project.findMember(TmfTraceFolder.TRACE_FOLDER_NAME);
                }
            } catch (CoreException e) {
            }
        }

        // Set the target trace folder
        if (traceFolder != null) {
            fTargetFolder = (traceFolder);
            String path = traceFolder.getFullPath().toOSString();
            setContainerFieldValue(path);
        }

    }

    /**
     * @return the Batch Import Wizard
     */
    public BatchImportTraceWizard getBatchWizard() {
        return bitw;
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setFont(parent.getFont());
        this.setControl(composite);
        bitw = (BatchImportTraceWizard) getWizard();
    }

    @Override
    protected void createSourceGroup(Composite parent) {
        // TODO Auto-generated method stub

    }

    @Override
    protected ITreeContentProvider getFileProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ITreeContentProvider getFolderProvider() {
        // TODO Auto-generated method stub
        return null;
    }

}
