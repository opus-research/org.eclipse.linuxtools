package org.eclipse.linuxtools.systemtap.ui.ide.test.swtbot;

import java.io.File;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestCreateSystemtapScript {

	static SWTWorkbenchBot bot;

	@BeforeClass
	public static void beforeClass() {
		bot = new SWTWorkbenchBot();
	}

	public static void createScript(SWTWorkbenchBot bot, String scriptName) {
		try {
			bot.viewByTitle("Welcome").close();
			// hide Subclipse Usage stats popup if present/installed
			bot.shell("Subclipse Usage").activate();
			bot.button("Cancel").click();
		} catch (WidgetNotFoundException e) {
			//ignore
		}

		String testFilePath = "/tmp/systemtap_test/" + scriptName;
		File testFile = new File(testFilePath);
		testFile.delete();

		SWTBotMenu fileMenu = bot.menu("File");
		SWTBotMenu newMenu = fileMenu.menu("New");
		SWTBotMenu projectMenu = newMenu.menu("Other...");
		projectMenu.click();

		SWTBotShell shell = bot.shell("New");
		shell.activate();

		bot.tree().expandNode("Systemtap").select("Systemtap Script");
		bot.button("Next >").click();

		bot.textWithLabel("Script Name:").setText(scriptName);
		bot.textWithLabel("Directory:").setText("/tmp/systemtap_test");
		bot.button("Finish").click();

		assert(bot.activeEditor().getTitle().equals(scriptName));

		bot.menu("File").menu("Exit").click();
		testFile.delete();
	}

	@Test
	public void testCreateScript(){
		createScript(bot, "testScript.stp");
	}
}