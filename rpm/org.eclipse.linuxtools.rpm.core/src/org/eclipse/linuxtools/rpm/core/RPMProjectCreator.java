/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 ************************************************************************/
package org.eclipse.linuxtools.rpm.core;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;

/**
 * Utility class to ease creation of RPM projects.
 * 
 */
public class RPMProjectCreator {
	private RPMProjectLayout layout;

	/**
	 * Creates the utility class and sets the layout that will be used.
	 * 
	 * @param layout
	 *            The layout of the projects to be created.
	 */
	public RPMProjectCreator(RPMProjectLayout layout) {
		this.layout = layout;
	}

	/**
	 * Creates the utility class with the default(RPMBuild) layout.
	 */
	public RPMProjectCreator() {
		this(RPMProjectLayout.RPMBUILD);
	}

	/**
	 * Creates a project with the given name in the given location.
	 * 
	 * @param projectName
	 *            The name of the project.
	 * @param projectPath
	 *            The parent location of the project.
	 * @param monitor
	 *            Progress monitor to report back status.
	 * @return The newly created project.
	 */
	public IProject create(String projectName, IPath projectPath,
			IProgressMonitor monitor) {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(projectName);
			IProjectDescription description = ResourcesPlugin.getWorkspace()
					.newProjectDescription(project.getName());
			String parsedIPathString = null;
			if (!Platform.getLocation().equals(projectPath)) {
				parsedIPathString = projectPath.toString().replaceFirst(
						":/", "://"); //$NON-NLS-1$ //$NON-NLS-2$
				try {
					description.setLocationURI(new URI(parsedIPathString));
				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR,
							IRPMConstants.RPM_CORE_ID, e.getMessage(), e));
				}
			}

			description
					.setNatureIds(new String[] { IRPMConstants.RPM_NATURE_ID });
			project.create(description, monitor);

			monitor.worked(10);
			project.open(monitor);
			new RPMProject(project, layout);
			if (projectPath.toString().indexOf(':') != -1) {
				if (layout.equals(RPMProjectLayout.RPMBUILD)) {
					createDirsRemote(monitor, project, parsedIPathString);
				}
			} 
			return project;

		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void createDirsRemote(IProgressMonitor monitor, IProject project,
			String parsedIPathString) throws CoreException {
		IRemoteFileProxy proxy = null;
		try {
			proxy = RemoteProxyManager.getInstance().getFileProxy(
					new URI(parsedIPathString));
		} catch (URISyntaxException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					IRPMConstants.RPM_CORE_ID, e.getMessage(), e));
		}

		IFileStore iFileStoreSpecs = proxy
				.getResource(IRPMConstants.SPECS_FOLDER);
		iFileStoreSpecs.mkdir(EFS.NONE, monitor);
		project.getFolder(IRPMConstants.SPECS_FOLDER).create(true, true,
				monitor);

		IFileStore iFileStoreSources = proxy
				.getResource(IRPMConstants.SOURCES_FOLDER);
		iFileStoreSources.mkdir(EFS.NONE, monitor);
		project.getFolder(IRPMConstants.SOURCES_FOLDER).create(true, true,
				monitor);

		IFileStore iFileStoreBuild = proxy
				.getResource(IRPMConstants.BUILD_FOLDER);
		iFileStoreBuild.mkdir(EFS.NONE, monitor);
		project.getFolder(IRPMConstants.BUILD_FOLDER).create(true, true,
				monitor);

		IFileStore iFileStoreRPMs = proxy
				.getResource(IRPMConstants.RPMS_FOLDER);
		iFileStoreRPMs.mkdir(EFS.NONE, monitor);
		project.getFolder(IRPMConstants.RPMS_FOLDER)
				.create(true, true, monitor);

		IFileStore iFileStoreSRPMs = proxy
				.getResource(IRPMConstants.SRPMS_FOLDER);
		iFileStoreSRPMs.mkdir(EFS.NONE, monitor);
		project.getFolder(IRPMConstants.SRPMS_FOLDER).create(true, true,
				monitor);

	}

}
