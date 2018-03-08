/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis (mathieu.denis@polymtl.ca) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfStatisticsViewer;

/**
 * Factory that creates a base class of TmfViewer from the definition of a trace type viewer element in the plug-in.xml
 *
 * @author Mathieu Denis
 */
public class TmfViewerFactory {
    /**
     * Retrieves and instantiates a viewer based on his plug-in definition for a specific trace type.
     *
     * The viewer is instantiate using its 0-argument constructor.
     *
     * @param resource The resource where to find the information about the trace properties
     * @param element The name of the element to find under the trace type definition
     * @return a new TmfViewer based on his definition in plugin.xml, or null if no definition was found
     */
    protected static Object getTraceTypeElement(IResource resource, String element) {
        try {
            if (resource != null) {
                String traceType = resource.getPersistentProperty(TmfCommonConstants.TRACETYPE);
                // Search in the configuration if there is any viewer specified for this kind of trace type.
                for (IConfigurationElement ce : TmfTraceType.getTypeElements()) {
                    if (ce.getAttribute(TmfTraceType.ID_ATTR).equals(traceType)) {
                        IConfigurationElement[] viewerCE = ce.getChildren(element);
                        if (viewerCE.length != 1) {
                            break;
                        }
                        return viewerCE[0].createExecutableExtension(element);
                    }
                }
            }
        } catch (CoreException e) {
            Activator.getDefault().logError("Error creating statistics viewer", e); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Retrieves and instantiates a viewer based on his plug-in definition for a specific trace type. It is specific to
     * the statistics viewer.
     *
     * It only calls the 0-parameter constructor without performing any other initialization on the viewer.
     *
     * @param resource The resource where to find the information about the trace properties
     * @return a new statistics viewer based on his plug-in definition, or null if no statistics definition was found
     *         for the trace type.
     */
    public static ITmfStatisticsViewer getStatisticsViewer(IResource resource) {
        return (ITmfStatisticsViewer) TmfViewerFactory.getTraceTypeElement(resource, TmfTraceType.STATISTICS_VIEWER_ELEM);
    }
}
