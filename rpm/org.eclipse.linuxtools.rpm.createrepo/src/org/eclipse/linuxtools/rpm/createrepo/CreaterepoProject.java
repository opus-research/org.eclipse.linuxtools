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
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;

/**
 * This class will contain the current project and basic operations of the
 * createrepo command.
 */
public class CreaterepoProject {

	private IProject project;
	private IFolder repodata;

	private IProgressMonitor monitor;
	private IEclipsePreferences projectPreferences;

	/**
	 * Default constructor.
	 *
	 * @param project The project.
	 * @throws CoreException Thrown when unable to initialize project.
	 */
	public CreaterepoProject(IProject project) throws CoreException {
		this.project = project;
		monitor = new NullProgressMonitor();
		projectPreferences = new ProjectScope(project.getProject())
				.getNode(Activator.PLUGIN_ID);
		intitialize();
		// if something is deleted from the project while outside of eclipse,
		// the
		// tree/preferences will be updated accordingly after refreshing
		getProject().refreshLocal(IResource.DEPTH_ONE, monitor);
	}

	/**
	 * Initialize the createrepo project by creating the repodata if it doesn't
	 * yet exist. Also update the included package list to make sure any
	 * included packages aren't missing in the project.
	 *
	 * @throws CoreException Thrown when unable to create the folders.
	 */
	private void intitialize() throws CoreException {
		repodata = project.getFolder(ICreaterepoConstants.REPODATA_FOLDER);
		if (!repodata.exists()) {
			repodata.create(false, true, monitor);
		}
		String includeRPMPrefValue = projectPreferences.get(
				CreaterepoPreferenceConstants.PREF_INC_RPM_LIST,
				ICreaterepoConstants.EMPTY_STRING);
		// if the project preferences have some RPMs already stored within it
		if (!includeRPMPrefValue.equals(ICreaterepoConstants.EMPTY_STRING)) {
			// update to make sure that the RPMs in the preferences exist in the
			// project
			List<IResource> rpmsInProject = getProjectRPMs();
			List<String> updatedPreferences = new ArrayList<String>();

			// for each of the current RPMs already in the preferences, save the
			// ones that have
			// their RPMs in the project into a list.
			for (String str : Arrays.asList(includeRPMPrefValue
					.split(ICreaterepoConstants.DELIMITER))) {
				if (rpmsInProject.contains(getProject().findMember(str))) {
					updatedPreferences.add(str);
				}
			}

			// The preferences will be updated to become this new list to ensure
			// that all the RPMs
			// in the preferences each have an existing RPM in the project.
			String updatedPreferenceString = ICreaterepoConstants.EMPTY_STRING;
			for (String string : updatedPreferences) {
				updatedPreferenceString = updatedPreferenceString.concat(string
						+ ICreaterepoConstants.DELIMITER);
			}
			// remove the hanging delimiter if not empty
			if (!updatedPreferenceString.isEmpty()) {
				updatedPreferenceString = updatedPreferenceString.substring(0,
						updatedPreferenceString.length() - 1);
			}
			try {
				projectPreferences.put(
						CreaterepoPreferenceConstants.PREF_INC_RPM_LIST,
						updatedPreferenceString);
				projectPreferences.flush();
			} catch (BackingStoreException e) {
				IStatus status = new Status(IStatus.ERROR, FrameworkUtil
						.getBundle(CreaterepoProject.class).getSymbolicName(),
						Messages.CreaterepoProject_errorSettingPreferences,
						null);
				throw new CoreException(status);
			}
		}
	}

	/**
	 * Import an RPM file outside of the eclipse workspace.
	 *
	 * @param externalFile The external file to import.
	 * @throws CoreException Thrown when failure to create a workspace file.
	 */
	public void importRPM(File externalFile) throws CoreException {
		IFile file = getProject().getFile(new Path(externalFile.getName()));
		if (!file.exists()) {
			try {
				file.create(new FileInputStream(externalFile), false, monitor);
			} catch (FileNotFoundException e) {
				IStatus status = new Status(IStatus.ERROR, FrameworkUtil
						.getBundle(CreaterepoProject.class).getSymbolicName(),
						Messages.CreaterepoProject_errorGettingFile, null);
				throw new CoreException(status);
			}
			getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
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
	 * Get the repodata folder.
	 *
	 * @return The repodata folder.
	 */
	public IFolder getRepodataFolder() {
		return repodata;
	}

	/**
	 * Get the repomd.xml file from the repodata folder.
	 *
	 * @return The repomd.xml file.
	 */
	public IResource getRepomdFile() {
		return getRepodataFolder().findMember(
				ICreaterepoConstants.REPO_METADATA_FILE);
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
	 * Get the RPMs in the project.
	 *
	 * @return A list of RPMs found in the root of the project.
	 * @throws CoreException Thrown when unable to look into the project.
	 */
	public List<IResource> getProjectRPMs() throws CoreException {
		List<IResource> rpms = new ArrayList<IResource>();
		if (getProject().members().length > 0) {
			for (IResource child : getProject().members()) {
				String extension = child.getFileExtension();
				if (extension != null
						&& extension
								.equals(ICreaterepoConstants.RPM_FILE_EXTENSION)) {
					rpms.add(child);
				}
			}
		}
		return rpms;
	}

	/**
	 * Get the RPMs to create a repository for, as stored in the project
	 * preferences. An empty list will be returned if no RPM is specified.
	 *
	 * @return A list of RPMs to create a repository for.
	 */
	public List<IResource> getIncludedPreferenceRPMs() {
		String pkglist = projectPreferences.get(
				CreaterepoPreferenceConstants.PREF_INC_RPM_LIST,
				ICreaterepoConstants.EMPTY_STRING).trim();
		List<IResource> rpms = new ArrayList<IResource>();
		if (!pkglist.isEmpty()) {
			for (String str : pkglist.split(ICreaterepoConstants.DELIMITER)) {
				IResource rpmToAdd = getProject().findMember(str);
				if (rpmToAdd != null && rpmToAdd.exists()) {
					rpms.add(rpmToAdd);
				}
			}
		}
		return rpms;
	}

}
