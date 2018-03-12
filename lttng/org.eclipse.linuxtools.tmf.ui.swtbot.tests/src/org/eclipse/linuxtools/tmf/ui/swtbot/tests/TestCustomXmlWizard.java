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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Custom XML wizard tests
 *
 * This test will help validate the CustomXmlParserInputWizardPage
 *
 * @author Matthew Khouzam
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TestCustomXmlWizard {

    private static final String EVENT = "event";
    private static final String TRACE = "trace";
    private static final String XML_TRACE1 = "<trace>\n\t<event time=\"100\" msg=\"hello\"/>\n\t<event time=\"200\" msg=\"world\"/></trace>";
    private static final String MANAGE_CUSTOM_PARSERS_SHELL_TITLE = "Manage Custom Parsers";
    private static final String PROJECT_NAME = "Test";
    private static final String EXPECTED_TEST_DEFINITION = "<Definition name=\"Test\">\n" +
            "<TimeStampOutputFormat>ss</TimeStampOutputFormat>\n" +
            "<InputElement name=\"trace\">\n" +
            "<InputElement logentry=\"true\" name=\"event\">\n" +
            "<InputData action=\"0\" format=\"\" name=\"Ignore\"/>\n" +
            "<Attribute name=\"msg\">\n" +
            "<InputData action=\"0\" name=\"msg\"/>\n" +
            "</Attribute>\n" +
            "<Attribute name=\"time\">\n" +
            "<InputData action=\"0\" name=\"time\"/>\n" +
            "</Attribute>\n" +
            "</InputElement>\n" +
            "</InputElement>\n" +
            "<OutputColumn name=\"msg\"/>\n" +
            "<OutputColumn name=\"time\"/>\n";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

    /** Test Class setup */
    @BeforeClass
    public static void init() {
        SWTBotUtil.failIfUIThread();
        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 200000; /* 20 second timeout */
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtil.closeView("welcome", fBot);

        SWTBotUtil.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        SWTBotUtil.waitForJobs();

    }

    /**
     * Test to create a custom txt trace and compare the xml
     *
     * @throws IOException
     *             the xml file is not accessible, this is bad
     * @throws FileNotFoundException
     *             the xml file wasn't written, this is bad
     */
    @Test
    public void testNew() throws FileNotFoundException, IOException {
        File xmlFile = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(".metadata/.plugins/org.eclipse.linuxtools.tmf.core/custom_xml_parsers.xml").toFile();
        SWTBotUtil.createProject(PROJECT_NAME);
        SWTBotView proejctExplorerBot = fBot.viewByTitle("Project Explorer");
        proejctExplorerBot.show();
        SWTBotTreeItem treeItem = proejctExplorerBot.bot().tree().getTreeItem(PROJECT_NAME);
        treeItem.select();
        treeItem.expand();
        SWTBotTreeItem treeNode = null;
        for (String node : treeItem.getNodes()) {
            if (node.startsWith("Trace")) {
                treeNode = treeItem.getNode(node);
                break;
            }

        }
        assertNotNull(treeNode);
        treeNode.contextMenu("Manage Custom Parsers...").click();
        fBot.shell(MANAGE_CUSTOM_PARSERS_SHELL_TITLE).setFocus();
        fBot.radio("XML").click();
        fBot.button("New...").click();
        fBot.textWithLabel("Log type:").setText(PROJECT_NAME);
        fBot.textWithLabel("Time Stamp format:").setText("ss");
        final SWTBotButton button[] = new SWTBotButton[6];

        fBot.styledText().setText(XML_TRACE1);
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                try {
                    Composite buttonBar = (Composite) ((Composite) ((Composite) ((Composite) ((Composite) ((Composite)
                            (fBot.shells()[6].widget.
                                    getChildren()[0])).
                                    getChildren()[0]).
                                    getChildren()[0]).
                                    getChildren()[1]).
                                    getChildren()[0]).
                                    getChildren()[1];
                    for (int i = 0; i < 6; i++) {
                        button[i] = new SWTBotButton((Button) buttonBar.getChildren()[i]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        button[3].click();

        fBot.tree().getTreeItem(TRACE).getNode(EVENT).select();
        fBot.checkBox("Log Entry").click();
        fBot.button("Next >").click();
        fBot.button("Finish").click();
        fBot.button("Close").click();

        String xmlPart = extractTestXml(xmlFile, PROJECT_NAME);
        assertEquals(EXPECTED_TEST_DEFINITION, xmlPart);
        fBot.button("Delete").click();
        fBot.button("Yes").click();
        fBot.button("Close").click();
        xmlPart = extractTestXml(xmlFile, PROJECT_NAME);
        assertEquals("", xmlPart);
    }

    private static String extractTestXml(File xmlFile, String definitionName) throws IOException, FileNotFoundException {
        StringBuilder xmlPart = new StringBuilder();
        boolean started = false;
        try (RandomAccessFile raf = new RandomAccessFile(xmlFile, "r");) {
            String s = raf.readLine();
            while (s != null) {
                if (s.equals("<Definition name=\"" + definitionName + "\">")) {
                    started = true;
                }
                if (started) {
                    if (s.equals("</Definition>")) {
                        break;
                    }
                    xmlPart.append(s);
                    xmlPart.append('\n');
                }
                s = raf.readLine();
            }
        }
        return xmlPart.toString();
    }
}
