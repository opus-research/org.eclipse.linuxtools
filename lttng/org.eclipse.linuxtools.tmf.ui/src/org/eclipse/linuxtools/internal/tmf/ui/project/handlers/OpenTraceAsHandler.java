/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfNavigatorContentProvider;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Open a trace for a given trace type.
 *
 * The implementation considers multiple scenarios:
 * 1) Original trace has no trace type
 *    -> original trace will be opened and the trace type will be
 *       set accordingly
 *
 * 2) Trace type of original trace and given trace type are the same
 *    -> original trace will be opened
 *
 * 3) Trace type of original trace and given trace type are different
 *    -> original trace will be copied
 *    -> copied trace will be opened and the trace type will be set
 *       accordingly
 *
 * 4) Trace type of original trace and given trace type are different
 *    and the new trace already exists
 *    -> the previously copied trace will be opened
 */
public class OpenTraceAsHandler extends OpenTraceHandler {

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        final TmfTraceElement originTrace = fTrace.getElementUnderTraceFolder();

        String traceType = event.getParameter(TraceTypeUtil.TYPE_PARAMETER);

        final String newName = getNewTraceName(originTrace, traceType);
        final TmfProjectElement project = TmfProjectRegistry.getProject(originTrace.getProject().getResource());

        // Check if trace with new name exists
        TmfTraceElement newTraceElement = project.findTraceElement(newName);

        // Check special:
        // Origin trace doens't have trace type yet -> open origin trace
        // Origin trace type and new trace type are the same -> open origin trace
        if ((originTrace.getTraceType() == null) || (traceType.equals(originTrace.getTraceType()))) {
            newTraceElement = originTrace;
        }

        IStatus status = Status.OK_STATUS;

        // Trace to open doesn't exist yet
        if (newTraceElement == null) {
            status = copyTrace(originTrace, newName);
            final TmfNavigatorContentProvider ncp = new TmfNavigatorContentProvider();
            ncp.getChildren(originTrace.getProject().getResource()); // force the model to be populated
            newTraceElement = project.findTraceElement(newName);
        }

        // Update persistent properties if needed
        if ((newTraceElement != null) && (status.isOK())) {
            IResource resource = newTraceElement.getResource();
            if (resource != null) {
                // Set the properties for this resource
                String bundleName = event.getParameter(TraceTypeUtil.BUNDLE_PARAMETER);
                String iconUrl = event.getParameter(TraceTypeUtil.ICON_PARAMETER);
                status = TraceTypeUtil.propagateProperties(newTraceElement, bundleName, traceType, iconUrl);
            }
            project.refresh();
        } else {
            status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Copy of trace failed"); //$NON-NLS-1$
        }

        if (!status.isOK()) {
            // Delete copied resources if needed
            if (newTraceElement != null) {
                newTraceElement.deleteSupplementaryFolder();
                try {
                    newTraceElement.getResource().delete(true, new NullProgressMonitor());
                } catch (CoreException e) {
                    // ignore
                }
                project.refresh();
            }
            final Shell shell = window.getShell();
            ErrorDialog.openError(shell, Messages.SelectTraceTypeHandler_Title, Messages.SelectTraceTypeHandler_InvalidTraceType, status);
            return null;
        }

        // Trace is validated and ready to be opened.
        performOpen(newTraceElement);
        return null;
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    // copy trace without supplementary files
    private static IStatus copyTrace(final TmfTraceElement originTrace, final String newName) {
            WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
                @Override
                public void execute(IProgressMonitor monitor) throws CoreException {
                    try {
                        monitor.beginTask("", 1000); //$NON-NLS-1$
                        if (monitor.isCanceled()) {
                            throw new OperationCanceledException();
                        }
                        // copy without supplementary files
                        originTrace.copy(newName, false);
                        if (monitor.isCanceled()) {
                            throw new OperationCanceledException();
                        }
                    } finally {
                        monitor.done();
                    }
                }
            };

            try {
                PlatformUI.getWorkbench().getProgressService().busyCursorWhile(operation);
            } catch (InterruptedException exception) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Exception caught while copying trace", exception); //$NON-NLS-1$
            } catch (InvocationTargetException exception) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Exception caught while copying trace", exception); //$NON-NLS-1$
            } catch (RuntimeException exception) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Exception caught while copying trace", exception); //$NON-NLS-1$
            }
            return Status.OK_STATUS;
    }

    private static String getNewTraceName(TmfTraceElement trace, String traceType) {
        Object prob = TmfTraceType.getTraceTypeName(traceType);
        if (prob instanceof String) {
            return trace.getName() + '-' + prob.toString();
        }
        //
        IFolder folder = trace.getProject().getTracesFolder().getResource();
        int i = 2;
        while (true) {
            String name = trace.getName() + '-' + Integer.toString(i++);
            IResource resource = folder.findMember(name);
            if (resource == null) {
                return name;
            }
        }
    }
}
