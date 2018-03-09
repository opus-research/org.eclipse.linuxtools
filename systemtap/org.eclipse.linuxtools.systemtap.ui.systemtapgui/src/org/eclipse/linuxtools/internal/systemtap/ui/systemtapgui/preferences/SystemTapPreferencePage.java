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

package org.eclipse.linuxtools.internal.systemtap.ui.systemtapgui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.linuxtools.internal.systemtap.ui.systemtapgui.Localization;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



public class SystemTapPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Get the current preferences.
	 */
	public SystemTapPreferencePage() {
		super(FLAT);
		setDescription(Localization.getString("SystemTapPreferencePage.GUIPreferencesDescription"));
	}

	/**
	 * Sets up the field editors for optional change by the user.
	 */
	public void createFieldEditors() {
	}

	public void init(IWorkbench workbench) {
	}

}
