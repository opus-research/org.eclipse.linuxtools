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

package org.eclipse.linuxtools.systemtap.ui.ide;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * A simple class that contains information about the current session of the IDE, such as
 * the path to the tapset libraries, the active SystemTap Script Editor, and if the user
 * chooses, the user's account password.
 * @author Ryan Morse
 */
public class IDESessionSettings {
	public static String tapsetLocation = "";
	public static String password = null;
	
	private static STPEditor activeSTPEditor = null;

	public static STPEditor getActiveSTPEditor() {
		if (activeSTPEditor == null){
			// If the active editor is not set and there is only one
			// stap script editor open set that to be the active editor.
			IEditorReference[] editors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
			int count = 0;
			for(IEditorReference editor: editors){
				if (editor.getId().equals(STPEditor.ID)){
					activeSTPEditor = (STPEditor) editor.getEditor(true);
					count++;
				}
			}
			if (count > 1){
				activeSTPEditor = null;
			}
		}
		return activeSTPEditor;
	}

	public static void setActiveSTPEditor (STPEditor editor){
		activeSTPEditor = editor;
	}
}
