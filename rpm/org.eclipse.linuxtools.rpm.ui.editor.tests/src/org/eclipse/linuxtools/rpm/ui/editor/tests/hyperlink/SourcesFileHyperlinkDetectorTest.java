/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.tests.hyperlink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.SourcesFileCreateHyperlink;
import org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.SourcesFileHyperlinkDetector;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.tests.FileTestCase;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.Before;
import org.junit.Test;

public class SourcesFileHyperlinkDetectorTest extends FileTestCase {
	@Before
	public void init() throws CoreException {
		super.setUp();
		String testText = "Source0: test.zip\nPatch0: test.patch\n";
		newFile(testText);
	}

	@Test
	public void testDetectHyperlinks() throws PartInitException {
		IEditorPart openEditor = IDE.openEditor(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage(), testFile,
				"org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor");

		editor = (SpecfileEditor) openEditor;
		editor.doRevertToSaved();
		SourcesFileHyperlinkDetector elementDetector = new SourcesFileHyperlinkDetector();
		elementDetector.setEditor(editor);
		// test source element
		IRegion region = new Region(10, 0);
		IHyperlink[] returned = elementDetector.detectHyperlinks(
				editor.getSpecfileSourceViewer(), region, false);
		assertEquals(2, returned.length);

		// test empty
		region = new Region(4, 0);
		returned = elementDetector.detectHyperlinks(
				editor.getSpecfileSourceViewer(), region, false);
		assertNull(returned);
	}

	@Test
	public void testDetectNoPatchInProject() throws PartInitException {
		IEditorPart openEditor = IDE.openEditor(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage(), testFile,
				"org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor");

		editor = (SpecfileEditor) openEditor;
		editor.doRevertToSaved();
		SourcesFileHyperlinkDetector elementDetector = new SourcesFileHyperlinkDetector();
		elementDetector.setEditor(editor);
		// test patch element
		IRegion region = new Region(27, 0);
		IHyperlink[] returned = elementDetector.detectHyperlinks(
				editor.getSpecfileSourceViewer(), region, false);
		// 1 = Create test.patch because test.patch doesn't exist in current project
		assertEquals(1, returned.length);
	}

	@Test
	public void testDetectHyperlinksNoRegionAndTextViewer() {
		SourcesFileHyperlinkDetector elementDetector = new SourcesFileHyperlinkDetector();
		elementDetector.setEditor(editor);
		IHyperlink[] returned = elementDetector.detectHyperlinks(null, null,
				false);
		assertNull(returned);
	}

	@Test
	public void testCreatePatch() {
		SourcesFileCreateHyperlink patchTest = new SourcesFileCreateHyperlink(testFile, specfile.getPatch(0).getFileName(), null);
		assertNotNull(patchTest);
		assertTrue(testCreate());
		assertNotNull(testFile.getProject().findMember("test.patch"));
	}

	/**
	 * Used to create the patch file specified in the specfile
	 *
	 * @return True if patch file was created
	 */
	private boolean testCreate() {
		boolean rc = false;
		IContainer container = testFile.getParent();
		final InputStream source = new ByteArrayInputStream("".getBytes()); //$NON-NLS-1$
		IFile file = null;

		IResource sourcesFolder = container.getProject().findMember(
				"SOURCES"); //$NON-NLS-1$
		file = container.getFile(new Path(specfile.getPatch(0).getFileName()));
		if (sourcesFolder != null && sourcesFolder.exists()) {
			file = ((IFolder) sourcesFolder).getFile(new Path(specfile.getPatch(0).getFileName()));
		}
		if (!file.exists()) {
			try {
				file.create(source, IResource.NONE, null);
				rc = true;
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return rc;
	}
}
