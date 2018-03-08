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

package org.eclipse.linuxtools.systemtap.ui.ide.actions;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.STPEditor;
import org.eclipse.linuxtools.systemtap.ui.editor.PathEditorInput;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.structures.TreeDefinitionNode;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;



/**
 * This <code>Action</code> is used when the user right clicks on an item in the Tapset Browsers.
 * Right-clicking on items in the browsers causes a menu item called "Go to definition" to appear,
 * which fires this action upon selection. The result is that the file containing the definition
 * for the entity that the user selected is opened in an <code>STPEditor</code> in the current window.
 * @author Ryan Morse
 * @see org.eclipse.jface.action.Action
 * @see org.eclipse.linuxtools.internal.systemtap.ui.ide.views.TapsetBrowserView
 */
public class DefinitionAction extends Action implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {
	private IStructuredSelection selection = null;

	public DefinitionAction() {
		super();
		LogManager.logInfo("initialized", this); //$NON-NLS-1$
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		//LogManager.logDebug("Start/End setActivePart: action-" + action + ", targetPart-" + targetPart, this);
	}

	/**
	 * The main body of the event. This code gets the filename from the selected entry in the viewer,
	 * then opens a new <code>STPEditor</code> for that file. 
	 */
	public void run(IAction action) {
		LogManager.logDebug("Start run: action-" + action, this); //$NON-NLS-1$
		if(!isEnabled())
			return;
		Object o = selection.getFirstElement();
		if(!(o instanceof TreeDefinitionNode))
			return;
		TreeDefinitionNode t = (TreeDefinitionNode)o;
		String filename = t.getDefinition();
		Path p = new Path(filename);
		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		PathEditorInput input = new PathEditorInput(p, window);
		try {
			IEditorPart editorPart = window.getActivePage().openEditor(input, STPEditor.ID);
			STPEditor editor = (STPEditor)editorPart;
			int line;
			
			if(t.getData().toString().startsWith("probe"))
				line = probeFind(t, editor);
			else
				line = functionFind(t, editor);

			editor.jumpToLocation(++line, 0);
		} catch (PartInitException e) {
			LogManager.logCritical("Exception run: " + e.getMessage(), this); //$NON-NLS-1$
			e.printStackTrace();
		}
		LogManager.logDebug("End run:", this); //$NON-NLS-1$
	}
	
	/**
	 * Tries to find the line of code that corrisponds to the provided
	 * function node within the file open in the provided editor.
	 * @param t The tree node that we want to look up
	 * @param editor The STPEditor with the file we are searching in
	 * @return int representing the line where the node is defined
	 */
	private int functionFind(TreeDefinitionNode t, STPEditor editor) {
		String func = t.toString();
		func = func.substring(0, func.indexOf('('));
		
		int line = editor.find("function " + func);

		if(line < 0)
			line = editor.find(func);
		return Math.max(line, 0);
	}
	
	/**
	 * Tries to find the line of code that corrisponds to the provided
	 * probe node within the file open in the provided editor.
	 * @param t The tree node that we want to look up
	 * @param editor The STPEditor with the file we are searching in
	 * @return int representing the line where the node is defined
	 */
	private int probeFind(TreeDefinitionNode t, STPEditor editor) {
		int line = editor.find("probe " + t.toString());

		if(line < 0)
			line = editor.find(t.getData().toString());
		if(line < 0)
			line = editor.find(t.getData().toString().replace(" ", ""));
		return Math.max(line, 0);
	}
	
	/**
	 * Updates <code>selection</code> with the current selection whenever the user changes
	 * the current selection.
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof IStructuredSelection) {
			LogManager.logDebug("this.selection changed: oldValue: "+ this.selection + " newValue: " + selection, this); //$NON-NLS-1$ //$NON-NLS-2$
			this.selection = (IStructuredSelection)selection;
			Object o = this.selection.getFirstElement();
			if(o instanceof TreeDefinitionNode)
			{
				TreeDefinitionNode t = (TreeDefinitionNode)o;
				String s = t.getDefinition();
				if(s != null)
					setEnabled(true);
				else
					setEnabled(false);
			}
			else
				setEnabled(false);
		} else {
			this.setEnabled(false);
		}
	}

	public void dispose() {
		LogManager.logInfo("disposing", this); //$NON-NLS-1$
	}

	public void init(IWorkbenchWindow window) {}
}
