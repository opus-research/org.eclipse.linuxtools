/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.builder;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.linuxtools.internal.rpm.rpmlint.Activator;
import org.eclipse.linuxtools.internal.rpm.rpmlint.parser.RpmlintParser;

public class RpmlintDeltaVisitor implements IResourceDeltaVisitor {

	private ArrayList<String> paths = new ArrayList<String>();

	/**
	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
	 */
	public boolean visit(IResourceDelta delta) {
		IResource resource = delta.getResource();
		if (Activator.SPECFILE_EXTENSION.equals(resource.getFileExtension())
				|| Activator.RPMFILE_EXTENSION.equals(resource
						.getFileExtension())) {
			switch (delta.getKind()) {
			// we first visiting resources to be able to run the rpmlint command
			// only once. That improve drastically the performance.
			case IResourceDelta.ADDED:
				paths.add(resource.getLocation().toOSString());
				break;
			case IResourceDelta.CHANGED:
				RpmlintParser.getInstance().deleteMarkers(resource);
				paths.add(resource.getLocation().toOSString());
				break;
			}
		}
		return true;
	}
	
	public ArrayList<String> getVisitedPaths() {
		return paths;
	}

}
