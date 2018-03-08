/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Improved trace selection
 *******************************************************************************/

package org.eclipse.linuxtools.internal.gdbtrace.ui.views.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.gdbtrace.trace.GdbTrace;
import org.eclipse.linuxtools.internal.gdbtrace.ui.views.project.dialogs.SelectTraceExecutableDialog;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for the Select Trace Executable command.
 * @author Patrick Tasse
 */
public class SelectTraceExecutableHandler extends AbstractHandler {

    /** The trace element reference. */
    private TmfTraceElement fTraceElement = null;

    @Override
    public boolean isEnabled() {

        // Check if we are closing down
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        // Get the selection
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return false;
        }
        final ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return false;
        }
        final ISelection selection = selectionProvider.getSelection();

        // Make sure there is only one selection and that it is a trace
        fTraceElement = null;
        if (selection instanceof TreeSelection) {
            final TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            final Object element = sel.getFirstElement();
            if (element instanceof TmfTraceElement) {
                fTraceElement = (TmfTraceElement) element;
            }
        }

        // We only enable opening from the Traces folder for now
        return (fTraceElement != null);
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        SelectTraceExecutableDialog dialog = new SelectTraceExecutableDialog(shell);
        dialog.open();
        if (dialog.getReturnCode() != Window.OK) {
            return null;
        }
        IPath tracedExecutable = dialog.getExecutablePath();

        IResource resource = fTraceElement.getResource();
        try {
            resource.setPersistentProperty(GdbTrace.EXEC_KEY, tracedExecutable.toString());
        } catch (CoreException e) {
            final MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
            mb.setText(e.getClass().getName());
            mb.setMessage(e.getMessage());
            mb.open();
        }
        return null;
    }
}
