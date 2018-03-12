/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.jdt.docker.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class JavaImageTab extends AbstractLaunchConfigurationTab {

	private ComboViewer connCmb, imageCmb;
	private IDockerConnection selectedConnection;
	private IDockerImage selectedImage;

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		Label connLbl = new Label(composite, SWT.NONE);
		connLbl.setText(Messages.ImageSelectionDialog_connection_label);
		connCmb = new ComboViewer(composite, SWT.READ_ONLY);
		connCmb.getCombo().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		connCmb.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				return DockerConnectionManager.getInstance().getAllConnections().stream().filter(c -> c.isOpen()).toArray(size -> new IDockerConnection[size]);
			}
		});
		connCmb.setInput("place_holder"); //$NON-NLS-1$

		Label imageLbl = new Label(composite, SWT.NONE);
		imageLbl.setText(Messages.ImageSelectionDialog_image_label);
		imageCmb = new ComboViewer(composite, SWT.READ_ONLY);
		imageCmb.getCombo().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		imageCmb.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				IDockerConnection conn = (IDockerConnection) inputElement;
				return conn.getImages().stream()
						.filter(i -> ! i.repoTags().get(0).equals("<none>:<none>")) //$NON-NLS-1$
						.toArray(size -> new IDockerImage[size]);
			}
		});
		imageCmb.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IDockerImage img = (IDockerImage) element;
				return img.repoTags().get(0);
			}
		});
		imageCmb.setInput(null);

		connCmb.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = event.getStructuredSelection();
				IDockerConnection conn = (IDockerConnection) sel.getFirstElement();
				selectedConnection = conn;
				imageCmb.setInput(conn);
				updateLaunchConfigurationDialog();
			}
		});

		imageCmb.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = event.getStructuredSelection();
				IDockerImage img = (IDockerImage) sel.getFirstElement();
				selectedImage = img;
				updateLaunchConfigurationDialog();
			}
		});

		setControl(composite);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(JavaLaunchConfigurationConstants.CONNECTION_URI, (String) null);
		configuration.setAttribute(JavaLaunchConfigurationConstants.IMAGE_ID, (String) null);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			DockerConnection conn;
			String connUri = configuration.getAttribute(JavaLaunchConfigurationConstants.CONNECTION_URI, (String) null);
			if (connUri != null) {
				connCmb.getCombo().setText(connUri);
				conn = (DockerConnection) DockerConnectionManager.getInstance().getConnectionByUri(connUri);
				String imageId = configuration.getAttribute(JavaLaunchConfigurationConstants.IMAGE_ID, (String) null);
				if (imageId != null && conn != null) {
					selectedConnection = conn;
					imageCmb.setInput(conn); // generate known images for given connection
					IDockerImage img = conn.getImage(imageId);
					if (img != null) {
						String repoTag = img.repoTags().get(0);
						imageCmb.getCombo().setText(repoTag);
						selectedImage = img;
					}
				}
			}
		} catch (CoreException e) {
		}

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (selectedConnection != null) {
			configuration.setAttribute(JavaLaunchConfigurationConstants.CONNECTION_URI, selectedConnection.getUri());
		}
		if (selectedImage != null) {
			configuration.setAttribute(JavaLaunchConfigurationConstants.IMAGE_ID, selectedImage.id());
		}
	}

	@Override
	public boolean canSave() {
		return selectedConnection != null && selectedImage != null;
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		return canSave();
	}

	@Override
	public String getName() {
		return "Image Tab";
	}

}
