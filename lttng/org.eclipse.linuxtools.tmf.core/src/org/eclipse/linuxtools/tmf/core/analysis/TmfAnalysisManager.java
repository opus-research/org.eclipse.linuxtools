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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.internal.tmf.core.analysis.TmfAnalysisType;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Manages the available analysis from the extension point
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfAnalysisManager {

    private static final List<TmfAnalysisModuleHelper> fAnalysisModules = new ArrayList<TmfAnalysisModuleHelper>();
    private static final Map<String, List<Class<? extends IAnalysisParameterProvider>>> fParameterProviders = new HashMap<String, List<Class<? extends IAnalysisParameterProvider>>>();

    /**
     * Gets all available analysis modules
     *
     * This map is read-only
     *
     * @return The map of available analysis modules
     */
    public static Map<String, TmfAnalysisModuleHelper> getAnalysisModules() {
        synchronized (fAnalysisModules) {
            if (fAnalysisModules.isEmpty()) {
                TmfAnalysisType analysis = TmfAnalysisType.getInstance();
                List<TmfAnalysisModuleHelper> modules = analysis.getAnalysisModules();
                for (TmfAnalysisModuleHelper module : modules) {
                    fAnalysisModules.add(module);
                }
            }
        }
        return getExecutableModules();
    }

    /**
     * Gets all analysis module that apply to a given trace
     *
     * This map is read-only
     *
     * @param trace
     *            The trace to get modules for
     * @return The map of available analysis modules
     */
    public static Map<String, TmfAnalysisModuleHelper> getAnalysisModules(ITmfTrace trace) {
        return getAnalysisModules(trace.getClass());
    }

    /**
     * Gets all analysis module that apply to a given trace type
     *
     * This map is read-only
     *
     * @param traceclass
     *            The trace class to get modules for
     * @return The map of available analysis modules
     */
    public static Map<String, TmfAnalysisModuleHelper> getAnalysisModules(Class<? extends ITmfTrace> traceclass) {
        Map<String, TmfAnalysisModuleHelper> allModules = getAnalysisModules();
        Map<String, TmfAnalysisModuleHelper> map = new HashMap<String, TmfAnalysisModuleHelper>();
        for (TmfAnalysisModuleHelper module : allModules.values()) {
            if (module.appliesToTraceType(traceclass)) {
                map.put(module.getId(), module);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Returns the map of executable modules. It is not saved in a static field
     * as some modules may have dynamic sub-modules. We want this list to be
     * updated.
     *
     * TODO: The map should not be computed each time this method is called
     * (quite often), it should be kept as a field and add a method to notify
     * the manager that new analysis appeared
     *
     * @return The map of executables modules
     */
    private static Map<String, TmfAnalysisModuleHelper> getExecutableModules() {
        Map<String, TmfAnalysisModuleHelper> map = new HashMap<String, TmfAnalysisModuleHelper>();
        for (TmfAnalysisModuleHelper module : fAnalysisModules) {
            List<TmfAnalysisModuleHelper> submodules = module.getExecutableModules();
            for (TmfAnalysisModuleHelper submodule : submodules) {
                map.put(submodule.getId(), submodule);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Gets an analysis module identified by an id
     *
     * @param id
     *            Id of the analysis module to get
     * @return The Analysis module
     */
    public static TmfAnalysisModuleHelper getAnalysisModule(String id) {
        Map<String, TmfAnalysisModuleHelper> map = getAnalysisModules();
        return map.get(id);
    }

    /**
     * Register a new parameter provider for an analysis
     *
     * @param analysisId
     *            The id of the analysis
     * @param paramProvider
     *            The class of the parameter provider
     */
    public static void registerParameterProvider(String analysisId, Class<? extends IAnalysisParameterProvider> paramProvider) {
        synchronized (fParameterProviders) {
            if (!fParameterProviders.containsKey(analysisId)) {
                fParameterProviders.put(analysisId, new ArrayList<Class<? extends IAnalysisParameterProvider>>());
            }
            fParameterProviders.get(analysisId).add(paramProvider);
        }
    }

    /**
     * Get a parameter provider that applies to the requested trace
     *
     * @param module
     *            Analysis module
     * @param trace
     *            The trace
     * @return A parameter provider if one applies to the trace, null otherwise
     */
    public static List<IAnalysisParameterProvider> getParameterProviders(IAnalysisModule module, ITmfTrace trace) {
        List<IAnalysisParameterProvider> providerList = new ArrayList<IAnalysisParameterProvider>();
        synchronized (fParameterProviders) {
            if (!fParameterProviders.containsKey(module.getId())) {
                return providerList;
            }
            for (Class<? extends IAnalysisParameterProvider> providerClass : fParameterProviders.get(module.getId())) {
                try {
                    IAnalysisParameterProvider provider = providerClass.newInstance();
                    if (provider.appliesToTrace(trace)) {
                        providerList.add(provider);
                    }
                } catch (IllegalArgumentException e) {
                    Activator.logError(Messages.TmfAnalysisManager_ErrorParameterProvider, e);
                } catch (SecurityException e) {
                    Activator.logError(Messages.TmfAnalysisManager_ErrorParameterProvider, e);
                } catch (InstantiationException e) {
                    Activator.logError(Messages.TmfAnalysisManager_ErrorParameterProvider, e);
                } catch (IllegalAccessException e) {
                    Activator.logError(Messages.TmfAnalysisManager_ErrorParameterProvider, e);
                }
            }
        }
        return providerList;
    }

}
