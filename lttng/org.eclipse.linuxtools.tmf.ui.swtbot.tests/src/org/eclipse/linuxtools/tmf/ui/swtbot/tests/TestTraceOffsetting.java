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
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.linuxtools.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.Test;

/**
 * Test trace offsetting
 *
 * @author Matthew Khouzam
 */
public class TestTraceOffsetting {

    private static final String TEST_FOR_OFFSETTING = "TestForOffsetting";

    private static final String TEST_TRACE = "#version 2.2.0\n" +
            "#creator Tester\n" +
            "#Producer Test case\n" +
            "#timeScale ns\n" +
            "100,TASK_Test_time_0,0,SCHED,SCHED_Tasks_C2,-1,processactivate\n" +
            "200,TASK_Test_time_0,0,SCHED,SCHED_Tasks_C2,-1,processactivate\n" +
            "300,TASK_Test_time_0,0,SCHED,SCHED_Tasks_C2,-1,processactivate\n" +
            "400,TASK_Test_time_0,0,SCHED,SCHED_Tasks_C2,-1,processactivate\n";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

    private File fLocation;

    /**
     * Initialization, creates a temp trace
     * @throws IOException should not happen
     */
    @Before
    public void Init() throws IOException {
        SWTBotUtil.failIfUIThread();
        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtil.closeView("welcome", fBot);

        SWTBotUtil.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        SWTBotUtil.waitForJobs();
        fLocation = File.createTempFile("sample", "btf");
        try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(fLocation, "w")) {
            braf.writeBytes(TEST_TRACE);
        }

    }

    /**
     * Test offsetting by 99 ns
     */
    @Test
    public void testOffsetting() {
        SWTBotTreeItem treeItem = SWTBotUtil.createProject(fBot, TEST_FOR_OFFSETTING);
        SWTBotUtil.openTrace(TEST_FOR_OFFSETTING, fLocation.getAbsolutePath());
        ITmfTimestamp before = TmfTraceManager.getInstance().getActiveTrace().getEndTime();
        treeItem.getItems()[0].contextMenu("Offset Trace").click();
        SWTBotUtil.waitForJobs();
        // set offset to 99 ns
        SWTBotShell shell = fBot.shell( org.eclipse.linuxtools.internal.tmf.ui.project.wizards.offset.Messages.OffsetDialog_Select);
        SWTBot shellBot = shell.bot();
        assertNotNull(shellBot);
        final SWTBotTreeItem swtBotTreeItem = shellBot.tree().getAllItems()[0];
        swtBotTreeItem.click(2);
        swtBotTreeItem.widget.setText(2, "99");
        SWTBotUtil.waitForJobs();
        // click "ok"
        shellBot.button("Ok").click();
        // re-open trace
        treeItem.getItems()[0].doubleClick();
        ITmfTimestamp after = TmfTraceManager.getInstance().getActiveTrace().getEndTime();
        assertEquals(99, after.getDelta(before).getValue());

    }

}
