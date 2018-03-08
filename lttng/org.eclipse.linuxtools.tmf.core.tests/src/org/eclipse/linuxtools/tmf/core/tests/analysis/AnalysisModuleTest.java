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

package org.eclipse.linuxtools.tmf.core.tests.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.tests.stubs.analysis.TestAnalysis;
import org.junit.Test;

/**
 * Test suite for the TmfAnalysisModule class
 */
public class AnalysisModuleTest {

    private static String MODULE_ID="test.id";
    private static String MODULE_NAME="Test analysis";

    /**
     * Test suite for analysis module getters and setters
     */
    @Test
    public void testGettersSetters() {
        IAnalysisModule module = new TestAnalysis();

        module.setName(MODULE_NAME);
        module.setId(MODULE_ID);
        assertEquals(MODULE_ID, module.getId());
        assertEquals(MODULE_NAME, module.getName());

        module.setAutomatic(false);
        assertFalse(module.isAutomatic());
        module.setAutomatic(true);
        assertTrue(module.isAutomatic());
    }

}
