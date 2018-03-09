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

package org.eclipse.linuxtools.tmf.core.analysis;

import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.internal.tmf.core.analysis.TmfAnalysisModuleSourceConfigElement;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

/**
 * Analysis module helper for modules provided by a plugin's configuration
 * elements.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfAnalysisModuleHelperConfigElement implements IAnalysisModuleHelper {

    private final IConfigurationElement fCe;

    /**
     * Constructor
     *
     * @param ce
     *            The source {@link IConfigurationElement} of this module helper
     */
    public TmfAnalysisModuleHelperConfigElement(IConfigurationElement ce) {
        fCe = ce;
    }

    // ----------------------------------------
    // Wrappers to {@link IAnalysisModule} methods
    // ----------------------------------------

    @Override
    public String getId() {
        return fCe.getAttribute(TmfAnalysisModuleSourceConfigElement.ID_ATTR);
    }

    @Override
    public String getName() {
        return fCe.getAttribute(TmfAnalysisModuleSourceConfigElement.NAME_ATTR);
    }

    @Override
    public boolean isAutomatic() {
        return Boolean.valueOf(fCe.getAttribute(TmfAnalysisModuleSourceConfigElement.AUTOMATIC_ATTR));
    }

    @Override
    public String getHelpText() {
        /*
         * FIXME: No need to externalize this. A better solution will be found
         * soon and this string is just temporary
         */
        return new String("The trace must be opened to get the help message"); //$NON-NLS-1$
    }

    @Override
    public String getIcon() {
        return fCe.getAttribute(TmfAnalysisModuleSourceConfigElement.ICON_ATTR);
    }

    @Override
    public Bundle getBundle() {
        return ContributorFactoryOSGi.resolve(fCe.getContributor());
    }

    @Override
    public boolean appliesToTraceType(Class<? extends ITmfTrace> traceclass) {
        boolean applies = false;

        /* Get the module's applying tracetypes */
        final IConfigurationElement[] tracetypeCE = fCe.getChildren(TmfAnalysisModuleSourceConfigElement.TRACETYPE_ELEM);
        for (IConfigurationElement element : tracetypeCE) {
            Class<?> applyclass;
            try {
                applyclass = getBundle().loadClass(element.getAttribute(TmfAnalysisModuleSourceConfigElement.CLASS_ATTR));
                String classAppliesVal = element.getAttribute(TmfAnalysisModuleSourceConfigElement.APPLIES_ATTR);
                boolean classApplies = true;
                if (classAppliesVal != null) {
                    classApplies = Boolean.valueOf(classAppliesVal);
                }
                if (classApplies) {
                    applies = applyclass.isAssignableFrom(traceclass);
                } else {
                    applies = !applyclass.isAssignableFrom(traceclass);
                }
            } catch (ClassNotFoundException e) {
                Activator.logError("Error in applies to trace", e); //$NON-NLS-1$
            } catch (InvalidRegistryObjectException e) {
                Activator.logError("Error in applies to trace", e); //$NON-NLS-1$
            }
        }
        return applies;
    }

    // ---------------------------------------
    // Functionalities
    // ---------------------------------------

    @Override
    public IAnalysisModule newModule(ITmfTrace trace) throws TmfAnalysisException {

        /* Check that analysis can be executed */
        if (!appliesToTraceType(trace.getClass())) {
            throw new TmfAnalysisException(NLS.bind(Messages.TmfAnalysisModuleHelper_AnalysisDoesNotApply, getName()));
        }

        IAnalysisModule module = null;
        try {
            module = (IAnalysisModule) fCe.createExecutableExtension(TmfAnalysisModuleSourceConfigElement.ANALYSIS_MODULE_ATTR);
            module.setName(getName());
            module.setId(getId());
            module.setAutomatic(isAutomatic());

            /* Get the module's parameters */
            final IConfigurationElement[] parametersCE = fCe.getChildren(TmfAnalysisModuleSourceConfigElement.PARAMETER_ELEM);
            for (IConfigurationElement element : parametersCE) {
                module.addParameter(element.getAttribute(TmfAnalysisModuleSourceConfigElement.NAME_ATTR));
                String defaultValue = element.getAttribute(TmfAnalysisModuleSourceConfigElement.DEFAULT_VALUE_ATTR);
                if (defaultValue != null) {
                    module.setParameter(element.getAttribute(TmfAnalysisModuleSourceConfigElement.NAME_ATTR), defaultValue);
                }
            }
            module.setTrace(trace);
        } catch (CoreException e) {
            Activator.logError("Error getting analysis modules from configuration files", e); //$NON-NLS-1$
        }
        return module;

    }

}
