/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.form.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.internal.rpm.createrepo.tree.CreaterepoTreeCategory;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoPreferenceConstants;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.rpm.createrepo.ICreaterepoConstants;
import org.eclipse.linuxtools.rpm.createrepo.tests.ICreaterepoTestConstants;
import org.eclipse.linuxtools.rpm.createrepo.tests.TestCreaterepoProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotMultiPageEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.prefs.BackingStoreException;

/**
 * SWTBot tests for MetadataPage.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CreaterepoMetadataPageTest {

	private static TestCreaterepoProject testProject;
	private CreaterepoProject project;
	private static SWTWorkbenchBot bot;
	private SWTBot metadataPageBot;

	/**
	 * Initialize the test project. Will close the welcome view.
	 *
	 * @throws CoreException
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws CoreException {
		testProject = new TestCreaterepoProject();
		bot = new SWTWorkbenchBot();
		try {
			bot.shell(ICreaterepoTestConstants.MAIN_SHELL).activate();
		} catch (WidgetNotFoundException e) {
			// cannot activate main shell, continue anyways
		}
	}

	/**
	 * Delete the project when tests are done.
	 *
	 * @throws CoreException
	 */
	@AfterClass
	public static void tearDownAfterClass() throws CoreException {
		testProject.dispose();
	}

	/**
	 * Get the CreaterepoProject at the beginning of each test, as
	 * well as import some test RPMs.
	 *
	 * @throws CoreException
	 * @throws IOException
	 */
	@Before
	public void setUp() throws CoreException, IOException {
		project = testProject.getCreaterepoProject();
		initializeMetadataPage();
	}

	/**
	 * Clear the project preferences.
	 *
	 * @throws BackingStoreException
	 */
	@After
	public void tearDown() throws BackingStoreException {
		IEclipsePreferences pref = project.getEclipsePreferences();
		pref.clear();
		pref.flush();
	}

	/**
	 * Test if tags are adding properly. Tags should be unique per category.
	 */
	@Test
	public void testAddTags() {
		// run in UI thread because accessing the tree in the metadata page
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				String tagName = "testTag"; //$NON-NLS-1$
				String newTagName = "newUniqueTag"; //$NON-NLS-1$
				String prefValueToBe = ""; //$NON-NLS-1$
				metadataPageBot.textWithLabel(Messages.MetadataPage_labelTags).setText(tagName);
				Tree tree = metadataPageBot.widget(WidgetMatcherFactory.widgetOfType(Tree.class));
				// 0 = distro, 1 = content, 2 = repo
				int category = 0;
				tree.select(tree.getItem(category));
				metadataPageBot.button(Messages.MetadataPage_buttonAddTag).click();
				CreaterepoTreeCategory test = (CreaterepoTreeCategory) tree.getItem(category).getData();
				assertEquals(1, test.getTags().size());
				// it should be distro = {testTag} now
				prefValueToBe = tagName;
				assertTrue(isPreferencesCorrect(CreaterepoPreferenceConstants.PREF_DISTRO_TAG, prefValueToBe));
				// try adding the same tag to the same category again, it should not work
				tree.select(tree.getItem(category));
				metadataPageBot.textWithLabel(Messages.MetadataPage_labelTags).setText(tagName);
				metadataPageBot.button(Messages.MetadataPage_buttonAddTag).click();
				assertEquals(1, test.getTags().size());
				// it should still be distro = {testTag}
				prefValueToBe = tagName;
				assertTrue(isPreferencesCorrect(CreaterepoPreferenceConstants.PREF_DISTRO_TAG, prefValueToBe));
				// try adding a new unique tag to the same category, it should work
				tree.select(tree.getItem(category));
				metadataPageBot.textWithLabel(Messages.MetadataPage_labelTags).setText(newTagName);
				metadataPageBot.button(Messages.MetadataPage_buttonAddTag).click();
				assertEquals(2, test.getTags().size());
				// it should be distro = {testTag,  newUniqueTag} now
				prefValueToBe = tagName + ICreaterepoConstants.DELIMITER + newTagName;
				assertTrue(isPreferencesCorrect(CreaterepoPreferenceConstants.PREF_DISTRO_TAG, prefValueToBe));
			}
		});
	}

	/**
	 * Test if tags can be edited correctly.
	 */
	@Test
	public void testEditTags() {
		// run in UI thread because accessing the tree in the metadata page
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				String tagName = "testTag"; //$NON-NLS-1$
				String tagName2 = "testTag2"; //$NON-NLS-1$
				String newTagName = "renameTag"; //$NON-NLS-1$
				String prefValueToBe = ""; //$NON-NLS-1$
				Tree tree = metadataPageBot.widget(WidgetMatcherFactory.widgetOfType(Tree.class));
				// 0 = distro, 1 = content, 2 = repo
				int category = 1;
				// select the category and the test tags to it
				tree.select(tree.getItem(category));
				metadataPageBot.textWithLabel(Messages.MetadataPage_labelTags).setText(tagName);
				metadataPageBot.button(Messages.MetadataPage_buttonAddTag).click();
				metadataPageBot.textWithLabel(Messages.MetadataPage_labelTags).setText(tagName2);
				metadataPageBot.button(Messages.MetadataPage_buttonAddTag).click();
				CreaterepoTreeCategory test = (CreaterepoTreeCategory) tree.getItem(category).getData();
				assertEquals(2, test.getTags().size());
				// it should be content = {testTag1, testTag2} now
				prefValueToBe = tagName + ICreaterepoConstants.DELIMITER + tagName2;
				assertTrue(isPreferencesCorrect(CreaterepoPreferenceConstants.PREF_CONTENT_TAG, prefValueToBe));
				// select the first test tag that was created and edit it with a new unique tag
				tree.select(tree.getItem(category).getItem(0));
				metadataPageBot.textWithLabel(Messages.MetadataPage_labelTags).setText(newTagName);
				metadataPageBot.button(Messages.MetadataPage_buttonEditTag).click();
				assertEquals(2, test.getTags().size());
				assertEquals(newTagName, test.getTags().get(0));
				// it should be content = {renameTag, testTag2} now
				prefValueToBe = newTagName + ICreaterepoConstants.DELIMITER + tagName2;
				assertTrue(isPreferencesCorrect(CreaterepoPreferenceConstants.PREF_CONTENT_TAG, prefValueToBe));
				// select the second test tag and try to rename it as the same name as the first tag, this should not happen
				tree.select(tree.getItem(category).getItem(1));
				metadataPageBot.textWithLabel(Messages.MetadataPage_labelTags).setText(newTagName);
				metadataPageBot.button(Messages.MetadataPage_buttonEditTag).click();
				assertEquals(2, test.getTags().size());
				assertNotEquals(newTagName, test.getTags().get(1));
				// it should still be content = {renameTag, testTag2}
				prefValueToBe = newTagName + ICreaterepoConstants.DELIMITER + tagName2;
				assertTrue(isPreferencesCorrect(CreaterepoPreferenceConstants.PREF_CONTENT_TAG, prefValueToBe));
			}
		});
	}

	/**
	 * Test if tags can be removed.
	 */
	@Test
	public void testRemoveTags() {
		// run in UI thread because accessing the tree in the metadata page
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				String tagName = "testTag"; //$NON-NLS-1$
				String prefValueToBe = ""; //$NON-NLS-1$
				Tree tree = metadataPageBot.widget(WidgetMatcherFactory.widgetOfType(Tree.class));
				// 0 = distro, 1 = content, 2 = repo
				int category = 2;
				// select the category and the test tags to it
				tree.select(tree.getItem(category));
				metadataPageBot.textWithLabel(Messages.MetadataPage_labelTags).setText(tagName);
				metadataPageBot.button(Messages.MetadataPage_buttonAddTag).click();
				CreaterepoTreeCategory test = (CreaterepoTreeCategory) tree.getItem(category).getData();
				assertEquals(1, test.getTags().size());
				// it should be repo = {testTag} now
				prefValueToBe = tagName;
				assertTrue(isPreferencesCorrect(CreaterepoPreferenceConstants.PREF_REPO_TAG, prefValueToBe));
				// select the first item and delete it
				tree.select(tree.getItem(category).getItem(0));
				metadataPageBot.button(Messages.MetadataPage_buttonRemoveTag).click();
				assertEquals(0, test.getTags().size());
				// it should be repo = {} now
				prefValueToBe = ""; //$NON-NLS-1$
				assertTrue(isPreferencesCorrect(CreaterepoPreferenceConstants.PREF_REPO_TAG, prefValueToBe));
				// try to remove the category
				tree.select(tree.getItem(category));
				metadataPageBot.button(Messages.MetadataPage_buttonRemoveTag).click();
				// it should still be there
				assertNotNull(tree.getItem(category));
			}
		});
	}

	/**
	 * Get the current project preference for the category tag and check if the values
	 * stored are the same as the values passed in.
	 *
	 * @param category The preference category to check.
	 * @param value The value to check.
	 * @return True if the value is same as what is stored, false otherwise.
	 */
	private boolean isPreferencesCorrect(String category, String value) {
		IEclipsePreferences pref = project.getEclipsePreferences();
		IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		String actual = pref.get(category, prefStore.getDefaultString(category));
		return actual.equals(value);
	}

	/**
	 * Helper method to help setup the test by opening the .repo file.
	 */
	private void initializeMetadataPage() {
		// open the package explorer view
		bot.menu(ICreaterepoTestConstants.WINDOW).menu(ICreaterepoTestConstants.SHOW_VIEW).menu(ICreaterepoTestConstants.OTHER).click();
		SWTBotShell shell = bot.shell(ICreaterepoTestConstants.SHOW_VIEW);
		shell.activate();
		bot.tree().expandNode(ICreaterepoTestConstants.JAVA_NODE).select(ICreaterepoTestConstants.PACKAGE_EXPLORER);
		bot.button(ICreaterepoTestConstants.OK_BUTTON).click();
		SWTBotView view = bot.viewByTitle(ICreaterepoTestConstants.PACKAGE_EXPLORER);
		view.show();
		// select the repo file from the package explorer and open it
		Composite packageExplorer = (Composite)view.getWidget();
		Tree swtTree = bot.widget(WidgetMatcherFactory.widgetOfType(Tree.class), packageExplorer);
		SWTBotTree botTree = new SWTBotTree(swtTree);
		botTree.expandNode(TestCreaterepoProject.PROJECT_NAME).select(TestCreaterepoProject.REPO_NAME);
		bot.menu(ICreaterepoTestConstants.OPEN).click();
		// get a handle on the multipage editor that was opened
		SWTBotMultiPageEditor editor = bot.multipageEditorByTitle(TestCreaterepoProject.REPO_NAME);
		editor.show();
		// 3 = repository form page, metadata form page, repo file
		assertEquals(3, editor.getPageCount());
		// activate the pages to make sure they exist and work
		editor.activatePage(Messages.MetadataPage_title);
		// make sure correct page is active
		assertEquals(Messages.MetadataPage_title, editor.getActivePageTitle());
		metadataPageBot = editor.bot();
	}

}
