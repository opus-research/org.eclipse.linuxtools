/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.tests.actions;

import static org.junit.Assert.assertNotNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.SourcesFileHyperlink;
import org.eclipse.linuxtools.rpm.ui.editor.tests.FileTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CreatePatchesTest extends FileTestCase {
	private SourcesFileHyperlink patchTest;

	@Override
	@Before
	public void setUp() throws CoreException {
		super.setUp();
		newFile("Patch0:         test.patch");
		assertNotNull(specfile);
		patchTest = new SourcesFileHyperlink(testFile, specfile.getPatch(0).getFileName(), null);
	}

	@Override
	@After
	public void tearDown () throws CoreException {
		super.tearDown();
	}

	@Test
	public void testPatches() {
		// click "OK" when prompted to create the patch file
		patchTest.open();
		assertNotNull(testFile.getProject().findMember("test.patch"));
	}
}
