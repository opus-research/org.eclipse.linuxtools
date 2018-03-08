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

package org.eclipse.linuxtools.tmf.ui.tests.project.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.ui.tests.experiment.type.TmfEventsEditorStub;
import org.eclipse.linuxtools.tmf.ui.tests.experiment.type.TmfEventsTableExperimentStub;
import org.eclipse.linuxtools.tmf.ui.tests.experiment.type.TmfStatisticsViewerStub;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.views.statistics.TmfStatisticsView;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Some unit tests for trace types and experiment types
 *
 * @author Geneviève Bastien
 */
public class TraceAndExperimentTypeTest {

    /** Test experiment type id */
    public final static String TEST_EXPERIMENT_TYPE = "org.eclipse.linuxtools.tmf.tests.experimenttype";

    private TmfProjectElement fixture;
    private TmfExperimentElement fExperiment;
    private final String EXPERIMENT_NAME = "exp_test";

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        assumeTrue(CtfTmfTestTrace.KERNEL.exists());
        try {
            fixture = ProjectModelTestData.getFilledProject();
            fExperiment = ProjectModelTestData.addExperiment(fixture, EXPERIMENT_NAME);
            assertNotNull(fExperiment);
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Cleans up the project after tests have been executed
     */
    @After
    public void cleanUp() {
        try {
            ProjectModelTestData.deleteProject(fixture);
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test whether a newly created experiment has the default experiment type,
     * even though none was specified
     */
    @Test
    public void testDefaultExperimentType() {
        TmfExperimentElement experimentElement = ProjectModelTestData.addExperiment(fixture, "testDefaultExpType");
        assertNotNull(experimentElement);
        TmfExperiment experiment = experimentElement.instantiateTrace();
        assertNotNull(experiment);
        assertEquals(TmfTraceType.DEFAULT_EXPERIMENT_TYPE, experimentElement.getTraceType());
    }

    /**
     * Test that event editor, event table and statistics viewer are the default
     * ones for a generic experiment
     */
    @Test
    public void testNoExperimentTypeChildren() {
        /* Initialize statistics viewer */
        TmfStatisticsViewerStub.LAST_INSTANCE = null;

        TmfOpenTraceHelper.openTraceFromElement(fExperiment);

        ProjectModelTestData.delayThread(200);

        final IWorkbench wb = PlatformUI.getWorkbench();
        final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
        IEditorPart editor = activePage.getActiveEditor();

        /* Test the editor class. Cannot test table class since it is unexposed */
        assertNotNull(editor);
        assertTrue(editor.getClass().equals(TmfEventsEditor.class));

        /* Check statistics viewer class */
        try {
            IViewPart view = activePage.showView(TmfStatisticsView.ID);
            assertNotNull(view);
            assertTrue(view instanceof TmfStatisticsView);
        } catch (PartInitException e) {
            fail(e.getMessage());
        }

        ProjectModelTestData.delayThread(200);

        TmfStatisticsViewerStub statViewer = TmfStatisticsViewerStub.LAST_INSTANCE;
        assertNull(statViewer);
    }

    /**
     * Test that event editor, event table and statistics viewer are built
     * correctly when specified
     */
    @Test
    public void testExperimentTypeChildren() {

        /* Set the trace type of the experiment */
        IResource resource = fExperiment.getResource();
        try {
            resource.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE, "org.eclipse.linuxtools.tmf.ui.tests");
            resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, TEST_EXPERIMENT_TYPE);
            fExperiment.refreshTraceType();
        } catch (CoreException e) {
            fail(e.getMessage());
        }

        /* Initialize statistics viewer */
        TmfStatisticsViewerStub.LAST_INSTANCE = null;

        TmfOpenTraceHelper.openTraceFromElement(fExperiment);

        ProjectModelTestData.delayThread(200);

        /* Test the editor class */
        final IWorkbench wb = PlatformUI.getWorkbench();
        final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
        IEditorPart editor = activePage.getActiveEditor();

        assertNotNull(editor);
        assertTrue(editor.getClass().equals(TmfEventsEditorStub.class));

        /* Test the event table class */
        TmfEventsEditorStub editorStub = (TmfEventsEditorStub) editor;
        TmfEventsTable table = editorStub.getNewEventsTable();

        assertNotNull(table);
        assertTrue(table.getClass().equals(TmfEventsTableExperimentStub.class));

        /*
         * Check statistics viewer class. Make sure the viewer stub was built
         * and applies to the experiment
         */
        try {
            IViewPart view = activePage.showView(TmfStatisticsView.ID);
            assertNotNull(view);
            assertTrue(view instanceof TmfStatisticsView);
        } catch (PartInitException e) {
            fail(e.getMessage());
        }

        ProjectModelTestData.delayThread(200);

        TmfStatisticsViewerStub statViewer = TmfStatisticsViewerStub.LAST_INSTANCE;
        assertNotNull(statViewer);
        assertEquals(EXPERIMENT_NAME, statViewer.getTrace().getName());
    }

}
