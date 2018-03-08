/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.controlflow.ControlFlowView;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources.ResourcesView;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.conditions.ConditionHelpers;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.BoolResult;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * SWTBot Smoke test. base for other tests
 *
 * @author Matthew Khouzam
 */
public class ImportAndReadKernelSmokeTest {

    private static final String KERNEL_PERSPECTIVE_ID = "org.eclipse.linuxtools.lttng2.kernel.ui.perspective";
    private static final String TRACE_PROJECT_NAME = "test";
    private static final String JOB_FAMILY = "jobs.ImportAndReadKernelSmokeTest";

    private static SWTWorkbenchBot bot;
    private static CtfTmfTestTrace ctt = CtfTmfTestTrace.SYNTHETIC_TRACE;
    private ITmfEvent fDesired1;
    private ITmfEvent fDesired2;

    /** The Log4j logger instance. */
    private static final Logger logger = Logger.getRootLogger();

    /**
     * Test Class setup
     */
    @BeforeClass
    public static void init() {
        /*
         * set up for swtbot
         */
        SWTBotPreferences.TIMEOUT = 50000; /* 50 second timeout */
        Appender nullAppender = new NullAppender();
        logger.addAppender(nullAppender);

        bot = new SWTWorkbenchBot();
        final List<SWTBotView> openViews = bot.views();
        for (SWTBotView view : openViews) {
            if (view.getTitle().equals("Welcome")) {
                view.close();
                bot.waitUntil(ConditionHelpers.ViewIsClosed(view));
            }
        }
        /*
         * Switch perspectives
         */
        switchKernelPerspective();
        /*
         * finish waiting for eclipse to load
         */
        waitForJobs();

        /*
         * Make a new test
         */
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                TmfProjectRegistry.createProject(TRACE_PROJECT_NAME, null, new NullProgressMonitor());
            }
        });

        // Get the selection
        final SWTBotView projectViewBot = bot.viewById(IPageLayout.ID_PROJECT_EXPLORER);
        projectViewBot.setFocus();
        waitForJobs();
    }

    /**
     * Finalize, close extra jobs
     */
    @AfterClass
    public static void finish() {
        Job.getJobManager().cancel(JOB_FAMILY);
        try {
            Job.getJobManager().join(JOB_FAMILY, new NullProgressMonitor());
        } catch (OperationCanceledException e) {
            fail(e.getMessage());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    private static void switchKernelPerspective() {
        final Exception retE[] = new Exception[1];
        if (!UIThreadRunnable.syncExec(new BoolResult() {
            @Override
            public Boolean run() {
                try {
                    PlatformUI.getWorkbench().showPerspective(KERNEL_PERSPECTIVE_ID,
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow());
                } catch (WorkbenchException e) {
                    retE[0] = e;
                    return false;
                }
                return true;
            }
        })) {
            fail(retE[0].getMessage());
        }

    }

    /**
     * Waits for all Eclipse jobs to finish
     */
    public static void waitForJobs() {
        while (!Job.getJobManager().isIdle()) {
            delay(100);
        }
        refreshUI(bot.getDisplay());
    }

    /**
     * Sleeps current thread or GUI thread for a given time.
     *
     * @param waitTimeMillis
     *            time in milliseconds to wait
     */
    public static void delay(final long waitTimeMillis) {
        final Display display = bot.getDisplay();
        if (display != null) {
            final long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
            while (System.currentTimeMillis() < endTimeMillis) {
                refreshUI(display);
                UIThreadRunnable.syncExec(new VoidResult() {
                    @Override
                    public void run() {
                        display.update();
                    }
                });
            }
        } else {
            try {
                Thread.sleep(waitTimeMillis);
            } catch (final InterruptedException e) {
                // Ignored
            }
        }
    }

    /**
     * Main test case
     *
     * @throws InterruptedException
     *             exception
     */
    @Test
    public void test() throws InterruptedException {
        bot = new SWTWorkbenchBot();

        final SWTBotTree trees[] = new SWTBotTree[1];
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                final SWTBotView projectExplorerBot = bot.viewById(IPageLayout.ID_PROJECT_EXPLORER);
                Widget w = projectExplorerBot.getWidget();
                trees[0] = new SWTBotTree((Tree) ((Composite) ((Composite) w).getChildren()[0]).getChildren()[0]);
            }
        });

        bot.waitUntil(ConditionHelpers.IsTreeNodeAvailable(TRACE_PROJECT_NAME, trees[0]));

        final SWTBotTreeItem treeItem = trees[0].getTreeItem(TRACE_PROJECT_NAME);
        if (treeItem.isExpanded()) {
            treeItem.collapse();
        }
        final Display display = Display.getDefault();

        refreshUI(display);

        treeItem.select();
        treeItem.click();
        treeItem.expand();

        treeItem.getNode("Traces [0]").select();
        waitForJobs();
        final Exception exception[] = new Exception[1];
        exception[0] = null;
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                try {
                    (new TmfOpenTraceHelper()).openTraceFromPath("test", ctt.getPath(), bot.activeShell().widget, "org.eclipse.linuxtools.lttng2.kernel.tracetype");
                } catch (CoreException e) {
                    exception[0] = e;
                }
            }
        });
        if (exception[0] != null) {
            fail(exception[0].getMessage());
        }

        waitForJobs();
        final List<IEditorReference> editorRefs = new ArrayList<>();
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                IEditorReference[] ieds = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
                editorRefs.addAll(Arrays.asList(ieds));
            }

        });
        assertFalse(editorRefs.isEmpty());
        IEditorPart iep = null;
        for (IEditorReference ied : editorRefs) {
            if (ied.getTitle().equals(ctt.getTrace().getName())) {
                iep = ied.getEditor(true);
                break;
            }
        }
        assertNotNull(iep);
        fDesired1 = getEvent(100);
        fDesired2 = getEvent(10000);
        final TmfEventsEditor tmfEd = (TmfEventsEditor) iep;
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                tmfEd.setFocus();
                tmfEd.selectionChanged(new SelectionChangedEvent(tmfEd, new StructuredSelection(fDesired1)));
            }
        });

        waitForJobs();
        Thread.sleep(1000);
        assertNotNull(tmfEd);
        final List<IViewReference> viewRefs = new ArrayList<>();
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                IViewReference[] viewRefArray = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
                viewRefs.addAll(Arrays.asList(viewRefArray));
            }
        });

        for (final IViewReference viewRef : viewRefs) {
            final IViewPart[] viewPartArray = new IViewPart[1];
            UIThreadRunnable.syncExec(new VoidResult() {
                @Override
                public void run() {
                    viewPartArray[0] = viewRef.getView(true);
                }
            });
            final IViewPart viewPart = viewPartArray[0];
            if (viewPart.getTitle().equals("Histogram")) {
                testHV(viewPart);
            } else if (viewPart.getTitle().equals("Control Flow")) {
                testCFV((ControlFlowView) viewPart);
            } else if (viewPart.getTitle().equals("Resource")) {
                testRV((ResourcesView) viewPart);
            }
        }
    }

    private static void testCFV(ControlFlowView vp) {
        assertNotNull(vp);
    }

    private void testHV(IViewPart vp) {
        SWTBotView hvBot = (new SWTWorkbenchBot()).viewById(HistogramView.ID);
        List<SWTBotToolbarButton> hvTools = hvBot.getToolbarButtons();
        for (SWTBotToolbarButton hvTool : hvTools) {
            if (hvTool.getToolTipText().toLowerCase().contains("lost")) {
                hvTool.click();
            }
        }
        HistogramView hv = (HistogramView) vp;
        final TmfTimeSynchSignal signal = new TmfTimeSynchSignal(hv, fDesired1.getTimestamp());
        final TmfTimeSynchSignal signal2 = new TmfTimeSynchSignal(hv, fDesired2.getTimestamp());
        hv.updateTimeRange(100000);
        waitForJobs();
        hv.currentTimeUpdated(signal);
        hv.broadcast(signal);
        waitForJobs();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        hv.updateTimeRange(1000000000);
        waitForJobs();
        hv.currentTimeUpdated(signal2);
        hv.broadcast(signal2);
        waitForJobs();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        assertNotNull(hv);
    }

    private static void testRV(ResourcesView vp) {
        assertNotNull(vp);
    }

    private static CtfTmfEvent getEvent(int rank) {
        CtfTmfTrace trace = CtfTmfTestTrace.SYNTHETIC_TRACE.getTrace();
        if (trace == null) {
            return null;
        }
        ITmfContext ctx = trace.seekEvent(0);
        for (int i = 0; i < rank; i++) {
            trace.getNext(ctx);
        }
        final CtfTmfEvent retVal = trace.getNext(ctx);
        trace.dispose();
        return retVal;
    }

    private static void refreshUI(final Display display) {
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        });
    }

}
