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
package org.eclipse.linuxtools.rpm.ui.editor.tests.hyperlink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
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
	}

	@Test
	public void testDetectHyperlinks() throws PartInitException {
		String testText = "Source0: test.zip\n";
		newFile(testText);

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
	public void testDetectHyperlinksNoRegionAndTextViewer() {
		SourcesFileHyperlinkDetector elementDetector = new SourcesFileHyperlinkDetector();
		elementDetector.setEditor(editor);
		IHyperlink[] returned = elementDetector.detectHyperlinks(null, null,
				false);
		assertNull(returned);
	}
}
