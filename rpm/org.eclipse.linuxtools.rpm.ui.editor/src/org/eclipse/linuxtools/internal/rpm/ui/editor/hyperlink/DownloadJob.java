/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Alexander Kurtakov (Red Hat) - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.osgi.util.NLS;

public class DownloadJob extends Job {
	private IFile file;
	private URLConnection content;

	public DownloadJob(IFile file, URLConnection content) {
		super(NLS.bind(Messages.SourcesFileDownloadHyperlink_4, file.getName()));
		this.file = file;
		this.content = content;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(
				NLS.bind(Messages.SourcesFileDownloadHyperlink_4,
						file.getName()), content.getContentLength());
		try {
			File tempFile = File.createTempFile(file.getName(), ""); //$NON-NLS-1$
			FileOutputStream fos = new FileOutputStream(tempFile);
			InputStream is = new BufferedInputStream(content.getInputStream());
			int b;
			byte buf[] = new byte[5 * 1024];
			boolean canceled = false;
				while ((b = is.read(buf)) != -1) {
					if (monitor.isCanceled()) {
						canceled = true;
						break;
					}
					fos.write(buf, 0 ,b);
					monitor.worked(1);
				}
			is.close();
			fos.close();
			if (!canceled) {
				// override the previous file if there is one
				if (file.exists()) {
					file.setContents(new FileInputStream(tempFile), true,
							false, monitor);
				} else {
					file.create(new FileInputStream(tempFile), true, monitor);

				}
			}
			tempFile.delete();
		} catch (CoreException e) {
			SpecfileLog.logError(e);
			return Status.CANCEL_STATUS;
		} catch (IOException e) {
			SpecfileLog.logError(e);
			return Status.CANCEL_STATUS;
		}
		monitor.done();
		return Status.OK_STATUS;
	}
}