/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.launch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

public class ValgrindExportWizard extends Wizard implements IExportWizard {

    private ValgrindExportWizardPage exportPage;

    @Override
    public boolean performFinish() {
        final File[] logs = exportPage.getSelectedFiles();
        final IPath outputPath = exportPage.getOutputPath();

        IProgressService ps = PlatformUI.getWorkbench().getProgressService();
		try {
			ps.busyCursorWhile(monitor -> {
				if (logs.length > 0) {
					File outputDir = outputPath.toFile();
					monitor.beginTask(
							NLS.bind(Messages.getString("ValgrindExportWizard.Export_task"), outputPath.toOSString()), //$NON-NLS-1$
							logs.length);
					for (File log : logs) {
						monitor.subTask(
								NLS.bind(Messages.getString("ValgrindExportWizard.Export_subtask"), log.getName())); //$NON-NLS-1$

						File outLog = new File(outputDir, log.getName());
						try (FileInputStream fis = new FileInputStream(log);
								FileChannel inChan = fis.getChannel();
								FileOutputStream fos = new FileOutputStream(outLog);
								FileChannel outChan = fos.getChannel()) {
							outChan.transferFrom(inChan, 0, inChan.size());
						} catch (IOException e) {
							throw new InvocationTargetException(e);
						}
						monitor.worked(1);
					}
					monitor.done();
				}
			});

		} catch (InvocationTargetException e) {
            IStatus status = new Status(IStatus.ERROR, ValgrindLaunchPlugin.PLUGIN_ID, Messages.getString("ValgrindExportWizard.Export_fail"), e); //$NON-NLS-1$
            ErrorDialog.openError(getShell(), ExportWizardConstants.WIZARD_TITLE, null, status);
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            //ignore
        }

        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle(ExportWizardConstants.WIZARD_WINDOW_TITLE);
        exportPage = getWizardPage();
        exportPage.setDescription(ExportWizardConstants.WIZARD_DESCRIPTION);
        addPage(exportPage);
    }

    private ValgrindExportWizardPage getWizardPage() {
        return new ValgrindExportWizardPage(Messages.getString("ValgrindExportWizard.Page_name"), ExportWizardConstants.WIZARD_TITLE, null); //$NON-NLS-1$
    }

}
