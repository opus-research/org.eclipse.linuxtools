package org.eclipse.linuxtools.internal.docker.core;

public class ContainerFileProxy {

	private String path;
	private String name;
	private boolean isFolder;

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
