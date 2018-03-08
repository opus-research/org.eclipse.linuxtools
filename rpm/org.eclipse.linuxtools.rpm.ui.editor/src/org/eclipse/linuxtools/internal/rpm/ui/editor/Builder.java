/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * Build the RPM project based on the BuildType specified.
 *
 * @since 1.0.0
 */
public class Builder {

	private BuildType type;

	/**
	 * Creates the generator for the given input type.
	 *
	 * @param type
	 *            The input type for this generator.
	 */
	public Builder(BuildType type) {
		this.type = type;
	}

	public String getExecutionMessage() {
		return type.getExecutionMessage();
	}

	/**
	 * Get the console.
	 *
	 * @param rpj
	 *            The RPM Project to build.
	 * @param out
	 *            The console outputstream to display the build process.
	 * @return The status of the build.
	 */
	public IStatus build(RPMProject rpj, IOConsoleOutputStream out) throws CoreException {
		IStatus result = null;
		switch (type) {
		case ALL:
			result = rpj.buildAll(out);
			break;
		case BINARY:
			result = rpj.buildBinaryRPM(out);
			break;
		case SOURCE:
			result = rpj.buildSourceRPM(out);
			break;
		}
		return result;
	}

}
