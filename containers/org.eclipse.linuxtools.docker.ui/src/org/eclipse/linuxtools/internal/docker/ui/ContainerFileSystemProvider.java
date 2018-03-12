/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.core.ContainerFileProxy;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

public class ContainerFileSystemProvider implements IImportStructureProvider {

	private final IDockerConnection connection;
	private final String containerId;

	public ContainerFileSystemProvider(IDockerConnection connection,
			String containerId) {
		this.connection = connection;
		this.containerId = containerId;
	}

	private class ReadContainerDirectoryThread extends Thread {
		private Object element;
		private String containerId;
		private List<ContainerFileProxy> childList;

		public ReadContainerDirectoryThread(Object element, String containerId,
				List<ContainerFileProxy> childList) {
			this.element = element;
			this.containerId = containerId;
			this.childList = childList;
		}

		@Override
		public void run() {
			try {
				ContainerFileProxy proxy = (ContainerFileProxy) element;
				if (proxy.isFolder()) {
					List<ContainerFileProxy> children = ((DockerConnection) connection)
							.readContainerDirectory(containerId,
									proxy.getFullPath());
					childList.addAll(children);
				}
			} catch (DockerException e) {
				e.printStackTrace();
			}

		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getChildren(Object element) {
		List<ContainerFileProxy> childList = new ArrayList<>();
		Thread t = new ReadContainerDirectoryThread(element, containerId,
				childList);
		t.start();
		try {
			t.join(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return childList;
	}

	@Override
	public InputStream getContents(Object element) {
		return null; // we do not have the contents of container file
	}

	@Override
	public String getFullPath(Object element) {
		return ((ContainerFileProxy) element).getFullPath();
	}

	@Override
	public String getLabel(Object element) {
		return ((ContainerFileProxy) element).getLabel();
	}

	@Override
	public boolean isFolder(Object element) {
		return ((ContainerFileProxy) element).isFolder();
	}

}
