/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * Open in a browser implementation for the hyperlinks.
 */
public class SourcesFileBrowserHyperlink implements IHyperlink {

	String fileName;
	IFile original;
	IRegion region;

	/**
	 * Creates hyperlink for the following file name, region and file whether
	 * the file name is found.
	 *
	 * @param original The file where the reference to this file name is.
	 * @param fileName The name of the file to open.
	 * @param region The hyperlink region.
	 */
	public SourcesFileBrowserHyperlink(IFile original, String fileName, IRegion region) {
		this.fileName = fileName;
		this.original = original;
		this.region = region;
	}

	/**
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkRegion()
	 */
	public IRegion getHyperlinkRegion() {
		return region;
	}

	/**
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkText()
	 */
	public String getHyperlinkText() {
		return NLS.bind(Messages.SourcesFileHyperlink_3, fileName);
	}

	/**
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getTypeLabel()
	 */
	public String getTypeLabel() {
		return null;
	}

	/**
	 * Tries to open the url with the eclipse internal browser.
	 *
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#open()
	 */
	public void open() {
		try {
			IWebBrowser browser = PlatformUI
					.getWorkbench()
					.getBrowserSupport()
					.createBrowser(
							IWorkbenchBrowserSupport.NAVIGATION_BAR
									| IWorkbenchBrowserSupport.LOCATION_BAR
									| IWorkbenchBrowserSupport.STATUS,
							"rpm_open", null, null); //$NON-NLS-1$
			browser.openURL(new URL(fileName));
		} catch (MalformedURLException e) {
			SpecfileLog.logError(e);
		} catch (PartInitException e) {
			SpecfileLog.logError(e);
		}
	}
}