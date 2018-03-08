/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Medeiros Teixeira <rafaelmt@linux.vnet.ibm.com> - initial API and implementation
*******************************************************************************/

package org.eclipse.linuxtools.internal.valgrind.ui.quickfixes;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindError;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindStackFrame;
import org.eclipse.linuxtools.internal.valgrind.ui.Messages;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Quick-fix for the Wrong deallocation function issue reported by memcheck.
 * Switches the deallocation function ("free" or "delete") accordingly.
 *
 * @author rafaelmt
 */
public class WrongDeallocationResolution extends AbstractValgrindMarkerResolution {
	private static final String DELETE = "delete"; //$NON-NLS-1$
	private static final String FREE = "free"; //$NON-NLS-1$

	public WrongDeallocationResolution(IMarker marker) {
		super(marker);
	}

	@Override
	public void apply(IMarker marker, IDocument document) {
		try {
			IASTNode astNode = getIASTNode(marker);
			if(astNode != null) {
				int nodeLength = astNode.getFileLocation().getNodeLength();
				int nodeOffset = astNode.getFileLocation().getNodeOffset();

				String content = document.get(nodeOffset, nodeLength);
				if(content.contains(DELETE)){
					content = content.replace(DELETE, FREE);
				} else if(content.contains(FREE)){
					content = content.replace(FREE, DELETE);
				}
				document.replace(nodeOffset, nodeLength, content);

				IValgrindMessage message = getMessage(marker);
				removeMessage(message.getParent());
				ValgrindStackFrame nestedStackFrame = getNestedStackFrame(message.getParent());
				int nestedLine = nestedStackFrame.getLine();
				String nestedFile = nestedStackFrame.getFile();
				removeMarker(nestedFile, nestedLine, marker.getType());
				marker.delete();
			}
		} catch (Exception e ){
			Status status = new Status(IStatus.ERROR, ValgrindUIPlugin.PLUGIN_ID, Messages.getString("ValgrindMemcheckQuickFixes.Error_applying_quickfix"), e); //$NON-NLS-1$
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			String title = Messages.getString("ValgrindLaunchConfigurationDelegate.Valgrind_error_title"); //$NON-NLS-1$
			String message = Messages.getString("ValgrindMemcheckQuickFixes.Error_applying_quickfix"); //$NON-NLS-1$
			ErrorDialog.openError(shell, title, message, status);
			e.printStackTrace();
		}
	}

	@Override
	public String getLabel() {
		return Messages.getString("ValgrindMemcheckQuickFixes.Wrong_dealloc_label"); //$NON-NLS-1$
	}

	/**
	 * Returns all of the messages from the currently active Valgrind view that
	 * contains a given String in their description.
	 * @param text the String to match the Valgrind messages' descriptions
	 * @return
	 */
	private IValgrindMessage[] getMessagesByText(String text){
		ValgrindViewPart valgrindView = ValgrindUIPlugin.getDefault().getView();
		ArrayList<IValgrindMessage> foundMessages = new ArrayList<IValgrindMessage>();

		IValgrindMessage[] messages = valgrindView.getMessages();
		for (IValgrindMessage message : messages) {
			if(message.getText().contains(text)){
				foundMessages.add(message);
			}
		}
		IValgrindMessage[] foundMessagesArray = new IValgrindMessage[foundMessages.size()];
		foundMessages.toArray(foundMessagesArray);
		return foundMessagesArray;
	}

	/**
	 * Return the last nested element from a given ValgrindMessage, or null if there are
	 * no nested messages.
	 * @param message
	 * @return
	 */
	private ValgrindStackFrame getStackBottom(IValgrindMessage message){
		ValgrindStackFrame stackBottom = null;
		IValgrindMessage[] children = message.getChildren();
		for (IValgrindMessage child : children) {
			if(child instanceof ValgrindStackFrame){
				stackBottom = (ValgrindStackFrame) child;
			}
		}
		return stackBottom;
	}

	/**
	 * Returns the ValgrindMessage element from the Valgrind View that represents
	 * a given Marker
	 * @param marker the marker to which the ValgrindMessage relates
	 * @return
	 */
	private IValgrindMessage getMessage(IMarker marker){
		IValgrindMessage message = null;
		String file = marker.getResource().getName();
		int line = marker.getAttribute(IMarker.LINE_NUMBER, 0);
		IValgrindMessage[] wrongDeallocMessages = getMessagesByText(Messages.getString("ValgrindMemcheckQuickFixes.Wrong_dealloc_message")); //$NON-NLS-1$
		for (IValgrindMessage wrongDeallocMessage : wrongDeallocMessages) {
			ValgrindStackFrame stackBottom = getStackBottom(wrongDeallocMessage);
			int stackBottomLine = stackBottom.getLine();
			String stackBottomFile = stackBottom.getFile();
			if(stackBottomLine == line && file != null && file.equals(stackBottomFile)){
				message = stackBottom;
			}
		}
		return message;
	}

	/**
	 * Returns the nested stack frame from a given ValgrindMessage in the Valgrind View
	 * @param message
	 * @return
	 */
	private ValgrindStackFrame getNestedStackFrame(IValgrindMessage message){
		ValgrindError nestedError = null;
		IValgrindMessage[] children = message.getChildren();
		for (IValgrindMessage child : children) {
			if(child instanceof ValgrindError){
				nestedError = (ValgrindError)child;
			}
		}
		if(nestedError != null){
			return getStackBottom(nestedError);
		} else{
			return null;
		}
	}

	/**
	 * Removes marker from file
	 *
	 * @param file
	 * @param line
	 * @param markerType
	 * @throws CoreException
	 */
	private void removeMarker(String file, int line, String markerType) throws CoreException {
		IMarker[] markers = ResourcesPlugin.getWorkspace().getRoot().findMarkers(markerType, false, IResource.DEPTH_INFINITE);
		for (IMarker marker : markers) {
			if(marker.getAttribute(IMarker.LINE_NUMBER, 0) == line && marker.getResource().getName().equals(file)){
				marker.delete();
			}
		}
	}

	/**
	 * Removes message from Valgrind view.
	 * @param message
	 */
	private void removeMessage(IValgrindMessage message){
		ValgrindViewPart valgrindView = ValgrindUIPlugin.getDefault().getView();
		valgrindView.getMessagesViewer().getTreeViewer().remove(message);
	}
}
