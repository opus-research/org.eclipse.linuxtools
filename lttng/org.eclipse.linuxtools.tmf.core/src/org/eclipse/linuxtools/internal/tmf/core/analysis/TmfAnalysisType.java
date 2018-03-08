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

package org.eclipse.linuxtools.internal.tmf.core.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;

/**
 * Utility class for accessing TMF analysis type extensions from the platform's
 * extensions registry.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public final class TmfAnalysisType {

    /**
     * Extension point ID
     */
    public static final String TMF_ANALYSIS_TYPE_ID = "org.eclipse.linuxtools.tmf.core.analysis"; //$NON-NLS-1$

    /**
     * Extension point element 'module'
     */
    public static final String MODULE_ELEM = "module"; //$NON-NLS-1$
    /**
     * Extension point element 'parameter'
     */
    public static final String PARAMETER_ELEM = "parameter"; //$NON-NLS-1$

    /**
     * Extension point attribute 'ID'
     */
    public static final String ID_ATTR = "id"; //$NON-NLS-1$
    /**
     * Extension point attribute 'name'
     */
    public static final String NAME_ATTR = "name"; //$NON-NLS-1$
    /**
     * Extension point attribute 'analysis_module'
     */
    public static final String ANALYSIS_MODULE_ATTR = "analysis_module"; //$NON-NLS-1$
    /**
     * Extension point attribute 'automatic'
     */
    public static final String AUTOMATIC_ATTR = "automatic"; //$NON-NLS-1$
    /**
     * Extension point attribute 'icon'
     */
    public static final String ICON_ATTR = "icon"; //$NON-NLS-1$
    /**
     * Extension point attribute 'default_value'
     */
    public static final String DEFAULT_VALUE_ATTR = "default_value"; //$NON-NLS-1$

    /**
     * The mapping of available trace type IDs to their corresponding
     * configuration element
     */
    private final Map<String, IConfigurationElement> fAnalysisTypeAttributes = new HashMap<String, IConfigurationElement>();

    private static TmfAnalysisType fInstance = null;

    /**
     * Retrieves all configuration elements from the platform extension registry
     * for the trace type extension.
     *
     * @return an array of trace type configuration elements
     */
    public static IConfigurationElement[] getTypeElements() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(TMF_ANALYSIS_TYPE_ID);
        List<IConfigurationElement> typeElements = new LinkedList<IConfigurationElement>();
        for (IConfigurationElement element : elements) {
            if (element.getName().equals(MODULE_ELEM)) {
                typeElements.add(element);
            }
        }
        return typeElements.toArray(new IConfigurationElement[typeElements.size()]);
    }

    private TmfAnalysisType() {
        init();
    }

    /**
     * The import utils instance
     *
     * @return the import utils instance
     * @since 2.0
     */
    public static TmfAnalysisType getInstance() {
        if (fInstance == null) {
            fInstance = new TmfAnalysisType();
        }
        return fInstance;
    }

    /**
     * Get the list of analysis modules
     *
     * @return list of analysis modules
     */
    public List<IAnalysisModule> getAnalysisModules() {
        List<IAnalysisModule> modules = new ArrayList<IAnalysisModule>();
        for (String key : fAnalysisTypeAttributes.keySet()) {
            IConfigurationElement ce = fAnalysisTypeAttributes.get(key);
            IAnalysisModule module;
            try {
                module = (IAnalysisModule) ce.createExecutableExtension(ANALYSIS_MODULE_ATTR);
                module.setName(ce.getAttribute(NAME_ATTR));
                module.setId(ce.getAttribute(ID_ATTR));
                module.setAutomatic(Boolean.valueOf(ce.getAttribute(AUTOMATIC_ATTR)));
                module.setIcon(ce.getAttribute(ICON_ATTR));

                /* Get the module's parameters */
                final IConfigurationElement[] parametersCE = ce.getChildren(PARAMETER_ELEM);
                for (IConfigurationElement element : parametersCE) {
                    module.addParameter(element.getAttribute(NAME_ATTR));
                    String defaultValue = element.getAttribute(DEFAULT_VALUE_ATTR);
                    if (defaultValue != null) {
                        module.setParameter(element.getAttribute(NAME_ATTR), defaultValue);
                    }
                }

                modules.add(module);
            } catch (CoreException e) {
            }

        }
        return modules;
    }

    private void populateAnalysisTypes() {
        if (fAnalysisTypeAttributes.isEmpty()) {
            // Populate the Categories and Trace Types
            IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(TMF_ANALYSIS_TYPE_ID);
            for (IConfigurationElement ce : config) {
                String elementName = ce.getName();
                if (elementName.equals(TmfAnalysisType.MODULE_ELEM)) {
                    String analysisTypeId = ce.getAttribute(TmfAnalysisType.ID_ATTR);
                    fAnalysisTypeAttributes.put(analysisTypeId, ce);
                }
            }
        }
    }

    private void init() {
        populateAnalysisTypes();
    }

}
