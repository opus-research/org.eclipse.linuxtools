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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource;
import org.eclipse.linuxtools.rpm.ui.editor.tests.FileTestCase;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CreatePatchesTest extends FileTestCase {
	private IEditorPart editor;

	@Override
	@Before
	public void setUp() throws CoreException {
		super.setUp();
		newFile("Patch0:         test.patch");
		editor = IDE.openEditor(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage(), testFile,
				"org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor");
	}

	@Override
	@After
	public void tearDown () throws CoreException {
		closeEditor(editor);
	}

	@Test
	public void testPatches() throws CoreException {
		assertNotNull(specfile);

		final byte[] empty = "".getBytes();
		final InputStream source = new ByteArrayInputStream(empty);

		List<SpecfileSource> specfileSourceList = specfile.getPatches();
		assertNotNull(specfileSourceList);

		IFile file = null;
		for (SpecfileSource ss : specfileSourceList) {
			file = testFile.getProject().getFile(new Path(ss.getFileName()));
			if (!file.exists()) {
				file.create(source, IResource.NONE, null);
				assertTrue(file.exists());
			}
		}

		// 3 Files = .project + test.patch + testCreatePatchesTest.spec
		assertTrue(testFile.getProject().members().length == 3);
		assertNotNull(testFile.getProject().findMember("test.patch"));
	}
}
