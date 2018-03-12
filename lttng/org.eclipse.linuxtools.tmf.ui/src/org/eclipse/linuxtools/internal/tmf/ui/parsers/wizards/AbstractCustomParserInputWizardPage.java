/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/


package org.eclipse.linuxtools.internal.tmf.ui.parsers.wizards;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

/**
 * Common routines for update wizards
 *
 * @author Matthew Khouzam
 *
 */
public abstract class AbstractCustomParserInputWizardPage extends WizardPage {

    private static final String TIMESTAMP_FORMAT_BUNDLE = "org.eclipse.linuxtools.lttng.help"; //$NON-NLS-1$
    private static final String TIMESTAMP_FORMAT_PATH = "reference/api/org/eclipse/linuxtools/tmf/core/timestamp/TmfTimestampFormat.html"; //$NON-NLS-1$

    private Composite container;
    private UpdateListener fUpdateListener;
    private Browser helpBrowser;

    /**
     * Abstract custom parser input wizard page constructor
     *
     * @param pageName
     *            the name of the page
     */
    public AbstractCustomParserInputWizardPage(String pageName) {
        super(pageName);
    }

    /**
     * Abstract custom parser input wizard page constructor
     *
     * @param pageName
     *            the name of the page
     * @param title
     *            the title of the page
     * @param titleImage
     *            the image to show the page
     */
    public AbstractCustomParserInputWizardPage(String pageName, String title, ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
    }

    @Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout());

        fUpdateListener = new UpdateListener();

        Composite headerComposite = new Composite(container, SWT.FILL);
        GridLayout headerLayout = new GridLayout(5, false);
        headerLayout.marginHeight = 0;
        headerLayout.marginWidth = 0;
        headerComposite.setLayout(headerLayout);
        headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label logtypeLabel = new Label(headerComposite, SWT.NULL);
        logtypeLabel.setText(Messages.CustomXmlParserInputWizardPage_logType);

        createControl(headerComposite, fUpdateListener);
        setControl(container);

        validate();
        updatePreviews();
    }

    /**
     * Get the container
     *
     * @return the composite
     */
    protected Composite getCompositeContainer() {
        return container;
    }

    /**
     * Get the help for timestamps
     *
     * @return the help listener showing how timestamps work
     */
    protected SelectionListener getTimestampHelpListener() {
        final SelectionAdapter listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Bundle plugin = Platform.getBundle(TIMESTAMP_FORMAT_BUNDLE);
                IPath path = new Path(TIMESTAMP_FORMAT_PATH);
                URL fileURL = FileLocator.find(plugin, path, null);
                try {
                    URL pageURL = FileLocator.toFileURL(fileURL);
                    openHelpShell(pageURL.toString());
                } catch (IOException e1) {
                }
            }
        };
        return listener;
    }

    /**
     * Create common control
     *
     * @param headerComposite
     *            the header where stuff goes
     * @param updateListener
     *            the update listener to apply to buttons
     */
    protected abstract void createControl(Composite headerComposite, UpdateListener updateListener);

    /**
     * open the help shell
     *
     * @param url
     *            the help location
     */
    protected void openHelpShell(String url) {
        if (helpBrowser != null && !helpBrowser.isDisposed()) {
            helpBrowser.getShell().setActive();
            if (!helpBrowser.getUrl().equals(url)) {
                helpBrowser.setUrl(url);
            }
            return;
        }
        final Shell helpShell = new Shell(getShell(), SWT.SHELL_TRIM);
        helpShell.setLayout(new FillLayout());
        helpBrowser = new Browser(helpShell, SWT.NONE);
        helpBrowser.addTitleListener(new TitleListener() {
            @Override
            public void changed(TitleEvent event) {
                helpShell.setText(event.title);
            }
        });
        Rectangle r = container.getBounds();
        Point p = container.toDisplay(r.x, r.y);
        Rectangle trim = helpShell.computeTrim(p.x + (r.width - 750) / 2, p.y + (r.height - 400) / 2, 750, 400);
        helpShell.setBounds(trim);
        helpShell.open();
        helpBrowser.setUrl(url);
        return;
    }

    /**
     * Validate input
     */
    protected abstract void validate();

    /**
     * Update preview page
     */
    protected abstract void updatePreviews();

    /**
     * Update listener, always validate and update
     *
     * @author Patrick Tasse
     */
    public class UpdateListener implements ModifyListener, SelectionListener {

        @Override
        public void modifyText(ModifyEvent e) {
            validate();
            updatePreviews();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            validate();
            updatePreviews();
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            validate();
            updatePreviews();
        }

    }

    /**
     * Get an update listener
     *
     * @return an update listener
     */
    protected UpdateListener getUpdateListener() {
        return fUpdateListener;
    }

}