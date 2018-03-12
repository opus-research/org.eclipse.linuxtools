/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.NETWORK_MODE;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * A {@link WizardPage} to let the user select the CPU and memory allocation for
 * the container, as well as the volumes to mount.
 * 
 * @author xcoulon
 *
 */
public class ImageRunNetworkPage extends WizardPage {

	private final int COLUMNS = 2;

	private final DataBindingContext dbc = new DataBindingContext();
	private final ImageRunNetworkModel model;
	private final ILaunchConfiguration lastLaunchConfiguration;

	private IDockerImage selectedImage;

	/**
	 * Default constructor.
	 * 
	 * @param connection
	 *            the {@link IDockerConnection} to use
	 * @throws DockerException
	 *             if obtaining info from the given {@link IDockerConnection}
	 *             failed
	 * 
	 */
	public ImageRunNetworkPage(final IDockerConnection connection) {
		super("ImageRunNetworkPage", //$NON-NLS-1$
				WizardMessages.getString("ImageRunNetworkPage.title"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setPageComplete(true);
		this.model = new ImageRunNetworkModel(connection);
		this.lastLaunchConfiguration = null;
	}

	/**
	 * Default constructor.
	 * 
	 * @param selectedImage
	 *            the {@link IDockerImage} to run
	 * @param connection
	 *            the {@link IDockerConnection} to use
	 * @throws DockerException
	 *             if obtaining info from the given {@link IDockerConnection}
	 *             failed
	 * 
	 */
	public ImageRunNetworkPage(
			@SuppressWarnings("unused") final IDockerImage selectedImage,
			final ILaunchConfiguration lastLaunchConfiguration) {
		super("ImageRunNetworkPage", //$NON-NLS-1$
				WizardMessages.getString("ImageRunNetworkPage.title"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setPageComplete(true);
		this.model = new ImageRunNetworkModel(selectedImage);
		this.lastLaunchConfiguration = lastLaunchConfiguration;
	}

	public ImageRunNetworkModel getModel() {
		return model;
	}

	@Override
	public void createControl(Composite parent) {
		final ScrolledComposite scrollTop = new ScrolledComposite(parent,
				SWT.H_SCROLL | SWT.V_SCROLL);
		scrollTop.setExpandVertical(true);
		scrollTop.setExpandHorizontal(true);
		final Composite container = new Composite(scrollTop, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.applyTo(container);
		createNetworkModeContainer(container);
		setDefaultValues();

		scrollTop.setContent(container);
		Point point = container.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrollTop.setSize(point);
		scrollTop.setMinSize(point);
		// TODO: Workaround https://bugs.eclipse.org/bugs/show_bug.cgi?id=487160
		setControl(scrollTop);
	}

	private void createNetworkModeContainer(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.span(COLUMNS, 1).grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().spacing(10, 2).applyTo(container);
		final int COLUMNS = 6;
		final int INDENT = 20;
		final Composite subContainer = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.indent(INDENT, 0).span(COLUMNS, 1).grab(true, false)
				.applyTo(subContainer);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.spacing(10, 2).applyTo(subContainer);

		// specify default network
		final Label modeLabel = new Label(subContainer, SWT.NONE);
		modeLabel.setText(WizardMessages.getString("ImageRunNetworkPage.mode")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(modeLabel);

		modeLabel.setEnabled(true);
		return;
		// final Button defaultButton = new Button(subContainer,
		// SWT.RADIO);
		// bindButton(defaultButton, ImageRunNetworkModel.DEFAULT_MODE);
		// defaultButton.setText(WizardMessages
		// .getString("ImageRunNetworkPage.mode.default")); //$NON-NLS-1$
		// GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
		// .applyTo(defaultButton);
		// final Button bridgeButton = new Button(subContainer,
		// SWT.RADIO);
		// bridgeButton.setText(WizardMessages
		// .getString("ImageRunNetworkPage.mode.bridge")); //$NON-NLS-1$
		// bindButton(bridgeButton, ImageRunNetworkModel.BRIDGE_MODE);
		// GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
		// .applyTo(bridgeButton);
		// final Button hostButton = new Button(subContainer,
		// SWT.RADIO);
		// hostButton.setText(WizardMessages
		// .getString("ImageRunNetworkPage.mode.host")); //$NON-NLS-1$
		// bindButton(hostButton, ImageRunNetworkModel.HOST_MODE);
		// GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
		// .applyTo(hostButton);
		// final Button noneButton = new Button(subContainer, SWT.RADIO);
		// hostButton.setText(
		// WizardMessages.getString("ImageRunNetworkPage.mode.none"));
		// //$NON-NLS-1$
		// bindButton(hostButton, ImageRunNetworkModel.NONE_MODE);
		// GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
		// .applyTo(noneButton);
		//
		// final Button containerButton = new Button(subContainer, SWT.RADIO);
		// containerButton.setText(
		// WizardMessages.getString("ImageRunNetworkPage.mode.container"));
		// //$NON-NLS-1$
		// bindButton(containerButton, ImageRunNetworkModel.CONTAINER_MODE);
		// GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
		// .applyTo(containerButton);
		//
		// return;

		// // Container list
		// final Combo containerList = new Combo(subContainer,
		// SWT.DROP_DOWN | SWT.READ_ONLY);
		// containerList
		// .setItems(model.getContainerNames().toArray(new String[] {}));
		// GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
		// .grab(false, false).applyTo(containerList);
		// dbc.bindValue(WidgetProperties.selection().observe(containerList),
		// BeanProperties
		// .value(ImageRunNetworkModel.class,
		// ImageRunNetworkModel.SELECTED_CONTAINER)
		// .observe(model));
	}

	/**
	 * Binds the given <code>cpuShares</code> value to the given {@link Button}
	 * when it is selected.
	 * 
	 * @param button
	 *            the {@link Button} to bind
	 * @param cpuShares
	 *            the <code>cpuShares</code> to bind to the {@link Button}
	 * @return
	 */
	private Binding bindButton(final Button button, final String strValue) {
		return dbc.bindValue(WidgetProperties.selection().observe(button),
				BeanProperties
						.value(ImageRunNetworkModel.class,
								ImageRunNetworkModel.NETWORK_MODE)
						.observe(model),
				new UpdateValueStrategy() {
					@Override
					public Object convert(Object value) {
						if (value.equals(Boolean.TRUE)) {
							return strValue;
						}
						return ""; //$NON-NLS-1$
					}

				}, new UpdateValueStrategy() {
					@Override
					public Object convert(final Object value) {
						return value.equals(strValue);
					}
				});
	}

	private void setDefaultValues() {
		try {
			if (lastLaunchConfiguration != null) {
				// network mode
				model.setNetworkMode(lastLaunchConfiguration.getAttribute(
						NETWORK_MODE, ImageRunNetworkModel.DEFAULT_MODE)); // $NON-NLS-1$
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	private static void setControlsEnabled(final Control[] controls,
			final boolean enabled) {
		for (Control control : controls) {
			control.setEnabled(enabled);
		}
	}

}
