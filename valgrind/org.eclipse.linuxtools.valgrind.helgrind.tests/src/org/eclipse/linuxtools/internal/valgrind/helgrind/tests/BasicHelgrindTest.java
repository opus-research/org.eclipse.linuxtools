/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel H Barboza <danielhb@br.ibm.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.helgrind.tests;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;

public class BasicHelgrindTest extends AbstractHelgrindTest {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
	}
	
	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
		super.tearDown();
	}
	
	public void testNumErrors() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testHelgrindGeneric"); //$NON-NLS-1$
				
		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		assertEquals(3, view.getMessages().length);
	}
}
