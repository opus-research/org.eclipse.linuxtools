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

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.ContextMenuHelper;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestCreateSystemtapScript {

	static SWTWorkbenchBot bot;

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
		public boolean test() throws IOException, InterruptedException {
			Process process = RuntimeProcessFactory.getFactory().exec(
					new String[] {"pgrep","stap"}, null); //$NON-NLS-1$
			process.waitFor();
			return (process.exitValue() != 0);
		}

		@Override
		public String getFailureMessage() {
			return "Timed out waiting for stap to exit";
		}
	}

	@BeforeClass
	public static void beforeClass() {
		bot = new SWTWorkbenchBot();

		// Dismiss "Systemtap not installed" dialog(s) if present.
		try {
			SWTBotShell shell = bot.shell("Cannot Run Systemtap").activate();
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
		assert(text.getText().equals(scriptName));
		bot.button("Browse").click();

		SWTBotTree tree = bot.tree().select(SYSTEMTAP_PROJECT_NAME);
		assertNotNull(tree);

		bot.button("OK").click();
		bot.button("Finish").click();
		bot.waitUntil(new ShellIsClosed(shell));

		assert(bot.activeEditor().getTitle().equals(scriptName));
	}

	@Test
	public void testCreateScript(){
		String scriptName = "testScript.stp";
		createScript(bot, scriptName);

		// Write a script
		SWTBotEclipseEditor editor = bot.editorByTitle(scriptName).toTextEditor();
		editor.typeText(0, editor.getText().length(), "\nprobe begin{log(\"began");
		editor.typeText(0, editor.getText().length() - 1, "; exit(");
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

		bot.button("Run").click();
		bot.waitUntil(new ShellIsClosed(shell));

		SWTBotView console = bot.viewByTitle("Console");
		console.setFocus();
		assert(console.bot().label().getText().contains(scriptName));
		bot.waitUntil(new StapHasExited(), 10000);
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
