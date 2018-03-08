/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Medeiros Teixeira <rafaelmt@linux.vnet.ibm.com> - initial API and implementation
*******************************************************************************/

package org.eclipse.linuxtools.internal.valgrind.memcheck.quickfix.tests;

import java.io.InputStream;
import java.util.Scanner;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.text.Document;
import org.eclipse.linuxtools.internal.valgrind.tests.AbstractValgrindTest;
import org.eclipse.linuxtools.internal.valgrind.ui.quickfixes.WrongDeallocationResolution;



public class WrongDeallocationResolutionTest extends AbstractValgrindTest {

	private final String EMPTY_STRING = ""; //$NON-NLS-1$
	private final String VALGRIND_MARKER_TYPE = "org.eclipse.linuxtools.valgrind.launch.marker"; //$NON-NLS-1$
	private final String MISMATCHED_FREE_MESSAGE = "Mismatched free() / delete / delete"; //$NON-NLS-1$

	@Override
	protected String getToolID() {
		return "org.eclipse.linuxtools.valgrind.launch.memcheck"; //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("wrongDeallocTest"); //$NON-NLS-1$
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
		super.tearDown();
	}

	public void testWrongDeallocationQuickFix() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "wrongDeallocTest"); //$NON-NLS-1$

		IMarker[] markers = proj.getProject().findMarkers(VALGRIND_MARKER_TYPE, true, 1);
		assertTrue(markers.length > 0);

		WrongDeallocationResolution resolution = null;
		for (IMarker marker : markers) {
			if(marker.getAttribute(IMarker.MESSAGE, EMPTY_STRING).contains(MISMATCHED_FREE_MESSAGE)){
				resolution = new WrongDeallocationResolution(markers[0]);
			}
		}
		assertNotNull(resolution);

		Document document = new Document();

		InputStream fileInputStream = proj.getProject().getFile("wrongDealloc.cpp").getContents(); //$NON-NLS-1$
	    Scanner scanner = new java.util.Scanner(fileInputStream).useDelimiter("\\A"); //$NON-NLS-1$
	    String content;
	    if(scanner.hasNext()) {
	    	content = scanner.next();
	    } else {
	    	content = EMPTY_STRING;
	    }
	    document.set(content);
	    resolution.apply(markers[0], document);

	    assertEquals(0, proj.getProject().findMarkers(VALGRIND_MARKER_TYPE, true, 1).length);

	    String newContent = document.get(0, document.getLength());
	    assertTrue(newContent.contains("delete")); //$NON-NLS-1$
	    assertFalse(newContent.contains("free")); //$NON-NLS-1$
	}
}
