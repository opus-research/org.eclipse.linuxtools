/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf;

import org.eclipse.core.runtime.IPath;

/**
 * This class handles the execution of the source disassembly command
 * and stores the resulting data.
 */
public class SourceDisassemblyData extends AbstractDataManipulator {

	private IPath workingDir;

	public SourceDisassemblyData(String title, IPath workingDir) {
		super(title);
		this.workingDir = workingDir;
	}

	@Override
	public void parse() {
		String [] cmd = getCommand(workingDir.toOSString());
		performCommand(cmd);
	}

	public String [] getCommand(String workingDir) {
		return new String[] { "perf", "annotate", //$NON-NLS-1$ //$NON-NLS-2$
				"-i", workingDir + "perf.data" }; //$NON-NLS-1$ //$NON-NLS-2$
	}

}