/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>SelectExperimentTypeHandler</u></b>
 * <p>
 */
public class SelectExperimentTypeHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String BUNDLE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_experiment_type.bundle"; //$NON-NLS-1$
    private static final String TYPE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_experiment_type.type"; //$NON-NLS-1$
    private static final String ICON_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_experiment_type.icon"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private TreeSelection fSelection = null;

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------

    @Override
    public boolean isEnabled() {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return false;
        }
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return false;
        }
        ISelection selection = selectionProvider.getSelection();

        // Make sure selection contains only traces
        fSelection = null;
        if (selection instanceof TreeSelection) {
            fSelection = (TreeSelection) selection;
            Iterator<Object> iterator = fSelection.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (!(element instanceof TmfExperimentElement)) {
                    return false;
                }
            }
        }

        // If we get here, either nothing is selected or everything is a trace
        return !selection.isEmpty();
    }

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
        List<IStatus> statuses = new ArrayList<IStatus>();
        boolean ok = true;
        for (Object element : fSelection.toList()) {
            TmfExperimentElement experiment = (TmfExperimentElement) element;
            IResource resource = experiment.getResource();
            if (resource != null) {
                try {
                    // Set the properties for this resource
                    String bundleName = event.getParameter(BUNDLE_PARAMETER);
                    String traceType = event.getParameter(TYPE_PARAMETER);
                    String iconUrl = event.getParameter(ICON_PARAMETER);
                    String previousTraceType = experiment.getTraceType();
                    IStatus status = propagateProperties(experiment, bundleName, traceType, iconUrl);
                    ok &= status.isOK();

                    if (status.isOK()) {
                        if ((previousTraceType != null) && (!traceType.equals(previousTraceType))) {
                            // Close the trace if open
                            experiment.closeEditors();
                            // Delete all supplementary resources
                            experiment.deleteSupplementaryResources();
                        }
                    } else {
                        statuses.add(status);
                    }
                } catch (CoreException e) {
                    Activator.getDefault().logError(Messages.SelectTraceTypeHandler_ErrorSelectingTrace + experiment.getName(), e);
                }
            }
        }
        ((ITmfProjectModelElement) fSelection.getFirstElement()).getProject().refresh();

        if (!ok) {
            final Shell shell = window.getShell();
            MultiStatus info = new MultiStatus(Activator.PLUGIN_ID, 1, Messages.SelectTraceTypeHandler_TraceFailedValidation, null);
            if (statuses.size() > 1)
            {
                info = new MultiStatus(Activator.PLUGIN_ID, 1, Messages.SelectTraceTypeHandler_TracesFailedValidation, null);
            }
            for (IStatus status : statuses) {
                info.add(status);
            }
            ErrorDialog.openError(shell, Messages.SelectTraceTypeHandler_Title, Messages.SelectTraceTypeHandler_InvalidTraceType, info);
        }
        return null;
    }

    private static IStatus propagateProperties(TmfExperimentElement trace,
            String bundleName, String traceType, String iconUrl)
            throws CoreException {

        IResource svResource = trace.getResource();
        String svBundleName = svResource.getPersistentProperty(TmfCommonConstants.TRACEBUNDLE);
        String svTraceType = svResource.getPersistentProperty(TmfCommonConstants.TRACETYPE);
        String svIconUrl = svResource.getPersistentProperty(TmfCommonConstants.TRACEICON);

        setProperties(trace.getResource(), bundleName, traceType, iconUrl);
        trace.refreshTraceType();
        final IStatus validateTraceType = validateTraceType(trace);
        if (!validateTraceType.isOK()) {
            setProperties(trace.getResource(), svBundleName, svTraceType, svIconUrl);
            trace.refreshTraceType();
            return validateTraceType;
        }

        trace.refreshTraceType();

        return Status.OK_STATUS;
    }

    private static void setProperties(IResource resource, String bundleName,
            String traceType, String iconUrl) throws CoreException {
        resource.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE, bundleName);
        resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, traceType);
        resource.setPersistentProperty(TmfCommonConstants.TRACEICON, iconUrl);
    }

    private static IStatus validateTraceType(TmfExperimentElement experiment) {
        IProject project = experiment.getProject().getResource();
        TmfExperiment tmfExperiment = null;
        IStatus validate = null;
        try {
            tmfExperiment = experiment.instantiateTrace();
            if (tmfExperiment != null) {
                validate = tmfExperiment.validate(project, experiment.getLocation().getPath());
            }
            else{
                validate =  new Status(IStatus.ERROR, experiment.getName(), "File does not exist : " + experiment.getLocation().getPath()); //$NON-NLS-1$
            }
        } finally {
            if (tmfExperiment != null) {
                tmfExperiment.dispose();
            }
        }
        if (validate == null) {
            validate = new Status(IStatus.ERROR, "unknown", "unknown"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return validate;
    }

}
