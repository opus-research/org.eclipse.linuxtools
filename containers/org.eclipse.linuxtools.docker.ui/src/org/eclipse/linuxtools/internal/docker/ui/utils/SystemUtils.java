/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.utils;

/**
 * Utility class for System/OS info
 */
public class SystemUtils {

	private static final String OS_NAME = System.getProperty("os.name") //$NON-NLS-1$
			.toLowerCase();

	/**
	 * @return <code>true</code> if if the current OS is Windows,
	 *         <code>false</code> otherwise.
	 */
	public static boolean isWindows() {
		return OS_NAME.indexOf("win") != -1; //$NON-NLS-1$
	}

	/**
	 * @return <code>true</code> if if the current OS is Mac, <code>false</code>
	 *         otherwise.
	 */
	public static boolean isMac() {
		return OS_NAME.indexOf("mac") != -1; //$NON-NLS-1$
	}

	/**
	 * @return <code>true</code> if if the current OS is Linux,
	 *         <code>false</code> otherwise.
	 */
	public static boolean isLinux() {
		return OS_NAME.indexOf("nix") != -1 || OS_NAME.indexOf("nux") != -1; //$NON-NLS-1$
	}

}
