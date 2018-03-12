/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Wizard to add a Docker daemon connection
 * @author xcoulon
 *
 */
public class NewDockerConnection extends Wizard {
	
	private final NewDockerConnectionPage wizardPage;
	private IDockerConnection dockerConnection;

	/**
	 * Opens the wizard after running a job that attempts to find the default
	 * connection settings and get the connection name from the available info
	 * (if the Docker daemon is reachable)
	 * 
	 * @param shell
	 * @param commonViewer
	 *            the {@link CommonViewer} to refresh if a connection was added
	 * 
	 * @return <code>true</code> if a new {@link IDockerConnection} was created,
	 *         <code>false</code> otherwise.
	 */
	public static void open(final Shell shell) {
		final NewDockerConnectionPageModel connectionPageModel = new NewDockerConnectionPageModel();
		final Job defaultSettingsJob = new Job(WizardMessages
				.getString("NewDockerConnectionPage.retrieveTask")) { //$NON-NLS-1$

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(
						WizardMessages.getString(
								"NewDockerConnectionPage.retrieveTask"), //$NON-NLS-1$
						1);
				final DockerConnection.Defaults defaults = new DockerConnection.Defaults();
				connectionPageModel.setCustomSettings(!defaults.isSettingsResolved());
				connectionPageModel
						.setPingSucceeded(defaults.isPingSucceeded());
				if (defaults.getName() != null
						&& !defaults.getName().isEmpty()) {
					connectionPageModel.setConnectionName(defaults.getName());
				}
				connectionPageModel.setBindingMode(defaults.getBindingMode());
				connectionPageModel
						.setUnixSocketPath(defaults.getUnixSocketPath());
				connectionPageModel.setTcpHost(defaults.getTcpHost());
				connectionPageModel.setTcpTLSVerify(defaults.getTcpTlsVerify());
				connectionPageModel.setTcpCertPath(defaults.getTcpCertPath());
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		defaultSettingsJob.setUser(true);
		defaultSettingsJob.addJobChangeListener(new JobChangeAdapter() {

			@Override
			public void done(final IJobChangeEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						final WizardDialog wizardDialog = new WizardDialog(
								shell,
								new NewDockerConnection(connectionPageModel));
						wizardDialog.create();
						wizardDialog.open();
					}
				});
			}
		});
		defaultSettingsJob.schedule();
	}

	private NewDockerConnection(
			final NewDockerConnectionPageModel connectionPageModel) {
		super();
		this.wizardPage = new NewDockerConnectionPage(connectionPageModel);
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		addPage(wizardPage);
	}

	@Override
	public boolean performFinish() {
		try {
			dockerConnection = wizardPage.getDockerConnection();
			DockerConnectionManager.getInstance().addConnection(dockerConnection);
			return true;
		} catch (DockerException e) {
			new MessageDialog(Display.getDefault().getActiveShell(),
					WizardMessages.getString("NewDockerConnection.failure"), //$NON-NLS-1$
					null,
					WizardMessages.getString("NewDockerConnection.failMessage"), //$NON-NLS-1$
					SWT.ICON_ERROR,
					new String[] { WizardMessages
							.getString("NewDockerConnectionPage.ok") }, //$NON-NLS-1$
					0).open(); // ;
		}
		return false;
	}
	
	public IDockerConnection getDockerConnection() {
		return dockerConnection;
	}

}
