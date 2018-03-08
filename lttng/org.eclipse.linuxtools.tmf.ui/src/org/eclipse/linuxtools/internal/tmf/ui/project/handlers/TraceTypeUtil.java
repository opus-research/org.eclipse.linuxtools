/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann Initial API and implementation (extracted from SelectTraceTypeHandler)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;

/**
 * Utility for trace type handling.
 */
public class TraceTypeUtil {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /** Bundle name command parameter */
    public static final String BUNDLE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_trace_type.bundle"; //$NON-NLS-1$
    /** Trace type command parameter*/
    public static final String TYPE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_trace_type.type"; //$NON-NLS-1$
    /** Icon trace type parameter*/
    public static final String ICON_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_trace_type.icon"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Propagates the trace type persistent properties across all relevant trace elements. It also validates the
     * trace according to the trace type/
     *
     * @param trace
     *          - The source trace model element
     * @param bundleName
     *          - The bundle name
     * @param traceType
     *          - The trace type to set
     * @param iconUrl
     *          - The Icon URL for the trace
     * @return
     *          - Status class with result information
     */
    public static IStatus propagateProperties(TmfTraceElement trace,
            String bundleName, String traceType, String iconUrl) {

        try {
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

            if (trace.getParent() instanceof TmfTraceFolder) {
                TmfExperimentFolder experimentFolder = trace.getProject().getExperimentsFolder();
                for (final ITmfProjectModelElement experiment : experimentFolder.getChildren()) {
                    for (final ITmfProjectModelElement child : experiment.getChildren()) {
                        if (child instanceof TmfTraceElement) {
                            TmfTraceElement linkedTrace = (TmfTraceElement) child;
                            if (linkedTrace.getName().equals(trace.getName())) {
                                IResource resource = linkedTrace.getResource();
                                setProperties(resource, bundleName, traceType, iconUrl);
                                linkedTrace.refreshTraceType();
                            }
                        }
                    }
                }
            }
        } catch (CoreException e){
            Activator.getDefault().logError(Messages.SelectTraceTypeHandler_ErrorSelectingTrace + trace.getName(), e);
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Exception caught", e); //$NON-NLS-1$
        }

        return Status.OK_STATUS;
    }

    /**
     * Set the persistent properties to the trace resource.
     * @param resource
     *          - The trace resource
     * @param bundleName
     *          - The bundle name property to set
     * @param traceType
     *          - The trace type property to set
     * @param iconUrl
     *          - The icon URL property to set.
     * @throws CoreException
     *          - if setting of persistent property fails
     */
    public static void setProperties(IResource resource, String bundleName,
            String traceType, String iconUrl) throws CoreException {
        resource.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE, bundleName);
        resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, traceType);
        resource.setPersistentProperty(TmfCommonConstants.TRACEICON, iconUrl);
    }

    /**
     * Validates the trace type for given trace element.
     * @param trace
     *          - The trace element to validate
     * @return status information
     */
    public static IStatus validateTraceType(TmfTraceElement trace) {
        IProject project = trace.getProject().getResource();
        ITmfTrace tmfTrace = null;
        IStatus validate = null;
        try {
            tmfTrace = trace.instantiateTrace();
            if (tmfTrace != null) {
                validate = tmfTrace.validate(project, trace.getLocation().getPath());
            }
            else{
                validate =  new Status(IStatus.ERROR, trace.getName(), "File does not exist : " + trace.getLocation().getPath()); //$NON-NLS-1$
            }
        } finally {
            if (tmfTrace != null) {
                tmfTrace.dispose();
            }
        }
        if (validate == null) {
            validate = new Status(IStatus.ERROR, "unknown", "unknown"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return validate;
    }
}
