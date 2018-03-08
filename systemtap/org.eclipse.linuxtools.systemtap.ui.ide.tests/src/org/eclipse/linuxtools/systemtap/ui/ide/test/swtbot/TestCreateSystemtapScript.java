/*******************************************************************************
 * Copyright (c) 2013 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial implementation
 *******************************************************************************/


package org.eclipse.linuxtools.systemtap.ui.ide.test.swtbot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.ContextMenuHelper;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestCreateSystemtapScript {

	static SWTWorkbenchBot bot;
	static boolean stapInstalled;

	private static final String SYSTEMTAP_PROJECT_NAME = "SystemtapTest";

	private static class ShellIsClosed extends DefaultCondition {

		private SWTBotShell shell;

		public ShellIsClosed(SWTBotShell shell) {
			super();
			this.shell = shell;
		}

		@Override
		public boolean test() {
			return !shell.isOpen();
		}

		@Override
		public String getFailureMessage() {
				return "Timed out waiting for " + shell + " to close."; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static class NodeAvaiable extends DefaultCondition {

		private String node;
		private SWTBotTreeItem parent;

		NodeAvaiable(SWTBotTreeItem parent, String node){
			this.node = node;
			this.parent = parent;
		}

		@Override
		public boolean test() {
			return this.parent.getNodes().contains(node);
		}

		@Override
		public String getFailureMessage() {
			return "Timed out waiting for " + node; //$NON-NLS-1$
		}
	}

	private static class StapHasExited extends DefaultCondition{

		@Override
		public boolean test() {
			SWTBotView console = TestCreateSystemtapScript.bot.viewById("org.eclipse.ui.console.ConsoleView");
			console.setFocus();
			return (!console.toolbarButton("Stop Script").isEnabled());
		}

		@Override
		public String getFailureMessage() {
			return "Timed out waiting for stap to exit";
		}
	}

	@BeforeClass
	public static void beforeClass() {
		bot = new SWTWorkbenchBot();
		stapInstalled = true;

		// Dismiss "Systemtap not installed" dialog(s) if present.
		try {
			SWTBotShell shell = bot.shell("Cannot Run Systemtap").activate();
			stapInstalled = false;
			shell.close();

			shell = bot.shell("Cannot Run Systemtap").activate();
			shell.close();
		} catch (WidgetNotFoundException e) {
			//ignore
		}

		try {
			bot.viewByTitle("Welcome").close();
			// hide Subclipse Usage stats popup if present/installed
			bot.shell("Subclipse Usage").activate();
			bot.button("Cancel").click();
		} catch (WidgetNotFoundException e) {
			//ignore
		}

		// Set SystemTap IDE perspective.
		bot.perspectiveByLabel("SystemTap IDE").activate();
		bot.sleep(500);
		for (SWTBotShell sh : bot.shells()) {
			if (sh.getText().startsWith("SystemTap IDE")) {
				sh.activate();
				bot.sleep(500);
				break;
			}
		}

		// Create a Systemtap project.
		SWTBotMenu fileMenu = bot.menu("File");
		SWTBotMenu newMenu = fileMenu.menu("New");
		SWTBotMenu projectMenu = newMenu.menu("Project...");
		projectMenu.click();

		SWTBotShell shell = bot.shell("New Project");
		shell.activate();

		SWTBotTreeItem node = bot.tree().expandNode("General").select("Project");
		assertNotNull(node);

		bot.button("Next >").click();

		bot.textWithLabel("Project name:").setText(SYSTEMTAP_PROJECT_NAME);
		bot.button("Finish").click();
		bot.waitUntil(new ShellIsClosed(shell));
	}

	public static void createScript(SWTWorkbenchBot bot, String scriptName) {

		SWTBotMenu fileMenu = bot.menu("File");
		SWTBotMenu newMenu = fileMenu.menu("New");
		SWTBotMenu projectMenu = newMenu.menu("Other...");
		projectMenu.click();

		SWTBotShell shell = bot.shell("New");
		shell.activate();

		SWTBotTreeItem node = bot.tree().expandNode("Systemtap");
		assertNotNull(node);
		bot.waitUntil(new NodeAvaiable(node, "Systemtap Script"));
		node.select("Systemtap Script");

		bot.button("Next >").click();

		SWTBotText text = bot.textWithLabel("Script Name:").setText(scriptName);
		assertEquals(scriptName, text.getText());

		text = bot.textWithLabel("Project:").setText(SYSTEMTAP_PROJECT_NAME);
		assertEquals(SYSTEMTAP_PROJECT_NAME, text.getText());

		bot.button("Finish").click();
		bot.waitUntil(new ShellIsClosed(shell));

		assertEquals(scriptName, bot.activeEditor().getTitle());
	}

	@Test
	public void testCreateScript(){
		String scriptName = "testScript.stp";
		createScript(bot, scriptName);

		// Write a script
		SWTBotEclipseEditor editor = bot.editorByTitle(scriptName).toTextEditor();
		editor.typeText(0, editor.getText().length(), "\nprobe begin{log(\"began");
		editor.typeText(0, editor.getText().length() - 1, "); exit(");
		editor.typeText(0, editor.getText().length(), "}");
		editor.save();

		// Focus on project explorer view.
		bot.viewByTitle("Project Explorer").setFocus();
		bot.activeShell();
		SWTBotTree treeBot = bot.tree();
		treeBot.setFocus();
		SWTBotTreeItem node = treeBot.expandNode((SYSTEMTAP_PROJECT_NAME));
		bot.waitUntil(new NodeAvaiable(node, scriptName));

		treeBot.expandNode(SYSTEMTAP_PROJECT_NAME).expand().select(scriptName);

		MenuItem menu = ContextMenuHelper.contextMenu(treeBot, "Run As", "Run Configurations...");
		click(menu);

		SWTBotShell shell = bot.shell("Run Configurations");

		SWTBotTree runConfigurationsTree = bot.tree();
		runConfigurationsTree.select("SystemTap").contextMenu("New").click();

		if (stapInstalled) {
			bot.button("Run").click();
			bot.waitUntil(new ShellIsClosed(shell));

			SWTBotView console = bot.viewById("org.eclipse.ui.console.ConsoleView");
			console.setFocus();
			assertTrue(console.bot().label().getText().contains(scriptName));
			bot.waitUntil(new StapHasExited(), 10000);
		} else {
			bot.button("Close").click();
			bot.waitUntil(new ShellIsClosed(shell));
		}
	}

	@Test
	public void testMissingColumns(){
		String scriptName = "missingColumns.stp";
		createScript(bot, scriptName);

		// Focus on project explorer view.
		bot.viewByTitle("Project Explorer").setFocus();
		bot.activeShell();
		SWTBotTree treeBot = bot.tree();
		treeBot.setFocus();
		SWTBotTreeItem node = treeBot.expandNode((SYSTEMTAP_PROJECT_NAME));
		bot.waitUntil(new NodeAvaiable(node, scriptName));

		treeBot.expandNode(SYSTEMTAP_PROJECT_NAME).expand().select(scriptName);

		MenuItem menu = ContextMenuHelper.contextMenu(treeBot, "Run As", "Run Configurations...");
		click(menu);

		SWTBotShell shell = bot.shell("Run Configurations");
		shell.setFocus();

		SWTBotTree runConfigurationsTree = bot.tree();
		runConfigurationsTree.select("SystemTap").contextMenu("New").click();

		// Select the "Graphing" tab.
		SWTBotCTabItem tab = bot.cTabItem(Messages.SystemTapScriptGraphOptionsTab_7);
		tab.activate();

		// Enable output graphing.
		bot.checkBox(Messages.SystemTapScriptGraphOptionsTab_2).click();

		// As soon as the Graphing tab is entered, no regular expression exists & nothing can be run.
		SWTBotCombo combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
		assertEquals("", combo.getText());
		assertTrue(!bot.button("Run").isEnabled());
		assertTrue(!bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton).isEnabled());
		combo.setText("(1)(2)");
		assertEquals("(1)(2)", combo.getText());
		assertTrue(bot.button("Run").isEnabled());
		assertTrue(bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton).isEnabled());

		bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton).click();
		setupGraph("Graph");

		shell.setFocus();
		assertTrue(bot.button("Run").isEnabled());

		// Removing groups from the regex disables graphs that rely on those groups.
		combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
		combo.setText("(1)");
		assertTrue(!bot.button("Run").isEnabled());
		combo.setText("(1)(2)(3)");
		assertTrue(bot.button("Run").isEnabled());

		shell.setFocus();
		bot.button("Apply").click();
		bot.button("Close").click();
		bot.waitUntil(new ShellIsClosed(shell));
	}

	@Test
	public void testGraphScript(){
		String scriptName = "testGraph.stp";
		createScript(bot, scriptName);

		// Write a script
		SWTBotEclipseEditor editor = bot.editorByTitle(scriptName).toTextEditor();
		editor.setText("#!/usr/bin/env stap"
				+ "\nglobal i,j,k"
				+ "\nprobe begin{i=0;j=0;k=0}"
				+ "\nprobe timer.ms(100){printf(\"Value:%d %d\\n\",i,j);i++;j+=2}"
				+ "\nprobe timer.ms(250){printf(\"Other:%d %d\\n\",i,k);k++}");
		editor.save();

		String val0 = "i";
		String val1 = "j";
		String val2 = "k";

		// Focus on project explorer view.
		bot.viewByTitle("Project Explorer").setFocus();
		bot.activeShell();
		SWTBotTree treeBot = bot.tree();
		treeBot.setFocus();
		SWTBotTreeItem node = treeBot.expandNode((SYSTEMTAP_PROJECT_NAME));
		bot.waitUntil(new NodeAvaiable(node, scriptName));

		treeBot.expandNode(SYSTEMTAP_PROJECT_NAME).expand().select(scriptName);

		MenuItem menu = ContextMenuHelper.contextMenu(treeBot, "Run As", "Run Configurations...");
		click(menu);

		SWTBotShell shell = bot.shell("Run Configurations");
		shell.setFocus();

		SWTBotTree runConfigurationsTree = bot.tree();
		runConfigurationsTree.select("SystemTap").contextMenu("New").click();

		// Select the "Graphing" tab.
		SWTBotCTabItem tab = bot.cTabItem(Messages.SystemTapScriptGraphOptionsTab_7);
		tab.activate();

		// Enable output graphing.
		bot.checkBox(Messages.SystemTapScriptGraphOptionsTab_2).click();
		SWTBotCombo combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
		combo.setText("Value:(\\d+) (\\d+)");
		assertEquals("Value:(\\d+) (\\d+)", combo.getText());

		SWTBotText text = bot.textWithLabel(Messages.SystemTapScriptGraphOptionsTab_sampleOutputLabel);
		text.setText("Value:1 2");
		assertEquals("Value:1 2", text.getText());

		text = bot.text(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_defaultColumnTitleBase, "0"));
		text.setText(val0);
		assertEquals(val0, text.getText());
		text = bot.text(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_defaultColumnTitleBase, "1"));
		text.setText(val1);
		assertEquals(val1, text.getText());

		// Add a graph.
		bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton).click();
		setupGraph("Values");

		// Make a second regex, and a graph for it.
		shell.setFocus();
		combo = bot.comboBoxWithLabel(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
		assertEquals(2, combo.itemCount());
		combo.setSelection(combo.selectionIndex() + 1);
		assertEquals(3, combo.itemCount());
		assertEquals("", combo.getText());
		combo.setText("Other:(\\d+) (\\d+)");
		assertEquals("Other:(\\d+) (\\d+)", combo.getText());

		text = bot.text(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_defaultColumnTitleBase, "0"));
		text.setText(val0);
		assertEquals(val0, text.getText());
		text = bot.text(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_defaultColumnTitleBase, "1"));
		text.setText(val2);
		assertEquals(val2, text.getText());

		text = bot.textWithLabel(Messages.SystemTapScriptGraphOptionsTab_sampleOutputLabel);
		assertEquals("", text.getText());

		bot.button(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton).click();
		setupGraph("Others");

		shell.setFocus();
		// If Systemtap is not installed, don't test graph output. Otherwise, do.
		if (!stapInstalled) {
			bot.button("Apply").click();
			bot.button("Close").click();
			bot.waitUntil(new ShellIsClosed(shell));
			return;
		}

		bot.button("Run").click();
		bot.waitUntil(new ShellIsClosed(shell));
		bot.sleep(2500); // Let the script run for a moment
		SWTBotView console = bot.viewById("org.eclipse.ui.console.ConsoleView");
		console.setFocus();
		console.toolbarButton("Stop Script").click(); // Stop the script manually
		bot.waitUntil(new StapHasExited(), 10000);

		bot.sleep(1000); // Give time for the table to be fully constructed
		SWTBotEditor graphDisplay = bot.activeEditor();
		graphDisplay.setFocus();
		graphDisplay.bot().cTabItem(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_regexBaseTitle, "0")).activate();
		graphDisplay.bot().cTabItem("Data View").activate();
		SWTBotTable dataTable = bot.table(0);
		List<String> colNames = dataTable.columns();
		assertEquals(3, colNames.size());
		assertEquals(val0, colNames.get(1));
		assertEquals(val1, colNames.get(2));
		assertEquals("3", dataTable.cell(3, 1));
		assertEquals("6", dataTable.cell(3, 2));

		graphDisplay.bot().cTabItem(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_regexBaseTitle, "1")).activate();
		graphDisplay.bot().cTabItem("Data View").activate();
		dataTable = bot.table(0);
		colNames = dataTable.columns();
		assertEquals(3, colNames.size());
		assertEquals(val0, colNames.get(1));
		assertEquals(val2, colNames.get(2));
		assertEquals("10", dataTable.cell(3, 1));
		assertEquals("3", dataTable.cell(3, 2));
	}

	private void setupGraph(String title) {
		SWTBotShell shell = bot.shell("Create Graph");
		shell.setFocus();

		SWTBotText text = bot.textWithLabel("Title:");
		text.setText(title);
		assertEquals(title, text.getText());

		SWTBotCombo combo_x = bot.comboBoxWithLabel("X Series:");
		assertEquals(3, combo_x.itemCount()); // X Series includes "Row ID" as a selection
		SWTBotCombo combo_y0 = bot.comboBoxWithLabel("Y Series 0:");
		assertEquals(2, combo_y0.itemCount()); // Y Series 0 only includes series entries
		combo_y0.setSelection(0);
		SWTBotCombo combo_y1 = bot.comboBoxWithLabel("Y Series 1:");
		assertEquals(3, combo_y1.itemCount()); // Y Series (i>0) has extra "NA" option as first entry
		combo_y1.setSelection(1);
		assertTrue(!bot.button("Finish").isEnabled()); // Don't allow duplicate selections
		combo_y1.setSelection(2);
		bot.button("Finish").click();

		bot.waitUntil(new ShellIsClosed(shell));
	}

	public static void click(final MenuItem menuItem) {
        final Event event = new Event();
        event.time = (int) System.currentTimeMillis();
        event.widget = menuItem;
        event.display = menuItem.getDisplay();
        event.type = SWT.Selection;

        UIThreadRunnable.asyncExec(menuItem.getDisplay(), new VoidResult() {
                @Override
                public void run() {
                        menuItem.notifyListeners(SWT.Selection, event);
                }
        });
	}
}
