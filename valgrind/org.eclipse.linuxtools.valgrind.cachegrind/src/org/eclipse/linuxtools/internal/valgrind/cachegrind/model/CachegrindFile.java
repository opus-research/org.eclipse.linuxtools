/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.cachegrind.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class CachegrindFile implements ICachegrindElement {
	private static final String UNKNOWN_FILE = "???"; //$NON-NLS-1$


	protected CachegrindOutput parent;
	protected String path;
	protected List<CachegrindFunction> functions;

	protected IAdaptable model;

	public CachegrindFile(CachegrindOutput parent, String path) {
		this.parent = parent;
		this.path = path;
		functions = new ArrayList<CachegrindFunction>();

		IPath pathObj = Path.fromOSString(path);
		if (path.equals(UNKNOWN_FILE)) {
			model = null;
		}
		else {
			model = CoreModel.getDefault().create(pathObj);
			if (model == null) {
				model = ResourcesPlugin.getWorkspace().getRoot().getFile(pathObj);
			}
		}
	}

	public void addFunction(CachegrindFunction func) {
		functions.add(func);
	}

	public CachegrindFunction[] getFunctions() {
		return functions.toArray(new CachegrindFunction[functions.size()]);
	}

	public ICachegrindElement[] getChildren() {
		return getFunctions();
	}

	public IAdaptable getModel() {
		return model;
	}

	public String getPath() {
		return path;
	}
	
	public String getName() {
		String name = path;
		if (Path.ROOT.isValidPath(path)) {
			name = Path.fromOSString(path).lastSegment();
		}
		return name;
	}
	
	public ICachegrindElement getParent() {
		return parent;
	}
	
	public int compareTo(ICachegrindElement o) {
		int result = 0;
		if (o instanceof CachegrindFile) {
			result = getName().compareTo(((CachegrindFile) o).getName()); 
		}
		return result;
	}

}
