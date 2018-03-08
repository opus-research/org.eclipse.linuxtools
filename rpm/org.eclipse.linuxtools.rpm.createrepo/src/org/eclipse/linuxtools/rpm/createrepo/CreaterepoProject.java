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
package org.eclipse.linuxtools.rpm.createrepo;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * This class will contain the current project and basic operations of the
 * createrepo command.
 */
public class CreaterepoProject {

	private IProject project;
	private IFolder content;
	private IFile repoFile;

	private IProgressMonitor monitor;

	/**
	 * Default constructor.
	 *
	 * @param project The project.
	 * @throws CoreException Thrown when unable to initialize project.
	 */
	public CreaterepoProject(IProject project) throws CoreException {
		this.project = project;
		monitor = new NullProgressMonitor();
		intitialize();
		// if something is deleted from the project while outside of eclipse,
		// the tree/preferences will be updated accordingly after refreshing
		getProject().refreshLocal(IResource.DEPTH_ONE, monitor);
	}

	/**
	 * Initialize the createrepo project by creating the content folder if it doesn't
	 * yet exist.
	 *
	 * @throws CoreException Thrown when unable to create the folders.
	 */
	private void intitialize() throws CoreException {
		content = getProject().getFolder(ICreaterepoConstants.CONTENT_FOLDER);
		if (!content.exists()) {
			content.create(false, true, monitor);
		}
		// tries to check if a .repo file already exists in the project
		for (IResource child : getProject().members()) {
			String extension = child.getFileExtension();
			if (extension != null && extension.equals(ICreaterepoConstants.REPO_FILE_EXTENSION)) {
				// assumes that there will only be 1 .repo file in the folder
				repoFile = (IFile) child;
			}
			// if no repo file then keep it null
		}
	}

	/**
	 * Get the project.
	 *
	 * @return The project.
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * Get the content folder.
	 *
	 * @return The content folder.
	 */
	public IFolder getContentFolder() {
		return content;
	}

	/**
	 * Get the .repo file.
	 *
	 * @return The .repo file.
	 */
	public IFile getRepoFile() {
		return repoFile;
	}

}
