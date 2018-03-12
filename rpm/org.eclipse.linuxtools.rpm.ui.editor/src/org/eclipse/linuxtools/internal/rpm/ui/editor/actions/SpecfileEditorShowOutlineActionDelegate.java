/*******************************************************************************
 * Copyright (c) 2008, 2017 Alexander Kurtakov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.linuxtools.internal.rpm.ui.editor.outline.SpecfileQuickOutlineDialog;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

public class SpecfileEditorShowOutlineActionDelegate extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		ITextEditor textEditor = editor.getAdapter(ITextEditor.class);
		SpecfileQuickOutlineDialog quickOutlinePopupDialog = new SpecfileQuickOutlineDialog(editor.getSite().getShell(),
				SWT.NONE, textEditor);
		quickOutlinePopupDialog.setVisible(true);
		return null;
	}

}
