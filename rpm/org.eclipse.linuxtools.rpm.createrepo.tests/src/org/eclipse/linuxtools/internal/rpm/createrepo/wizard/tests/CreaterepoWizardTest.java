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
package org.eclipse.linuxtools.internal.rpm.createrepo.wizard.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProjectNature;
import org.eclipse.linuxtools.rpm.createrepo.ICreaterepoConstants;
import org.eclipse.linuxtools.rpm.createrepo.IRepoFileConstants;
import org.eclipse.linuxtools.rpm.createrepo.tests.ICreaterepoTestConstants;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot tests for CreaterepoWizard.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CreaterepoWizardTest {

	private static final String PROJECT_NAME = "createrepo-test-project"; //$NON-NLS-1$
	private static final String REPO_ID = "createrepo-test-repo"; //$NON-NLS-1$
	private static final String REPO_FILE = REPO_ID.concat(".repo"); //$NON-NLS-1$

	private static final String REPO_WIZARD_NAME = "Test repository for createrepo plugin"; //$NON-NLS-1$
	private static final String REPO_WIZARD_URL = "http://www.example.com/test"; //$NON-NLS-1$
	private static final String REPO_FILE_CONTENTS =
			String.format("[%s]%s=%s%s=%s", REPO_ID, IRepoFileConstants.NAME,  //$NON-NLS-1$
			REPO_WIZARD_NAME, IRepoFileConstants.BASE_URL, REPO_WIZARD_URL);

	private static IWorkspaceRoot root;
	private static NullProgressMonitor monitor;
	private IProject project;

	/**
	 * Setup the bot, monitor and workspace root.
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		root = ResourcesPlugin.getWorkspace().getRoot();
		monitor = new NullProgressMonitor();
		SWTWorkbenchBot bot = new SWTWorkbenchBot();
		try {
			bot.viewByTitle("Welcome").close(); //$NON-NLS-1$
			// hide Subclipse Usage stats popup if present/installed
			bot.shell("Subclipse Usage").activate(); //$NON-NLS-1$
			bot.button("Cancel").click(); //$NON-NLS-1$
		} catch (WidgetNotFoundException e) {
			// ignore
		}
		bot.sleep(1000);
	}

	/**
	 * Delete the project and its contents for each test itereation.
	 *
	 * @throws CoreException
	 */
	@After
	public void tearDown() throws CoreException {
		if (project != null && project.exists()) {
			project.delete(true, monitor);
		}
	}

	/**
	 * Go through the project creation wizard process of creating a new
	 * createrepo project.
	 *
	 * @throws CoreException
	 * @throws IOException
	 */
	@Test
	public void testCreaterepoWizardProjectCreation() throws CoreException, IOException {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();
		bot.activeShell().activate();
		// go through the process of creating a new createrepo project
		SWTBotMenu fileMenu = bot.menu("File"); //$NON-NLS-1$
		assertNotNull(fileMenu);
		fileMenu.menu("New").menu("Other...").click();
		SWTBotShell shell = bot.shell("New"); //$NON-NLS-1$
		shell.activate();

		SWTBotTreeItem treeItem = bot.tree().expandNode("Createrepo"); //$NON-NLS-1$
		assertNotNull(treeItem);
		treeItem.select("Createrepo Wizard");

		SWTBotButton button = bot.button("Next >");
		assertNotNull(button);
		button.click();

		SWTBotText text = bot.textWithLabel("Project name:");
		assertNotNull(text);
		text.setText(PROJECT_NAME);

		button = bot.button("Next >");
		assertNotNull(button);
		button.click();

		text = bot.textWithLabel(Messages.CreaterepoNewWizardPageTwo_labelID);
		assertNotNull(text);
		text.setText(REPO_ID);

		text = bot.textWithLabel(Messages.CreaterepoNewWizardPageTwo_labelName);
		assertNotNull(text);
		text.setText(REPO_WIZARD_NAME);

		text = bot.textWithLabel(Messages.CreaterepoNewWizardPageTwo_labelURL);
		assertNotNull(text);
		text.setText(REPO_WIZARD_URL);

		button = bot.button(ICreaterepoTestConstants.FINISH_BUTTON);
		assertNotNull(button);
		button.click();

		// verify that project has been initialized properly
		project = root.getProject(PROJECT_NAME);
		assertTrue(project.exists());
		assertTrue(project.hasNature(CreaterepoProjectNature.CREATEREPO_NATURE_ID));
		// 3 = .project + content folder + .repo file
		assertEquals(3, project.members().length);

		// contains the content folder and repo file
		assertTrue(project.findMember(ICreaterepoConstants.CONTENT_FOLDER).exists());
		assertTrue(project.findMember(REPO_FILE).exists());

		// content folder has nothing in it
		IFolder contentFolder = (IFolder) project.findMember(ICreaterepoConstants.CONTENT_FOLDER);
		assertEquals(0, contentFolder.members().length);

		// get the created .repo file contents
		IFile repoFile = (IFile) project.findMember(REPO_FILE);
		// repo file should not be empty
		assertNotEquals(repoFile.getContents().available(), 0);
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(repoFile.getContents()));
		String line;
		while ((line = br.readLine()) != null) {
			// disregards newline
			sb.append(line);
		}
		assertEquals(REPO_FILE_CONTENTS, sb.toString());
	}

}
