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
package org.eclipse.linuxtools.rpm.createrepo.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProjectCreator;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProjectNature;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for project creation with CreaterepoProjectCreatorTest class.
 * This checks if the project creator is working, but does not check for
 * initializing of .repo file contents and the content folder. These are done
 * via the wizard (SWTBot test should handle this).
 */
public class CreaterepoProjectCreatorTest {

	private static final String PROJECT_NAME = "createrepo-test-project"; //$NON-NLS-1$
	private static final String REPO_NAME = "createrepo-test-repo.repo"; //$NON-NLS-1$

	private static IWorkspaceRoot root;
	private static NullProgressMonitor monitor;
	private IProject project;

	/**
	 * Initialize workspace root and progress monitor.
	 *
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		root = ResourcesPlugin.getWorkspace().getRoot();
		monitor = new NullProgressMonitor();
	}

	/**
	 * Create the project using CreaterepoProjectCreator.
	 *
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		if (project == null || !project.exists()) {
			project = CreaterepoProjectCreator.create(PROJECT_NAME, root.getLocation(), REPO_NAME, monitor);
		}
	}

	/**
	 * Forcefully delete the project if it exists.
	 *
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		if (project != null && project.exists()) {
			project.delete(true, monitor);
		}
	}

	/**
	 * Test to see if the project has been properly created. Content folder
	 * should not appear due to CreaterepoWizard handling its creation. Repo
	 * file should be empty for the same reason.
	 *
	 * @throws CoreException
	 * @throws IOException
	 */
	@Test
	public void testProjectContents() throws CoreException, IOException {
		assertTrue(project.exists());
		// 3 = .project + .repo file
		assertEquals(2, project.members().length);

		// contains the repo file
		assertTrue(project.findMember(REPO_NAME).exists());

		IFile repoFile = (IFile) project.findMember(REPO_NAME);
		// repo file should be empty because test did not go through project creation
		// to initialize .repo contents
		assertEquals(repoFile.getContents().available(), 0);
	}

	/**
	 * Test to see if the project has the proper nature.
	 *
	 * @throws CoreException
	 */
	@Test
	public void testProjectNature() throws CoreException {
		assertTrue(project.hasNature(CreaterepoProjectNature.CREATEREPO_NATURE_ID));
	}

}
