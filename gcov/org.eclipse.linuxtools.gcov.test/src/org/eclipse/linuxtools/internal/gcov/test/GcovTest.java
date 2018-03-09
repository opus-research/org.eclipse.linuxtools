package org.eclipse.linuxtools.internal.gcov.test;

import static org.eclipse.swtbot.swt.finder.finders.ContextMenuHelper.contextMenu;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.waitForShell;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.dataviewers.actions.STExportToCSVAction;
import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.actions.AbstractOpenSourceFileAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.hamcrest.Matcher;
import org.osgi.framework.FrameworkUtil;

public abstract class GcovTest {
	private static final String PROJECT_EXPLORER = "Project Explorer";

	private static final class UnCheckTest implements ICondition {
		SWTBotCheckBox checkBox;

		public UnCheckTest(SWTBotCheckBox bot) {
			checkBox = bot;
		}

		@Override
		public boolean test() {
			return !checkBox.isChecked();
		}

		@Override
		public void init(SWTBot bot) {
		}

		@Override
		public String getFailureMessage() {
			return null;
		}
	}

	public static SWTWorkbenchBot init(String PROJECT_NAME, String PROJECT_TYPE)
			throws Exception {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();
		bot.captureScreenshot(PROJECT_NAME + ".beforeClass.1.jpg");
		try {
			bot.viewByTitle("Welcome").close();
			// hide Subclipse Usage stats popup if present/installed
			bot.shell("Subclipse Usage").activate();
			bot.button("Cancel").click();
		} catch (WidgetNotFoundException e) {
			// ignore
		}

		bot.perspectiveByLabel("C/C++").activate();
		for (SWTBotShell sh : bot.shells()) {
			if (sh.getText().startsWith("C/C++")) {
				sh.activate();
				bot.waitUntil(Conditions.shellIsActive(sh.getText()));
				break;
			}
		}

		bot.captureScreenshot(PROJECT_NAME + ".beforeClass.2.jpg");
		// Turn off automatic building by default
		SWTBotMenu windowsMenu = bot.menu("Window");
		windowsMenu.menu("Preferences").click();
		SWTBotShell shell = bot.shell("Preferences");
		shell.activate();
		bot.tree().expandNode("General").select("Workspace");
		SWTBotCheckBox buildAuto = bot.checkBox("Build automatically");
		if (buildAuto != null && buildAuto.isChecked())
			buildAuto.click();
		bot.waitUntil(new UnCheckTest(buildAuto));
		bot.button("Apply").click();
		bot.button("OK").click();

		System.out.println("Test: " + GcovTestC.class.getName());
		// repopulate project explorer
		GcovTest.createProject(bot, PROJECT_NAME, PROJECT_TYPE);
		GcovTest.populateProject(bot, PROJECT_NAME);
		GcovTest.compileProject(bot, PROJECT_NAME);
		return bot;
	}

	public static void cleanup(SWTWorkbenchBot bot) {
		// clear project explorer
		SWTBotTree treeBot = getProjectExplorerTree(bot);
		for (SWTBotTreeItem treeItem : treeBot.getAllItems()) {
			removeTreeItem(bot, treeItem);
		}
	}

	public static void createProject(SWTWorkbenchBot bot, String projectName,
			String projectType) {
		SWTBotMenu fileMenu = bot.menu("File");
		SWTBotMenu newMenu = fileMenu.menu("New");
		SWTBotMenu projectMenu = newMenu.menu(projectType);
		projectMenu.click();

		SWTBotShell shell = bot.shell(projectType);
		shell.activate();

		bot.tree().expandNode("Makefile project").select("Empty Project");
		bot.textWithLabel("Project name:").setText(projectName);
		bot.table().select("Linux GCC");

		bot.button("Next >").click();
		bot.button("Finish").click();
		bot.waitUntil(Conditions.shellCloses(shell));
	}

