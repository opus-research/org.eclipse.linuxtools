/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

public class ContainerFileProxy {

	private final String path;
	private final String name;
	private final boolean isFolder;

	public ContainerFileProxy(String directory, String name,
			boolean isFolder) {
		this.path = directory + (directory.equals("/") ? "" : "/") + name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.name = name;
		this.isFolder = isFolder;
	}

	public String getFullPath() {
		return path;
	}

	public String getLabel() {
		return name + (isFolder() ? "/" : "");
	}

	public boolean isFolder() {
		return isFolder;
	}

	@Override
	public String toString() {
		return getFullPath();
	}

}
