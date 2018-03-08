/*******************************************************************************
 * Copyright (c) 2009, 2010, 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEditorInput;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

/**
 * <b><u>OpenTraceHandler</u></b>
 * <p>
 * TODO: Add support for multiple trace selection
 */
public class OpenTraceHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private TmfTraceElement fTrace = null;

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------

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
        final ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return false;
        }
        final ISelection selection = selectionProvider.getSelection();

        // Make sure there is only one selection and that it is a trace
        fTrace = null;
        if (selection instanceof TreeSelection) {
            final TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            final Object element = sel.getFirstElement();
            if (element instanceof TmfTraceElement) {
                fTrace = (TmfTraceElement) element;
            }
        }

        // We only enable opening from the Traces folder for now
        return (fTrace != null);
    }

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {

        // Check if we are closing down
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        // Check that the trace is valid
        if (fTrace == null) {
            return null;
        }

        // If trace is under an experiment, use the original trace from the traces folder
        final TmfTraceElement traceElement = fTrace.getElementUnderTraceFolder();

        Thread thread = new Thread() {
            @Override
            public void run() {

                final ITmfTrace trace = traceElement.instantiateTrace();
                final ITmfEvent traceEvent = traceElement.instantiateEvent();
                if ((trace == null) || (traceEvent == null)) {
                    displayErrorMsg(Messages.OpenTraceHandler_NoTraceType);
                    if (trace != null) {
                        trace.dispose();
                    }
                    return;
                }

                // Get the editor_id from the extension point
                String traceEditorId = traceElement.getEditorId();
                final String editorId = (traceEditorId != null) ? traceEditorId : TmfEventsEditor.ID;

                try {
                    trace.initTrace(traceElement.getResource(), traceElement.getLocation().getPath(), traceEvent.getClass());
                } catch (final TmfTraceException e) {
                    displayErrorMsg(Messages.OpenTraceHandler_InitError + "\n\n" + e); //$NON-NLS-1$
                    trace.dispose();
                    return;
                }

                final IFile file;
                try {
                    file = traceElement.getBookmarksFile();
                } catch (final CoreException e) {
                    Activator.getDefault().logError("Error opening trace " + traceElement.getName(), e); //$NON-NLS-1$
                    displayErrorMsg(Messages.OpenTraceHandler_Error + "\n\n" + e.getMessage()); //$NON-NLS-1$
                    trace.dispose();
                    return;
                }

                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final IEditorInput editorInput = new TmfEditorInput(file, trace);
                            final IWorkbench wb = PlatformUI.getWorkbench();
                            final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();

                            final IEditorPart editor = activePage.findEditor(new FileEditorInput(file));
                            if ((editor != null) && (editor instanceof IReusableEditor)) {
                                activePage.reuseEditor((IReusableEditor) editor, editorInput);
                                activePage.activate(editor);
                            } else {
                                activePage.openEditor(editorInput, editorId);
                                IDE.setDefaultEditor(file, editorId);
                                // editor should dispose the trace on close
                            }
                        } catch (final PartInitException e) {
                            displayErrorMsg(Messages.OpenTraceHandler_Error + "\n\n" + e.getMessage()); //$NON-NLS-1$
                            Activator.getDefault().logError("Error opening trace " + traceElement.getName(), e); //$NON-NLS-1$
                            trace.dispose();
                        }
                    }
                });

            }
        };

        thread.start();
        return null;
    }

    private static void displayErrorMsg(final String errorMsg) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                final MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
                mb.setText(Messages.OpenTraceHandler_Title);
                mb.setMessage(errorMsg);
                mb.open();
            }
        });
    }

}
