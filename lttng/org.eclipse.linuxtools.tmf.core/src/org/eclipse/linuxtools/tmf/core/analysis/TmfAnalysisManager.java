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

import org.eclipse.linuxtools.internal.tmf.core.analysis.TmfAnalysisType;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Manages the available analysis from the extension point
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfAnalysisManager {

    private static List<IAnalysisModule> fAnalysisModules = new ArrayList<IAnalysisModule>();

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

}
