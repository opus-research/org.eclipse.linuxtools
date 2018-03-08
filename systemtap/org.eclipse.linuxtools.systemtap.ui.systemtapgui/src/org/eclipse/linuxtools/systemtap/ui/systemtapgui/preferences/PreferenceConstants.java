/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.systemtapgui.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	//environmentvariables
	public static final String[][] P_ENV = new String[][] {
		{"EnvLdLibraryPath", "LD_LIBRARY_PATH"},
		{"EnvPath", "PATH"},
		{"EnvSystemtapTapset", "SYSTEMTAP_TAPSET"},
		{"EnvSystemtapRuntime", "SYSTEMTAP_RUNTIME"},
	};

	//systemtap
	public static final String P_WINDOW_STATE = "RestoreWindowStatePreference";
}
