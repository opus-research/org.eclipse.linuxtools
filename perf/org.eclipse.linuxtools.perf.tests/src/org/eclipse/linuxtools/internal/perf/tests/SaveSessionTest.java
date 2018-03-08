/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import java.io.File;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.StatData;
import org.eclipse.linuxtools.internal.perf.ui.AbstractSaveDataHandler;
import org.eclipse.linuxtools.internal.perf.ui.PerfSaveSessionHandler;
import org.eclipse.linuxtools.internal.perf.ui.PerfSaveStatsHandler;

import junit.framework.TestCase;

public class SaveSessionTest extends TestCase {
	private static final String WORKING_DIR = "/mock/working/dir/"; //$NON-NLS-1$
	private static final String DATA_FILE_PATH = "/mock/data/path"; //$NON-NLS-1$
	private static final String DATA_FILE_NAME = "data"; //$NON-NLS-1$
	private static final String DATA_FILE_EXT = "ext"; //$NON-NLS-1$

	public void testGenericHandler() {
		GenericSaveDataHandler handler = new GenericSaveDataHandler();
		assertTrue(handler.canSave(new File(DATA_FILE_PATH)));
		assertEquals(WORKING_DIR, handler.getWorkingDir().toOSString());

		IPath path = handler.getNewDataLocation(DATA_FILE_NAME, DATA_FILE_EXT);
		assertEquals(WORKING_DIR + DATA_FILE_NAME + '.' + DATA_FILE_EXT, //$NON-NLS-1$
				path.toOSString());

		assertTrue(handler.isEnabled());
		assertTrue(handler.isHandled());
	}

	public void testPerfSaveSessionHandler() {
		PerfSaveSessionTestHandler handler = new PerfSaveSessionTestHandler();

		PerfPlugin.getDefault().setPerfProfileData(null);
		assertFalse(handler.verifyData());

		PerfPlugin.getDefault().setPerfProfileData(new Path(DATA_FILE_PATH));
		assertTrue(handler.verifyData());

	}

	// mock handlers

	public void testPerfSaveStatsHandler() {
		PerfSaveStatsTestHandler handler = new PerfSaveStatsTestHandler();

		PerfPlugin.getDefault().setStatData(null);
		assertFalse(handler.verifyData());

		PerfPlugin.getDefault().setStatData(
				new StatData("title", "prog", new String[] {}, 1) { //$NON-NLS-1$ //$NON-NLS-2$
					@Override
					public String getPerfData() {
						return DATA_FILE_PATH;
					}
				});
		assertTrue(handler.verifyData());
	}

	private class GenericSaveDataHandler extends AbstractSaveDataHandler {
		@Override
		public Object execute(ExecutionEvent event) {
			return null;
		}

		@Override
		protected void saveData(String filename) {
			// skip
		}

		@Override
		public boolean verifyData() {
			return true;
		}

		@Override
		protected IPath getWorkingDir() {
			return new Path(WORKING_DIR);
		}
	}

	private class PerfSaveSessionTestHandler extends PerfSaveSessionHandler {
		@Override
		protected IPath getWorkingDir() {
			return new Path(WORKING_DIR);
		}
	}

	private class PerfSaveStatsTestHandler extends PerfSaveStatsHandler {
		@Override
		protected IPath getWorkingDir() {
			return new Path(WORKING_DIR);
		}
	}

}
