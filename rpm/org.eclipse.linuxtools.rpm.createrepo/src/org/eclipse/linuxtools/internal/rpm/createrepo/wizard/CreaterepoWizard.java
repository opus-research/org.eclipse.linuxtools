/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.wizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProjectCreator;
import org.eclipse.linuxtools.rpm.createrepo.ICreaterepoConstants;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;

/**
 * This wizard will guide the user to creating a createrepo project. It will
 * allow the user to optionally import RPMs during the process.
 */
public class CreaterepoWizard extends Wizard implements INewWizard {

	/*
	 * TODO: new page to also allow user option to create a .repo file
	 */

	private CreaterepoNewProjectWizardPage page1;
	private IProject project;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		super.addPages();
		page1 = new CreaterepoNewProjectWizardPage(
				Messages.CreaterepoNewProjectWizardPage_wizardPageName);
		addPage(page1);
	}

	/**
	 * Pressing Finish will create the project and initialize an empty
	 * repomd.xml file.
	 */
	public boolean performFinish() {
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor)
					throws CoreException {
				createProject(monitor);
			}
		};
		try {
			getContainer().run(false, true, op);
		} catch (InvocationTargetException e) {
			Activator.logError(Messages.CreaterepoWizard_errorCreatingProject,
					e);
		} catch (InterruptedException e) {
			Activator.logError(Messages.CreaterepoWizard_errorCreatingProject,
					e);
		}
		return true;
	}

	/**
	 * Create an empty createrepo project that contains an empty repomd.xml
	 * file. Open it using the default repomd.xml editor.
	 *
	 * @param monitor The progress monitor.
	 */
	protected void createProject(IProgressMonitor monitor) {
		try {
			project = CreaterepoProjectCreator.create(page1.getProjectName(),
					page1.getLocationPath(), monitor);
			IFolder folder = project
					.getFolder(ICreaterepoConstants.REPODATA_FOLDER);
			if (!folder.exists()) {
				folder.create(false, true, monitor);
			}
			final IFile file = folder
					.getFile(ICreaterepoConstants.REPO_METADATA_FILE);
			InputStream stream = new ByteArrayInputStream(
					ICreaterepoConstants.EMPTY_STRING.getBytes());
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			monitor.worked(1);
			monitor.setTaskName(Messages.CreaterepoWizard_openFileOnCreation);
			getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, file, true);
					} catch (PartInitException e) {
						Activator
								.logError(
										Messages.CreaterepoWizard_errorOpeningNewlyCreatedFile,
										e);
					}
				}
			});
			monitor.worked(1);
		} catch (CoreException e) {
			Activator.logError(Messages.CreaterepoWizard_errorCreatingProject,
					e);
		}
	}

}