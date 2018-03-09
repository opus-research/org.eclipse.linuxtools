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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.Createrepo;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.osgi.framework.FrameworkUtil;

/**
 * This class will contain the current project and basic operations of the
 * createrepo command.
 */
public class CreaterepoProject {

	private IEclipsePreferences projectPreferences;

	private IProject project;
	private IFolder content;
	private IFile repoFile;

	private IProgressMonitor monitor;

	/**
	 * Constructor without repo file.
	 *
	 * @param project The project.
	 * @throws CoreException Thrown when unable to initialize project.
	 */
	public CreaterepoProject(IProject project) throws CoreException {
		this(project, null);
	}

	/**
	 * Default constructor.
	 *
	 * @param project The project.
	 * @throws CoreException Thrown when unable to initialize project.
	 */
	public CreaterepoProject(IProject project, IFile repoFile) throws CoreException {
		this.project = project;
		this.repoFile = repoFile;
		monitor = new NullProgressMonitor();
		projectPreferences = new ProjectScope(project.getProject()).getNode(Activator.PLUGIN_ID);
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
		if (repoFile == null) {
			for (IResource child : getProject().members()) {
				String extension = child.getFileExtension();
				if (extension != null && extension.equals(ICreaterepoConstants.REPO_FILE_EXTENSION)) {
					// assumes that there will only be 1 .repo file in the folder
					repoFile = (IFile) child;
				}
				// if no repo file then keep it null
			}
		}
	}

	/**
	 * Create the content folder if it doesn't exist.
	 *
	 * @throws CoreException
	 */
	private void createContentFolder() throws CoreException {
		content = getProject().getFolder(ICreaterepoConstants.CONTENT_FOLDER);
		if (!content.exists()) {
			content.create(false, true, monitor);
		}
	}

	/**
	 * Import an RPM file outside of the eclipse workspace.
	 *
	 * @param externalFile The external file to import.
	 * @throws CoreException Thrown when failure to create a workspace file.
	 */
	public void importRPM(File externalFile) throws CoreException {
		// must put imported RPMs into the content folder; create if missing
		if (!getContentFolder().exists()) {
			createContentFolder();
		}
		IFile file = getContentFolder().getFile(new Path(externalFile.getName()));
		if (!file.exists()) {
			try {
				file.create(new FileInputStream(externalFile), false, monitor);
			} catch (FileNotFoundException e) {
				IStatus status = new Status(
						IStatus.ERROR,
						FrameworkUtil.getBundle(CreaterepoProject.class).getSymbolicName(),
						Messages.CreaterepoProject_errorGettingFile, null);
				throw new CoreException(status);
			}
			getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		}
	}

	/**
	 * Execute the createrepo command.
	 *
	 * @param os Direct execution stream to this.
	 * @return The status of the execution.
	 * @throws CoreException Thrown when failure to execute command.
	 */
	public IStatus createrepo(OutputStream os) throws CoreException {
		if (!getContentFolder().exists()) {
			createContentFolder();
		}
		Createrepo createrepo = new Createrepo();
		IStatus result = createrepo.execute(os, this, getCommandArguments());
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		return result;
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

	/**
	 * Get the RPMs in the project.
	 *
	 * @return A list of RPMs located within the project.
	 * @throws CoreException Thrown when unable to look into the project.
	 */
	public List<IResource> getRPMs() throws CoreException {
		List<IResource> rpms = new ArrayList<IResource>();
		if (!getContentFolder().exists()) {
			return rpms;
		}
		if (getProject().members().length > 0) {
			for (IResource child : getContentFolder().members()) {
				String extension = child.getFileExtension();
				if (extension != null && extension.equals(ICreaterepoConstants.RPM_FILE_EXTENSION)) {
					rpms.add(child);
				}
			}
		}
		return rpms;
	}

	/**
	 * Get the eclipse preferences of this project.
	 *
	 * @return The eclipse preferences for the project.
	 */
	public IEclipsePreferences getEclipsePreferences() {
		return projectPreferences;
	}

	/**
	 * Get the command arguments to pass to the createrepo command. The
	 * arguments come from the stored preferences from the preference page
	 * and the project preferences.
	 *
	 * @return The command arguments.
	 */
	private static List<String> getCommandArguments() {
		List<String> commands = new ArrayList<String>();
		return commands;
	}

}