	public static void populateProject(SWTWorkbenchBot bot, String projectName)
			throws Exception {
		SWTBotTree treeBot = getProjectExplorerTree(bot);
		treeBot.setFocus();
		treeBot = treeBot.select(projectName);
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(projectName);
		InputStream is = FileLocator.openStream(
				FrameworkUtil.getBundle(GcovTest.class), new Path("resource/"
						+ projectName + "/content"), false);
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(is));
		String filename;
		while (null != (filename = lnr.readLine())) {
			final ProgressMonitor pm = new ProgressMonitor();
			final IFile ifile = project.getFile(filename);
			InputStream fis = FileLocator.openStream(FrameworkUtil
					.getBundle(GcovTest.class), new Path("resource/"
					+ projectName + "/" + filename), false);
			ifile.create(fis, true, pm);
			bot.waitUntil(new DefaultCondition() {

				@Override
				public boolean test() {
					return pm.isDone();
				}

				@Override
				public String getFailureMessage() {
					return ifile + " not yet created after 6000ms";
				}
			}, 6000);
		}
		lnr.close();
	}

	private static SWTBotTree getProjectExplorerTree(SWTWorkbenchBot bot) {
		SWTBot viewBot = bot.viewByTitle(PROJECT_EXPLORER).bot();
		viewBot.activeShell().activate();
		SWTBotTree treeBot = viewBot.tree();
		return treeBot;
	}

	public static void compileProject(SWTWorkbenchBot bot, String projectName) {
		SWTBotTree treeBot = getProjectExplorerTree(bot);
		treeBot.setFocus();
		treeBot = treeBot.select(projectName);
		treeBot.expandNode(projectName);
		bot.waitUntil(Conditions.treeHasRows(treeBot, 1));
		SWTBotMenu menu = bot.menu("Build Project");
		menu.click();
		bot.waitUntil(new JobsRunning(ResourcesPlugin.FAMILY_MANUAL_BUILD),
				30000);
	}

	private static void removeTreeItem(SWTWorkbenchBot bot,
			SWTBotTreeItem treeItem) {
		treeItem.contextMenu("Delete").click();
		SWTBotShell deleteShell = bot.shell("Delete Resources");
		deleteShell.activate();
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(deleteShell));
	}

	private static TreeSet<String> getGcovFiles(SWTWorkbenchBot bot,
			String projectName) throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(projectName);
		TreeSet<String> ret = new TreeSet<String>();
		for (IResource r : project.members()) {
			if (r.getType() == IResource.FILE && r.exists()) {
				if (r.getName().endsWith(".gcda")
						|| r.getName().endsWith(".gcno")) {
					ret.add(r.getFullPath().toOSString());
				}
			}
		}
		return ret;
	}

	private static void testGcovSummary(SWTWorkbenchBot bot,
			String projectName, String filename, String binName,
			boolean testProducedReference) throws Exception {
		IPath filePath = new Path(filename);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
		String binPath = file.getProject().getFile(binName).getLocation()
				.toOSString();

		SWTBot viewBot = bot.viewByTitle(PROJECT_EXPLORER).bot();
		SWTBotShell wbShell = bot.activeShell();

		String fileName = file.getName();
		SWTBotTree treeBot = viewBot.tree();
		doubleclickOnFile(projectName, fileName, treeBot);
		Matcher<Shell> withText = withText("Gcov - Open coverage results...");
		waitForShell(withText);
		SWTBotShell openCoverageResultsShell = bot
				.shell("Gcov - Open coverage results...");
		openCoverageResultsShell.activate();
		bot.textInGroup("Binary File", 0).setText(binPath);
		bot.button("OK").click();

		wbShell.activate();

		SWTBotView botView = bot.viewByTitle("gcov");
		// The following cannot be tested on 4.2 because the SWTBot
		// implementation of toolbarButton()
		// is broken there because it relies PartPane having a method getPane()
		// which is no longer true.
		botView.toolbarButton("Sort coverage per function").click();
		dumpCSV(bot, botView, projectName, "function", testProducedReference);
		botView.toolbarButton("Sort coverage per file").click();
		dumpCSV(bot, botView, projectName, "file", testProducedReference);
		botView.toolbarButton("Sort coverage per folder").click();
		dumpCSV(bot, botView, projectName, "folder", testProducedReference);
		botView.close();
	}

	private static void doubleclickOnFile(String projectName, String fileName,
			SWTBotTree treeBot) {
		treeBot.setFocus();
		SWTBotTreeItem expandedNode = treeBot.expandNode(projectName);
		SWTBotTreeItem item = expandedNode.getNode(fileName);
		item.doubleClick();
	}

	private static void testGcovFileDetails(SWTWorkbenchBot bot,
			String projectName, String filename, String binName)
			throws Exception {
		IPath filePath = new Path(filename);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
		String binPath = file.getProject().getFile(binName).getLocation()
				.toOSString();

		SWTBot viewBot = bot.viewByTitle(PROJECT_EXPLORER).bot();
		SWTBotShell wbShell = bot.activeShell();

		SWTBotTree treeBot = viewBot.tree();
		doubleclickOnFile(projectName, file.getName(), treeBot);

		Matcher<Shell> withText = withText("Gcov - Open coverage results...");
		waitForShell(withText);

		SWTBotShell shell = bot.shell("Gcov - Open coverage results...");
		shell.activate();
		bot.textInGroup("Binary File", 0).setText(binPath);
		SWTBotRadio button = bot.radioInGroup("Coverage result", 0);
		button.click();
		bot.button("OK").click();

		wbShell.activate();

		SWTBotEditor editor = bot
				.editorById(AbstractOpenSourceFileAction.EDITOR_ID);
		SWTBotEclipseEditor edt = editor.toTextEditor(); /*
														 * just to verify that
														 * the correct file was
														 * found
														 */
		edt.close();
	}

	private static void testGcovLaunchSummary(SWTWorkbenchBot bot,
			String projectName, String binName) throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(projectName);
		String binLocation = project.getFile(binName).getLocation()
				.toOSString();
		IPath binPath = new Path(binLocation);
		IFile binFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFile(binPath);

		SWTBot viewBot = bot.viewByTitle(PROJECT_EXPLORER).bot();
		SWTBotShell wbShell = bot.activeShell();

		SWTBotTree treeBot = viewBot.tree();
		treeBot.setFocus();
		// We need to select the binary, but in the tree, it may have additional
		// info appended to the
		// name such as [x86_64/le]. So, we look at all nodes of the project and
		// look for the one that
		// starts with our binary file name. We can then select the node.
		List<String> nodes = treeBot.expandNode(projectName).getNodes();
		String binNodeName = binFile.getName();
		for (String item : nodes) {
			if (item.startsWith(binFile.getName())) {
				binNodeName = item;
				break;
			}
		}
		treeBot.expandNode(projectName).select(binNodeName);
		String menuItem = "Profiling Tools";
		String subMenuItem = "1 Profile Code Coverage";
		click(contextMenu(treeBot, menuItem, subMenuItem));

		bot.button("OK").click();
		wbShell.activate();
		final boolean result[] = new boolean[1];
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage()
							.showView("org.eclipse.linuxtools.gcov.view");
					result[0] = true;
				} catch (PartInitException e) {
					result[0] = false;
				}
			}
		});
		if (!result[0]) {
			fail();
		}
		SWTBotView botView = bot.viewByTitle("gcov");

		botView.close();
	}

	private static void dumpCSV(SWTWorkbenchBot bot, SWTBotView botView,
			String projectName, String type, boolean testProducedReference) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(projectName);
		botView.toolbarButton("Export to CSV").click();
		SWTBotShell shell = bot.shell("Export to CSV");
		shell.activate();
		String s = project.getLocation() + "/" + type + "-dump.csv";
		new File(s).delete();
		bot.text().setText(s);
		bot.button("OK").click();
		bot.waitUntil(new JobsRunning(
				STExportToCSVAction.EXPORT_TO_CSV_JOB_FAMILY), 3000);
		if (testProducedReference) {
			String ref = STJunitUtils.getAbsolutePath(
					FrameworkUtil.getBundle(GcovTest.class).getSymbolicName(),
					"resource/" + projectName + "/" + type + ".csv");
			STJunitUtils.compareIgnoreEOL(project.getLocation() + "/" + type
					+ "-dump.csv", ref, false);
		}
	}

	public static void openGcovFileDetails(SWTWorkbenchBot bot,
			String projectName) throws Exception {
		openGcovFileDetails(bot, projectName, "a.out");
	}

	public static void openGcovSummary(SWTWorkbenchBot bot, String projectName,
			boolean testProducedReference) throws Exception {
		openGcovSummary(bot, projectName, "a.out", testProducedReference);
	}

	public static void openGcovSummary(SWTWorkbenchBot bot, String projectName,
			String binName, boolean testProducedReference) throws Exception {
		TreeSet<String> ts = getGcovFiles(bot, projectName);
		for (String string : ts) {
			testGcovSummary(bot, projectName, string, binName,
					testProducedReference);
		}
	}

	public static void openGcovFileDetails(SWTWorkbenchBot bot,
			String projectName, String binName) throws Exception {
		TreeSet<String> ts = getGcovFiles(bot, projectName);
		for (String string : ts) {
			testGcovFileDetails(bot, projectName, string, binName);
		}
	}

	public static void openGcovSummaryByLaunch(SWTWorkbenchBot bot,
			String projectName) throws Exception {
		testGcovLaunchSummary(bot, projectName, "a.out");
	}

	/**
	 * Click on the specified MenuItem.
	 * 
	 * @param menuItem
	 *            MenuItem item to click
	 */
	private static void click(final MenuItem menuItem) {
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
