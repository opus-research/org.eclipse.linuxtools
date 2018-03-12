package org.eclipse.linuxtools.internal.docker.ui;

/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * This class provides information regarding the structure and content of
 * specified file system File objects.
 */
public class FileSystemStructureProvider implements IImportStructureProvider {

	/**
	 * Holds a singleton instance of this class.
	 */
	public final static FileSystemStructureProvider INSTANCE = new FileSystemStructureProvider();

	/**
	 * Creates an instance of <code>FileSystemStructureProvider</code>.
	 */
	private FileSystemStructureProvider() {
		super();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List getChildren(Object element) {
		File folder = (File) element;
		String[] children = folder.list();
		int childrenLength = children == null ? 0 : children.length;
		List result = new ArrayList(childrenLength);

		for (int i = 0; i < childrenLength; i++) {
			result.add(new File(folder, children[i]));
		}

		return result;
	}

	@Override
	public InputStream getContents(Object element) {
		try {
			return new FileInputStream((File) element);
		} catch (FileNotFoundException e) {
			Activator.log(e);
			return null;
		}
	}

	@Override
	public String getFullPath(Object element) {
		return ((File) element).getPath();
	}

	@Override
	public String getLabel(Object element) {

		// Get the name - if it is empty then return the path as it is a file
		// root
		File file = (File) element;
		String name = file.getName();
		if (name.length() == 0) {
			return file.getPath();
		}
		return name;
	}

	@Override
	public boolean isFolder(Object element) {
		return ((File) element).isDirectory();
	}
}
