/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Export and Import wizard tests
 *
 * @author Matthew Khouzam
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TestImportExportPackageWizard {

    private static final int PACKAGE_SIZE = 282349;
    private static final String EXPORT_LOCATION = System.getProperty("java.io.tmpdir") + File.separator + "test.zip";
    private static final String ORG_ECLIPSE_LINUXTOOLS_BTF_TRACETYPE = "org.eclipse.linuxtools.btf.trace";
    private static final String IMPORT_TRACE_PACKAGE = "Import Trace Package...";
    private static final String EXPORT_TRACE_PACKAGE = "Export Trace Package...";
    private static final String PROJECT_EXPLORER = "Project Explorer";
    private static final String FINISH = "Finish";
    private static final String COMPRESS_THE_CONTENTS_OF_THE_FILE = "Compress the contents of the file";
    private static final String SAVE_IN_ZIP_FORMAT = "Save in zip format";
    private static final String SAVE_IN_TAR_FORMAT = "Save in tar format";
    private static final String SELECT_ALL = "Select All";
    private static final String DESELECT_ALL = "Deselect All";
    private static final String WELCOME_NAME = "welcome";
    private static final String SWT_BOT_THREAD_NAME = "SWTBot Thread";
    private static final String PROJECT_NAME = "Test";

    private static final String TRACE_CONTENT = "#timeScale ns\n"
            + "10,Task_A,0,R,Runnable_A_1,0,start\n"
            + "20,Task_A,0,R,Runnable_A_1,0,terminate\n"
            + "30,Task_A,0,R,Runnable_A_2,0,start";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

    /** Test Class setup */
    @BeforeClass
    public static void init() {
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        SWTBotUtil.failIfUIThread();
        Thread.currentThread().setName(SWT_BOT_THREAD_NAME); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtil.closeView(WELCOME_NAME, fBot);

        SWTBotUtil.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        SWTBotUtil.waitForJobs();

    }

    /**
     * test opening a trace, importing
     *
     * @throws IOException
     *             won't happen
     */
    @Test
    public void test() throws IOException {
        File f = File.createTempFile("temp", ".btf");
        try (FileWriter fw = new FileWriter(f)) {
            fw.write(TRACE_CONTENT);
        }
        File exportPackage = new File(EXPORT_LOCATION);
        if (exportPackage.exists()) {
            exportPackage.delete();
        }
        assertFalse("File: " + EXPORT_LOCATION + " already present, aborting test", exportPackage.exists());
        assertTrue("Trace :" + f.getAbsolutePath() + " does not exist, aborting test", f.exists());
        SWTBotUtil.createProject(PROJECT_NAME);
        SWTBotUtil.openTrace(PROJECT_NAME, f.getAbsolutePath(), ORG_ECLIPSE_LINUXTOOLS_BTF_TRACETYPE);
        SWTBotUtil.waitForJobs();
        assertEquals("Incorrect opened trace!", f.getAbsolutePath(), (new File(TmfTraceManager.getInstance().getActiveTrace().getPath())).getAbsolutePath());
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        assertNotNull("Cannot find " + PROJECT_EXPLORER, projectExplorerBot);
        projectExplorerBot.show();
        SWTBotTreeItem treeItem = getTracesFolderTreeItem(projectExplorerBot);

        treeItem.contextMenu(EXPORT_TRACE_PACKAGE).click();
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        bot.button(DESELECT_ALL).click();
        SWTBotTreeItem[] items = bot.tree().getAllItems();
        for (SWTBotTreeItem item : items) {
            assertEquals(item.isChecked(), false);
        }
        bot.button(SELECT_ALL).click();
        for (SWTBotTreeItem item : items) {
            assertEquals(item.isChecked(), true);
        }
        bot.radio(SAVE_IN_TAR_FORMAT).click();
        bot.radio(SAVE_IN_ZIP_FORMAT).click();

        bot.checkBox(COMPRESS_THE_CONTENTS_OF_THE_FILE).click();
        bot.checkBox(COMPRESS_THE_CONTENTS_OF_THE_FILE).click();
        bot.comboBox().typeText(EXPORT_LOCATION);
        bot.button(FINISH).click();
        // finished exporting
        SWTBotUtil.waitForJobs();
        exportPackage = new File(EXPORT_LOCATION);
        assertTrue("Exported package", exportPackage.exists());
        assertEquals("Exported package size check", PACKAGE_SIZE, exportPackage.length());
        // import
        treeItem = getTracesFolderTreeItem(projectExplorerBot);
        treeItem.contextMenu(IMPORT_TRACE_PACKAGE).click();
        bot = new SWTWorkbenchBot();
        bot.comboBox().typeText(EXPORT_LOCATION + "\n");
        bot = new SWTWorkbenchBot();
        bot.button(SELECT_ALL).click();
        bot.button(FINISH).click();
        bot.button("Yes To All").click();
        // finish import
        // open
        projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        projectExplorerBot.show();
        treeItem = getTracesFolderTreeItem(projectExplorerBot);
        treeItem.select();
        treeItem.expand();
        assertEquals("Tree missmatch", Arrays.asList(f.getName()), treeItem.getNodes());
        treeItem = treeItem.getNode(f.getName());
        treeItem.doubleClick();
        assertEquals("Test if import matches", f.getName(), TmfTraceManager.getInstance().getActiveTrace().getName());
        assertFalse("Test if import files don't match", f.getAbsolutePath().equals(TmfTraceManager.getInstance().getActiveTrace().getPath()));
        SWTBotUtil.waitForJobs();
    }

    private static SWTBotTreeItem getTracesFolderTreeItem(SWTBotView projectExplorerBot) {
        SWTBotTreeItem treeItem = projectExplorerBot.bot().tree().getTreeItem(PROJECT_NAME);
        treeItem.select();
        treeItem.expand();
        assertEquals("Tree missmatch", Arrays.asList("Experiments [0]", "Traces [1]"), treeItem.getNodes());
        return treeItem.getNode("Traces [1]");

    }

}
