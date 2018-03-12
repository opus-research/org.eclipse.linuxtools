/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tracing.examples.ui;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.importtrace.Messages;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * My import trace wizard implementation.
 */
@SuppressWarnings("restriction")
public class MyImportTraceWizard extends Wizard implements IImportWizard {

    static private final String PLUGIN_ID = Activator.PLUGIN_ID;
    static private final String IMPORT_WIZARD = "MyImportTraceWizard"; //$NON-NLS-1$
    static private final String ICON_PATH = "icons/wizban/trace_import_wiz.png"; //$NON-NLS-1$

    private IStructuredSelection fSelection;
    private MyImportTraceWizardPage fTraceImportWizardPage;

    /**
     * Default constructor
     */
    public MyImportTraceWizard() {
        IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(IMPORT_WIZARD);
        if (section == null) {
            section = workbenchSettings.addNewSection(IMPORT_WIZARD);
        }
        setDialogSettings(section);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        fSelection = selection;

        setWindowTitle(Messages.ImportTraceWizard_DialogTitle);
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, ICON_PATH));
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        super.addPages();
        fTraceImportWizardPage = new MyImportTraceWizardPage(fSelection);
        addPage(fTraceImportWizardPage);
    }

    @Override
    public boolean performFinish() {
        return fTraceImportWizardPage.finish();
    }

}
