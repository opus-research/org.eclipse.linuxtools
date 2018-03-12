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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Custom text wizard tests
 *
 * Some reminders to help making tests (javadoc to keep formatting)
 *
 * Button reminder
 *
 * <pre>
 * 0 Time Stamp Format Help
 * 1 Remove line
 * 2 Add next line
 * 3 Add child line
 * 4 Move up
 * 5 Move down
 * 6 Regular Expression Help
 * 7 Remove group (group 1 toggle)
 * 8 Remove group (group 2 toggle)
 * 9 Add group (group 3 toggle ...)
 * 10 Show parsing result
 * 11 Preview Legend
 * </pre>
 *
 * Combo box reminder
 *
 * <pre>
 * 0 cardinality
 * 1 event type (message, timestamp...)
 * 2 how to handle the data (set, append...)
 * repeat
 * </pre>
 *
 * @author Matthew Khouzam
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TestImportExportWizard {

    private static final String EXPORT_LOCATION = "/tmp/test.zip";
    private static final String ORG_ECLIPSE_LINUXTOOLS_BTF_TRACETYPE = "org.eclipse.linuxtools.btf.tracetype";
    private static final String IMPORT_TRACE_PACKAGE = "Import Trace Package...";
    private static final String EXPORT_TRACE_PACKAGE = "Export Trace Package...";
    private static final String PROJECT_EXPLORER = "Project Explorer";
    private static final String TRACE_NODE = "Trace";
    private static final String FINISH = "Finish";
    private static final String COMPRESS_THE_CONTENTS_OF_THE_FILE = "Compress the contents of the file";
    private static final String SAVE_IN_ZIP_FORMAT = "Save in zip format";
    private static final String SAVE_IN_TAR_FORMAT = "Save in tar format";
    private static final String SELECT_ALL = "Select All";
    private static final String DESELECT_ALL = "Deselect All";
    private static final String TRACE_DIRECTORY = "TraceDir";
    private static final String WELCOME_NAME = "welcome";
    private static final String SWT_BOT_THREAD_NAME = "SWTBot Thread";
    private static final String PROJECT_NAME = "Test";

    private static final String TRACE_CONTENT = "#timeScale ns\n"
            + "10, Task_A, 0, R, Runnable_A_1, 0, start\n"
            + "20, Task_A, 0, R, Runnable_A_1, 0, terminate\n"
            + "30, Task_A, 0, R, Runnable_A_2, 0, start ";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

    /** Test Class setup */
    @BeforeClass
    public static void init() {
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

    @Test
    public void test() throws IOException {

        File f = File.createTempFile("temp", ".btf");
        try (FileWriter fw = new FileWriter(f)) {
            fw.write(TRACE_CONTENT);
        }
        final String name = f.getName();

        SWTBotUtil.createProject(PROJECT_NAME);
        SWTBotUtil.openTrace(PROJECT_NAME, f.getAbsolutePath(), ORG_ECLIPSE_LINUXTOOLS_BTF_TRACETYPE);
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);

        projectExplorerBot.show();
        SWTBotTreeItem treeItem = projectExplorerBot.bot().tree().getTreeItem(PROJECT_NAME);
        treeItem.select();
        treeItem.expand();
        treeItem = treeItem.getItems()[1];

        SWTWorkbenchBot bot = new SWTWorkbenchBot();

        treeItem.contextMenu(EXPORT_TRACE_PACKAGE).click();
        bot.button(DESELECT_ALL).click();
        bot.button(SELECT_ALL).click();
        bot.button(DESELECT_ALL).click();
        bot.tree().getTreeItem(name).select();
        bot.tree().getTreeItem(name).select();
        bot.radio(SAVE_IN_TAR_FORMAT).click();
        bot.radio(SAVE_IN_ZIP_FORMAT).click();
        bot.checkBox(COMPRESS_THE_CONTENTS_OF_THE_FILE).click();
        bot.checkBox(COMPRESS_THE_CONTENTS_OF_THE_FILE).click();
        bot.comboBox().setText(EXPORT_LOCATION);
        bot.button(FINISH).click();
        bot.button("Yes").click();
        treeItem.contextMenu(IMPORT_TRACE_PACKAGE).click();
        bot.comboBox().setText(EXPORT_LOCATION);
        bot.button(SELECT_ALL).click();
        bot.button(DESELECT_ALL).click();
        bot.tree().getTreeItem(TRACE_DIRECTORY).getNode(TRACE_NODE).select();
        bot.tree().getTreeItem(TRACE_DIRECTORY).getNode(TRACE_NODE).select();
        bot.tree().getTreeItem(TRACE_DIRECTORY).select();
        bot.tree().getTreeItem(TRACE_DIRECTORY).select();
        bot.tree().getTreeItem(TRACE_DIRECTORY).select();
        bot.tree().getTreeItem(TRACE_DIRECTORY).select();
        bot.button(FINISH).click();
        bot.button("No").click();
    }

}
