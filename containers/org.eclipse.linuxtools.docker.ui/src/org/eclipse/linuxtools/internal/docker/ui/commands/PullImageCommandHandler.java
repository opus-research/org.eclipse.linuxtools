/*******************************************************************************
 * Copyright (c) 2015,2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IRegistry;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;
import org.eclipse.linuxtools.docker.ui.wizards.ImageSearch;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.views.ImagePullProgressHandler;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImagePull;
import org.eclipse.linuxtools.internal.docker.ui.wizards.NewDockerConnection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.spotify.docker.client.DockerCertificateException;

/**
 * Command handler that opens the {@link ImageSearch} wizard and pulls the
 * selected image in background on completion.
 *
 */
public class PullImageCommandHandler extends AbstractHandler {

	private final static String PULL_IMAGE_JOB_TITLE = "ImagePull.title"; //$NON-NLS-1$
	private final static String PULL_IMAGE_JOB_TASK = "ImagePull.msg"; //$NON-NLS-1$
	private static final String ERROR_PULLING_IMAGE = "ImagePullError.msg"; //$NON-NLS-1$

	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final IDockerConnection connection = CommandUtils
				.getCurrentConnection(activePart);
		if (connection == null || !connection.isOpen()) {
			if (DockerConnectionManager.getInstance().getConnections().length == 0) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						boolean confirm = MessageDialog.openQuestion(
								PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow().getShell(),
								CommandMessages.getString(
										"BuildImageCommandHandler.no.connections.msg"), //$NON-NLS-1$
								CommandMessages.getString(
										"BuildImageCommandHandler.no.connections.desc")); // $NON-NLS-1$
						if (confirm) {
							NewDockerConnection newConnWizard = new NewDockerConnection();
							CommandUtils.openWizard(newConnWizard,
									HandlerUtil.getActiveShell(event));
						}
					}
				});
			} else {
			MessageDialog.openError(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell(),
						CommandMessages
								.getString("ErrorNoActiveConnection.msg"), //$NON-NLS-1$
						CommandMessages
								.getString("ErrorNoActiveConnection.desc")); //$NON-NLS-1$
			}
		} else {
			final ImagePull wizard = new ImagePull(connection);
			final boolean pullImage = CommandUtils.openWizard(wizard,
					HandlerUtil.getActiveShell(event));
			if (pullImage) {
				performPullImage(connection, wizard.getImageName(),
						wizard.getRegistry());
			}
		}
		return null;
	}

	private void performPullImage(final IDockerConnection connection,
			final String imageName, final IRegistry registry) {
		final Job pullImageJob = new Job(DVMessages
				.getFormattedString(PULL_IMAGE_JOB_TITLE, imageName)) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask(DVMessages.getString(PULL_IMAGE_JOB_TASK),
						IProgressMonitor.UNKNOWN);
				// pull the image and let the progress
				// handler refresh the images when done
				try {
					if (registry == null) {
						((DockerConnection) connection).pullImage(imageName,
								new ImagePullProgressHandler(connection,
										imageName));
					} else {
						String fullImageName = registry.getServerAddress() + '/' + imageName;
						if (registry instanceof IRegistryAccount) {
							IRegistryAccount account = (IRegistryAccount) registry;
							((DockerConnection) connection).pullImage(fullImageName,
									account, new ImagePullProgressHandler(
											connection, fullImageName));
						} else {
							((DockerConnection) connection).pullImage(fullImageName,
									new ImagePullProgressHandler(connection,
											fullImageName));
						}
					}
				} catch (final DockerException e) {
					Display.getDefault().syncExec(() -> MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
							DVMessages.getFormattedString(ERROR_PULLING_IMAGE,
									imageName),
							e.getMessage()));
					// for now
				} catch (InterruptedException | DockerCertificateException e) {
					// do nothing
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}

		};

		pullImageJob.schedule();

	}

}
