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
import java.util.Map.Entry;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.internal.tmf.core.analysis.TmfAnalysisType;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Manages the available analysis from the extension point
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfAnalysisManager {

    private static final List<IAnalysisModule> fAnalysisModules = new ArrayList<IAnalysisModule>();
    private static final Map<String, List<Class<? extends IAnalysisParameterProvider>>> fParameterProviders = new HashMap<String, List<Class<? extends IAnalysisParameterProvider>>>();

    /**
     * Gets all available analysis modules
     *
     * This list is read-only
     *
     * @return The list of available analysis modules
     */
    public static Map<String, IAnalysisModule> getAnalysisModules() {
        if (fAnalysisModules.isEmpty()) {
            TmfAnalysisType analysis = TmfAnalysisType.getInstance();
            List<IAnalysisModule> modules = analysis.getAnalysisModules();
            for (IAnalysisModule module : modules) {
                fAnalysisModules.add(module);
            }
        }
        return getExecutableModules();
    }

    /**
     * Gets all analysis module that apply to a given trace
     *
     * This list is read-only
     *
     * @param trace
     *            The trace to get modules for
     * @return The list of available analysis modules
     */
    public static Map<String, IAnalysisModule> getAnalysisModules(ITmfTrace trace) {
        return getAnalysisModules(trace.getClass());
    }

    /**
     * Gets all analysis module that apply to a given trace type
     *
     * This list is read-only
     *
     * @param traceclass
     *            The trace class to get modules for
     * @return The list of available analysis modules
     */
    public static Map<String, IAnalysisModule> getAnalysisModules(Class<? extends ITmfTrace> traceclass) {
        Map<String, IAnalysisModule> allModules = getAnalysisModules();
        Map<String, IAnalysisModule> map = new HashMap<String, IAnalysisModule>();
        for (IAnalysisModule module : allModules.values()) {
            if (module.appliesToTraceType(traceclass)) {
                map.put(module.getId(), module);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Returns the list of executable modules. It is not saved in a static field
     * as some modules may have dynamic sub-modules. We want this list to be
     * updated.
     *
     * @return The map of executables modules
     */
    private static Map<String, IAnalysisModule> getExecutableModules() {
        Map<String, IAnalysisModule> map = new HashMap<String, IAnalysisModule>();
        for (IAnalysisModule module : fAnalysisModules) {
            List<IAnalysisModule> submodules = module.getExecutableModules();
            for (IAnalysisModule submodule : submodules) {
                map.put(submodule.getId(), submodule);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Gets a specific analysis module for a given trace
     *
     * TODO There is only one instance of each analysis module for all traces.
     * That could bring concurrency problem if the analysis is running on a
     * trace and it is also requested by another trace. A copy should be made,
     * or we could store the configuration element and create an instance when
     * necessary, but we need to know who would own the analysis (no one except
     * the manager now). Unless the manager also manages a trace's analysis.
     *
     * @param trace
     *            The trace to get a module for
     * @param id
     *            The id of the module to fetch
     * @return The module or null if unavailable
     */
    public static IAnalysisModule getAnalysisModule(ITmfTrace trace, String id) {
        Map<String, IAnalysisModule> map = getAnalysisModules(trace);
        return map.get(id);
    }

    /**
     * Gets an analysis module identified by an id
     *
     * @param id
     *            Id of the analysis module to get
     * @return The Analysis module
     */
    public static IAnalysisModule getAnalysisModule(String id) {
        Map<String, IAnalysisModule> map = getAnalysisModules();
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
        if (!fParameterProviders.containsKey(analysisId)) {
            fParameterProviders.put(analysisId, new ArrayList<Class<? extends IAnalysisParameterProvider>>());
        }
        fParameterProviders.get(analysisId).add(paramProvider);
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
                Activator.logError("Error instantiating parameter provider", e); //$NON-NLS-1$
            } catch (SecurityException e) {
                Activator.logError("Error instantiating parameter provider", e); //$NON-NLS-1$
            } catch (InstantiationException e) {
                Activator.logError("Error instantiating parameter provider", e); //$NON-NLS-1$
            } catch (IllegalAccessException e) {
                Activator.logError("Error instantiating parameter provider", e); //$NON-NLS-1$
            }
        }
        return providerList;
    }

    /**
     * Get the list of state systems modules
     *
     * @param trace
     *            The trace to get the modules from
     * @return List of state system modules
     */
    public static List<IAnalysisModule> getStateSystems(ITmfTrace trace) {
        List<IAnalysisModule> list = new ArrayList<IAnalysisModule>();
        Map<String, IAnalysisModule> modules = getAnalysisModules(trace);

        for (Entry<String, IAnalysisModule> entry : modules.entrySet()) {
            if (entry.getValue() instanceof TmfStateSystemAnalysisModule) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

}
