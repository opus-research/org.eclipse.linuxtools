/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.widgets.Display;

public class ProjectBuildListener implements IResourceChangeListener {

	private IProject project;
	public ProjectBuildListener(IProject targetProject) {
		project = targetProject;
	}

	public void resourceChanged(IResourceChangeEvent event) {
		int buildKind = event.getBuildKind();
		int eventType = event.getType();
		if (project != null
				&& eventType == IResourceChangeEvent.POST_BUILD
				&& (buildKind == IncrementalProjectBuilder.FULL_BUILD
				|| buildKind == IncrementalProjectBuilder.INCREMENTAL_BUILD
				|| buildKind == IncrementalProjectBuilder.CLEAN_BUILD)) {

			/*
			 * get changes from event and find the corresponding project from
			 * which to delete markers
			 */
			IResourceDelta rootDelta = event.getDelta();
			IResourceDelta[] childrenDelta = rootDelta.getAffectedChildren(IResourceDelta.CHANGED);
			for (IResourceDelta childDelta : childrenDelta) {
				IResource res = childDelta.getResource();
				if (childDelta.getKind() == IResourceDelta.CHANGED
						&& res != null
						&& res.getType() == IResource.PROJECT) {
					if (project.equals(res)) {
						try {
							// clear valgrind error view
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									ValgrindUIPlugin.getDefault().resetView();
								}
							});

							// remove project markers
							project.deleteMarkers(ValgrindLaunchPlugin.MARKER_TYPE, true, IResource.DEPTH_INFINITE);

							// de-register this listener, no longer needed since markers are now cleared
							ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
						} catch (CoreException e) {
							Status status = new Status(IStatus.ERROR, ValgrindLaunchPlugin.PLUGIN_ID, e.getMessage());
							ValgrindLaunchPlugin.getDefault().getLog().log(status);
						}

					}
				}
			}
		}
	}
}