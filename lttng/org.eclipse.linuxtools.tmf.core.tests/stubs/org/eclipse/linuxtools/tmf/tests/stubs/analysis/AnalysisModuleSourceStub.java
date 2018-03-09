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

package org.eclipse.linuxtools.tmf.tests.stubs.analysis;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleSource;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.AnalysisModuleTestHelper.moduleStubEnum;

/**
 * Stub class for analysis module source
 *
 * @author Geneviève Bastien
 */
public class AnalysisModuleSourceStub implements IAnalysisModuleSource {

    @Override
    public Map<String, IAnalysisModuleHelper> getAnalysisModules() {
        Map<String, IAnalysisModuleHelper> map = new HashMap<String, IAnalysisModuleHelper>();
        IAnalysisModuleHelper helper = new AnalysisModuleTestHelper(moduleStubEnum.TEST);
        map.put(helper.getId(), helper);
        helper = new AnalysisModuleTestHelper(moduleStubEnum.TESTCTF);
        map.put(helper.getId(), helper);
        return map;
    }

}
