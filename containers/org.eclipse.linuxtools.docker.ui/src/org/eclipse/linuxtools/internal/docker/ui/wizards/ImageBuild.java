/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;

public class ImageBuild extends Wizard {

	private ImageBuildPage mainPage;
	private String imageName;
	private IPath directory;
	private int lines;

	public ImageBuild() {
		super();
	}

	public String getImageName() {
		return imageName;
	}

	public IPath getDirectory() {
		return directory;
	}

	public int getNumberOfLines() {
		return lines;
	}

	@Override
	public void addPages() {
		mainPage = new ImageBuildPage();
		addPage(mainPage);
	}

	@Override
	public boolean canFinish() {
		return mainPage.isPageComplete();
	}

	private int numberOfLines() throws IOException {
		String fileName = directory.append("Dockerfile").toString(); //$NON-NLS-1$
		InputStream is = null;
		int count = 0;
		boolean empty = false;
		try {
			is = new BufferedInputStream(new FileInputStream(fileName));
			byte[] c = new byte[1024];
			int readChars = 0;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
		} finally {
			if (is != null)
				is.close();
		}
		return (count == 0 && !empty) ? 1 : count;
	}

	@Override
	public boolean performFinish() {
		imageName = mainPage.getImageName();
		directory = new Path(mainPage.getDirectory());

		try {
			lines = numberOfLines();
		} catch (IOException e) {
			// do nothing
		}

		try {
			Files.walkFileTree(Paths.get(directory.toString()),
					new FileVisitor<java.nio.file.Path>() {
						@Override
						public FileVisitResult preVisitDirectory(
								java.nio.file.Path dir,
								BasicFileAttributes attrs) {
							return FileVisitResult.CONTINUE;
						}
						@Override
						public FileVisitResult visitFile(
								java.nio.file.Path file,
								BasicFileAttributes attrs) {
							return FileVisitResult.CONTINUE;
						}
						@Override
						public FileVisitResult visitFileFailed(
								java.nio.file.Path file, IOException exc)
										throws IOException {
							throw exc;
						}
						@Override
						public FileVisitResult postVisitDirectory(
								java.nio.file.Path dir, IOException exc) {
							return FileVisitResult.CONTINUE;
						}
					});
		} catch (final IOException e) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(
							Display.getCurrent().getActiveShell(),
									WizardMessages.getString(
											"ErrorInvalidDirectory.msg"),
							WizardMessages.getFormattedString(
									"ErrorInvalidPermissions.msg",
									directory.toString()));
				}
			});
			return false;
		}

		return true;
	}

}
