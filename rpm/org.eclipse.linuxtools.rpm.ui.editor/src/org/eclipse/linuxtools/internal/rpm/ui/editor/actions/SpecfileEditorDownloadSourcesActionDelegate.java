/*******************************************************************************
 * Copyright (c) 2008, 2013 Alexander Kurtakov, Neil Guzman.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *    Neil Guzman        - RPM download sources implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.actions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.linuxtools.internal.rpm.ui.editor.UiUtils;
import org.eclipse.linuxtools.internal.rpm.core.utils.DownloadJob;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.linuxtools.rpm.ui.editor.utils.RPMHandlerUtils;
import org.eclipse.linuxtools.rpm.ui.editor.utils.RPMUtils;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class SpecfileEditorDownloadSourcesActionDelegate extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Shell shell =  HandlerUtil.getActiveShellChecked(event);
		final SpecfileParser specparser = new SpecfileParser();
		IFile workFile = null;
		Specfile specfile = null;

		IResource resource = RPMHandlerUtils.getResource(event);
		workFile = resource instanceof IFile ? (IFile) resource : null;
		specfile = specparser.parse(workFile);

		// retrieve source(s) from specfile
		final List<SpecfileSource> sourceURLList = specfile != null ? (List<SpecfileSource>) specfile
				.getSources() : null;

		// go through each source, resolve the defines, and then download the file
		// currently stops immediately once an invalid source URL is encountered
		for (final SpecfileSource sourceurls : sourceURLList) {
			try {
				String rawURL = sourceurls.getFileName().toString();
				String resolvedURL = UiUtils.resolveDefines(specfile, rawURL.toString());
				URL url = null;
				try {
					url = new URL(resolvedURL);
				} catch(MalformedURLException e) {
					SpecfileLog.logError(Messages.DownloadSources_malformedURL + resolvedURL, e);
					RPMUtils.showErrorDialog(shell, "Error", //$NON-NLS-1$
							Messages.DownloadSources_malformedURL + resolvedURL);
					return null;
				}

				HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				if (connection.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND) {
					// grab the name of the file from the URL
					int offset = url.toString().lastIndexOf("/"); //$NON-NLS-1$
					String filename = url.toString().substring(offset + 1);

					// create the path to the "to be downloaded" file
					IFile file = workFile.getParent().getFile(new Path(filename));

					Job downloadJob = new DownloadJob(file, connection);
					downloadJob.setUser(true);
					downloadJob.schedule();
				}
			} catch (IOException e) {
				SpecfileLog.logError(Messages.DownloadSources_cannotConnectToURL, e);
				RPMUtils.showErrorDialog(shell, "Error", //$NON-NLS-1$
						Messages.DownloadSources_cannotConnectToURL);
				return null;
			}
		}

		return null;
	}

}
