/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.core.tests;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.Plugin;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.osgi.framework.Bundle;

public abstract class AbstractInlineDataTest {
	public static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
	public static final Charset CHARSET_UTF_8 = Charset.forName(UTF_8);
	@Rule public TemporaryFolder tmpfiles = new TemporaryFolder();
	@Rule public TestName testName = new TestName();

	/**
	 * Due to stack frame hack this method have to be called directly from the method that has comment
	 */
	protected String getAboveComment() {
		return getAboveComment(getName());
	}

	protected String getName() {
		return testName.getMethodName();
	}


	protected String getAboveComment(String name) {
		return getContents(1, name)[0].toString();
	}

	protected File getAboveCommentAndSaveFile(String fileName) throws IOException {
		String value = getAboveComment(getName());
		File file = fileName == null ? tmpfiles.newFile() : tmpfiles.newFile(fileName);
		saveToFile(value, file);
		return file;
	}

	protected void saveToFile(String value, File file) throws IOException, FileNotFoundException {
		try (FileOutputStream st = new FileOutputStream(file)) {
			st.write(value.getBytes(CHARSET_UTF_8));
		}
	}

	protected StringBuilder[] getContents(int sections, String name) {
		try {
			return TestSourceReader.getContentsForTest(getBundle(), getSourcePrefix(), getClass(), name, sections);
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}

	protected String getSourcePrefix() {
		return "src";
	}

	protected Bundle getBundle() {
		if (getPlugin() == null)
			return null;
		return getPlugin().getBundle();
	}

	protected abstract Plugin getPlugin();
}
