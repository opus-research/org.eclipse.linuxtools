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

package org.eclipse.linuxtools.tmf.ui.tests.smoketest;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace.BatchImportTraceWizard;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramView;
import org.eclipse.linuxtools.tmf.ui.views.statistics.TmfStatisticsView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.views.properties.PropertySheet;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * SWTBot Smoke test. base for other tests
 *
 * @author Matthew Khouzam
 */
public class ImportAndReadSmokeTest {

    private static final String PROJECT_EXPLORER_ID = "org.eclipse.ui.navigator.ProjectExplorer";
    private static IWorkbench workbench;
    private static SWTWorkbenchBot bot;
    private static volatile BatchImportTraceWizard bitw = null;
    private static CtfTmfTestTrace ctt = CtfTmfTestTrace.SYNTHETIC_TRACE;
    private static final String traceName = "synthetic-trace";
    private ITmfEvent fDesired1;
    private ITmfEvent fDesired2;

    /**
     * Test Class setup
     *
     * @throws WorkbenchException
     *             exceptions to throw
     * @throws InterruptedException
     *             modal exception
     */
    @BeforeClass
    public static void init() throws WorkbenchException, InterruptedException {
        /*
         * set up for swtbot
         */
        SWTBotPreferences.TIMEOUT = 50000; /* 50 second timeout */

        bot = new SWTWorkbenchBot();
        final List<SWTBotView> openViews = bot.views();
        for (SWTBotView view : openViews) {
            if (view.getTitle().equals("Welcome")) {
                view.close();
            }
        }
        /*
         * Switch perspectives
         */
        {
            workbench = PlatformUI.getWorkbench();
            IPerspectiveDescriptor x = workbench.getPerspectiveRegistry().findPerspectiveWithId("org.eclipse.linuxtools.lttng2.kernel.ui.perspective");
            workbench.showPerspective(x.getId(), workbench.getActiveWorkbenchWindow());
        }
        /*
         * finish waiting for eclipse to load
         */
        waitForJobs();

        /*
         * Make a new test
         */
        TmfProjectRegistry.createProject("test", null, new NullProgressMonitor());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        assertNotNull(window);
        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final SWTBotView projectViewBot = bot.viewById(PROJECT_EXPLORER_ID);
        projectViewBot.setFocus();
        waitForJobs();
        IWorkbenchPart part = page.getActivePart();

        final Composite widget = (Composite) projectViewBot.getWidget();
        final Composite control = (Composite) widget.getChildren()[0];
        Tree tree = (Tree) control.getChildren()[0];
        assertNotNull(part);
        SWTBotTree treeBot = new SWTBotTree(tree);
        final SWTBotTreeItem treeItem = treeBot.getTreeItem("test");
        if (treeItem != null) {
            treeItem.select();
            Job j = new Job("Delete") {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    monitor.beginTask("Delete", 10);
                    bot.waitUntil(Conditions.shellIsActive("Delete Resources"));
                    // find modal window
                    monitor.worked(5);
                    bot = new SWTWorkbenchBot();
                    bot.button("OK").click();
                    monitor.done();
                    return Status.OK_STATUS;
                }
            };
            j.schedule();
            treeItem.contextMenu("Delete").click();
            j.join();
            // modal window

        }
        waitForJobs();
    }

    /**
     * Add a trace to the batch import wizard, workaround for the os specific
     * non-swt dialog
     *
     * @param batchImportTraceWizard
     *            the batch import trace wizard
     * @param f
     *            the file to add
     * @throws IllegalAccessException
     *             reflexion issues
     * @throws InvocationTargetException
     *             reflexion issues
     */
    public static void addTrace(BatchImportTraceWizard batchImportTraceWizard, File f) throws IllegalAccessException, InvocationTargetException {
        Class<? extends BatchImportTraceWizard> batch = batchImportTraceWizard.getClass();
        Method meth[] = batch.getMethods();
        for (Method man : meth) {
            if (man.getName().contains("Test")) {
                man.setAccessible(true);
                man.invoke(batchImportTraceWizard, f, "test");
            }
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
                display.update();
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

        createProject();
        // reset
        bot = new SWTWorkbenchBot();
        bot.viewById(PROJECT_EXPLORER_ID);

        final SWTBotTree tree = bot.tree();

        final SWTBotTreeItem treeItem = tree.getTreeItem("test");
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

        Job job = new Job("Batch Import testing") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                initModalPage(monitor);
                selecTraceType(monitor);
                addDirectoryPage(monitor);
                selectTrace(monitor);
                targetPage(monitor);
                finishBitw();
                monitor.done();
                return Status.OK_STATUS;
            }

            private void initModalPage(IProgressMonitor monitor) {
                monitor.beginTask("BITW testing", 100);
                while (bitw == null) {
                    miniWait();
                }
            }

            private void targetPage(IProgressMonitor monitor) {
                monitor.worked(2);
                monitor.subTask("Target");
            }

            private void selecTraceType(IProgressMonitor monitor) {
                bot = new SWTWorkbenchBot();
                monitor.subTask("TraceType");
                monitor.worked(10);
                SWTBotTree treeBot = bot.tree();
                while (!treeBot.getTreeItem("Common Trace Format").getNode("LTTng Kernel Trace").isChecked()) {
                    treeBot.getTreeItem("Common Trace Format").getNode("LTTng Kernel Trace").check();
                }
                clickNextBitw();
            }

            private void addDirectoryPage(IProgressMonitor monitor) {
                bot = new SWTWorkbenchBot();
                monitor.subTask("Directory");
                Display.getDefault().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        bitw.addFileToScan(ctt.getPath());
                    }
                });
                miniWait();
                final SWTBotButton removeButton = bot.button("Remove");
                removeButton.click();
                miniWait();
                monitor.worked(10);

                clickNextBitw();
            }

            private void selectTrace(IProgressMonitor monitor) {
                monitor.subTask("Select Trace Type");
                bot = new SWTWorkbenchBot();
                SWTBotTree treeBot;
                treeBot = bot.tree();
                miniWait();
                SWTBotTreeItem temp = treeBot.getTreeItem("LTTng Kernel Trace");
                miniWait();
                while (temp == null) {
                    bot = new SWTWorkbenchBot();
                    treeBot = bot.tree();
                    miniWait();
                    temp = treeBot.getTreeItem("LTTng Kernel Trace");
                }
                final SWTBotTreeItem genericCtfTreeItem = treeBot.getTreeItem("LTTng Kernel Trace");
                miniWait();
                genericCtfTreeItem.expand();
                miniWait();
                genericCtfTreeItem.check();
                miniWait();
                monitor.worked(10);
                clickNextBitw();
            }

            private void finishBitw() {
                bot = new SWTWorkbenchBot();
                final SWTBotButton finishButton = bot.button("Finish");
                finishButton.click();
                miniWait();
            }

            private void clickNextBitw() {
                bot = new SWTWorkbenchBot();
                SWTBotButton nextButton = bot.button("Next >");
                nextButton.click();
                miniWait();
            }

            private void miniWait() {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                }
            }
        };
        job.schedule();
        bitw = openBITW();
        job.join();

        waitForJobs();

        List<String> nodes = treeItem.getNodes();
        String nodeName = "";
        for (String node : nodes) {
            if (node.startsWith("Traces")) {
                nodeName = node;
            }
        }
        treeItem.getNode(nodeName).expand();

        treeItem.getNode(nodeName).getNode(traceName).select();
        treeItem.getNode(nodeName).getNode(traceName).doubleClick();
        display.sleep();
        Thread.sleep(1000);
        waitForJobs();
        IEditorReference[] ieds = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
        assertNotNull(ieds);
        IEditorPart iep = null;
        for (IEditorReference ied : ieds) {
            if (ied.getTitle().equals(traceName)) {
                iep = ied.getEditor(true);
                break;
            }
        }
        assertNotNull(iep);
        fDesired1 = getEvent(100);
        fDesired2 = getEvent(10000);
        TmfEventsEditor tmfEd = (TmfEventsEditor) iep;

        tmfEd.setFocus();
        tmfEd.selectionChanged(new SelectionChangedEvent(tmfEd, new StructuredSelection(fDesired1)));

        waitForJobs();
        Thread.sleep(1000);
        assertNotNull(tmfEd);
        IViewReference[] viewRefs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
        for (IViewReference viewRef : viewRefs) {
            IViewPart vp = viewRef.getView(true);
            if (vp.getTitle().equals("Histogram")) {
                testHV(vp);
            } else if (vp.getTitle().equals("Properties")) {
                testPV(vp);
            } else if (vp.getTitle().equals("Statistics")) {
                testSV(vp);
            }
        }
    }

    private static void testPV(IViewPart vp) {
        PropertySheet pv = (PropertySheet) vp;
        assertNotNull(pv);
    }

    private void testHV(IViewPart vp) {
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

    private static void testSV(IViewPart vp) {
        TmfStatisticsView sv = (TmfStatisticsView) vp;
        assertNotNull(sv);
    }

    private static CtfTmfEvent getEvent(int rank) {
        CtfTmfTrace trace = CtfTmfTestTrace.SYNTHETIC_TRACE.getTrace();
        if( trace == null ) {
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
        if (!display.readAndDispatch()) {
            display.sleep();
        }
    }

    private static void createProject() throws InterruptedException {
        bot = new SWTWorkbenchBot();
        Job j = new Job("Create Project") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                bot = new SWTWorkbenchBot();
                bot.waitUntil(Conditions.shellIsActive("New Project"));
                final SWTBotTreeItem tracingNode = bot.tree().getTreeItem("Tracing");
                if (!tracingNode.select().isExpanded()) {
                    tracingNode.expand();
                }

                monitor.worked(20);
                List<String> children = tracingNode.getNodes();

                final SWTBotTreeItem TracingProject = tracingNode.getNode(children.get(0));
                TracingProject.select();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                monitor.worked(40);
                bot.button("Next >").click();
                bot.text().setText("test");

                monitor.worked(80);
                bot.button("Finish").click();
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        j.schedule();
        bot.menu("File").menu("New").menu("Project...").click();
        j.join();

    }

    private static BatchImportTraceWizard openBITW() {
        TmfTraceFolder traceFolder = getTraceFolder();
        if (traceFolder == null) {
            bitw = null;
            return null;
        }

        // Fire the Import Trace Wizard
        if (workbench != null) {
            final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
            if (activeWorkbenchWindow != null) {
                Shell shell = activeWorkbenchWindow.getShell();

                bitw = new BatchImportTraceWizard();
                bitw.init(PlatformUI.getWorkbench(), new StructuredSelection(traceFolder));
                WizardDialog dialog = new WizardDialog(shell, bitw);
                dialog.open();

                traceFolder.refresh();
            }
        }
        return bitw;
    }

    private static TmfTraceFolder getTraceFolder() {
        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return null;
        }
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return null;
        }
        ISelection selection = selectionProvider.getSelection();

        TmfTraceFolder traceFolder = null;
        if (selection instanceof TreeSelection) {
            TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            Object element = sel.getFirstElement();
            if (element instanceof TmfTraceFolder) {
                traceFolder = (TmfTraceFolder) element;
            }
        }
        return traceFolder;
    }
}
