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

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.AbstractRegistry;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IRegistry;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountManager;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

/**
 * {@link WizardPage} to push an image to a registry.
 */
public class ImagePushPage extends WizardPage {

	private final static String NAME = "ImagePush.name"; //$NON-NLS-1$
	private final static String TITLE = "ImagePush.title"; //$NON-NLS-1$
	private final static String NAME_LABEL = "ImagePushName.label"; //$NON-NLS-1$
	private final static String NAME_TOOLTIP = "ImagePushName.toolTip"; //$NON-NLS-1$

	// private final IDockerImage image;
	// private String tag;
	// private IRegistry info;

	private final DataBindingContext dbc;
	private final ImagePushModel model;

	/**
	 * Constructor
	 * 
	 * @param image
	 *            the {@link IDockerImage} to push.
	 * @param selectedImageName
	 *            the default image name/tag
	 */
	public ImagePushPage(final IDockerImage image,
			final String selectedImageName) {
		super(WizardMessages.getString(NAME));
		this.model = new ImagePushModel(image, selectedImageName);
		this.dbc = new DataBindingContext();
		setTitle(WizardMessages.getString(TITLE));
		setImageDescriptor(SWTImagesFactory.DESC_WIZARD);
	}

	@Override
	public void dispose() {
		this.dbc.dispose();
		super.dispose();
	}

	/**
	 * @return the tag to select/apply on the image
	 */
	public String getSelectedImageName() {
		return this.model.getSelectedImageName();
	}

	/**
	 * @return the target {@link IRegistry} on which to push the image
	 */
	public IRegistry getSelectedRegistryAccount() {
		return this.model.getSelectedRegistryAccount();
	}

	/**
	 * @return flag to indicate if the 'force' option should be used when
	 *         tagging the image.
	 */
	public boolean isForceTagging() {
		return model.isForceTagging();
	}

	/**
	 * @return flag to indicate if the tagged image should be kept upon
	 *         completion.
	 */
	public boolean isKeepTaggedImage() {
		return model.isKeepTaggedImage();
	}


	@SuppressWarnings("unchecked")
	@Override
	public void createControl(final Composite parent) {
		parent.setLayout(new GridLayout());
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(6, 6)
				.applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(container);
		final Label nameLabel = new Label(container, SWT.NULL);
		nameLabel.setText(WizardMessages.getString(NAME_LABEL));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(nameLabel);
		// repo/name/tag for the selected image
		final Combo imageNameCombo = new Combo(container, SWT.DROP_DOWN);
		imageNameCombo.setToolTipText(WizardMessages.getString(NAME_TOOLTIP));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(imageNameCombo);
		final ComboViewer imageNameComboViewer = new ComboViewer(
				imageNameCombo);
		imageNameComboViewer.setContentProvider(new ArrayContentProvider());
		imageNameComboViewer.setInput(this.model.getImage().repoTags());
		// binding must take place after the input is set, so that default
		// repo/name can be selected.
		final IObservableValue<String> imageNameObservable = BeanProperties
				.value(ImagePushModel.class, ImagePushModel.SELECTED_IMAGE_NAME)
				.observe(model);
		dbc.bindValue(WidgetProperties.selection().observe(imageNameCombo),
				imageNameObservable);

		// registry selection
		final Label accountLabel = new Label(container, SWT.NULL);
		accountLabel.setText(WizardMessages.getString("ImagePushPage.registry.account.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(accountLabel);
		final Combo registryAccountCombo = new Combo(container,
				SWT.DROP_DOWN | SWT.READ_ONLY);
		registryAccountCombo.setToolTipText(WizardMessages
				.getString("ImagePushPage.registry.account.desc")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(registryAccountCombo);
		final ComboViewer registryAccountComboViewer = new ComboViewer(
				registryAccountCombo);
		registryAccountComboViewer
				.setContentProvider(ArrayContentProvider.getInstance());
		registryAccountComboViewer
				.setLabelProvider(new RegistryAccountLabelProvider());
		registryAccountComboViewer
				.setInput(RegistryAccountManager.getInstance().getAccounts());
		final IObservableValue<IRegistryAccount> registryAccountObservable = BeanProperties
				.value(ImagePushModel.class,
						ImagePushModel.SELECTED_REGISTRY_ACCOUNT)
				.observe(model);
		dbc.bindValue(
				ViewerProperties.singleSelection()
						.observe(registryAccountComboViewer),
				registryAccountObservable);

		// link to configure registries and accounts
		final Link configureRegistriesLink = new Link(container, SWT.NONE);
		configureRegistriesLink.setText(
				WizardMessages.getString("ImagePullPushPage.browse.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER)
				.grab(true, false).span(2, 1).applyTo(configureRegistriesLink);
		configureRegistriesLink
				.addSelectionListener(
						onAddRegistry(registryAccountComboViewer));

		// force tagging
		final Button forceTaggingButton = new Button(container, SWT.CHECK);
		forceTaggingButton.setText(WizardMessages
				.getString("ImagePushPage.forcetagging.label")); //$NON-NLS-1$ );
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(2, 1).applyTo(forceTaggingButton);
		dbc.bindValue(WidgetProperties.selection().observe(forceTaggingButton),
				BeanProperties.value(ImagePushModel.class,
						ImagePushModel.FORCE_TAGGING).observe(model));

		// keep tagged image upon completion
		final Button keepTaggedImageButton = new Button(container, SWT.CHECK);
		keepTaggedImageButton.setText(WizardMessages
				.getString("ImagePushPage.keeptaggedimage.label")); //$NON-NLS-1$ );
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(2, 1).applyTo(keepTaggedImageButton);
		dbc.bindValue(
				WidgetProperties.selection().observe(keepTaggedImageButton),
				BeanProperties
						.value(ImagePushModel.class,
								ImagePushModel.KEEP_TAGGED_IMAGE)
						.observe(model));

		// setup validation support
		WizardPageSupport.create(this, dbc);
		dbc.addValidationStatusProvider(model.new ImagePushValidator(
				imageNameObservable, registryAccountObservable));
		setControl(container);
	}

	private SelectionListener onAddRegistry(
			final ComboViewer registryAccountComboViewer) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final RegistryAccountDialog dialog = new RegistryAccountDialog(
						getShell(),
						WizardMessages
								.getString("ImagePullPushPage.login.title"), //$NON-NLS-1$
						AbstractRegistry.DOCKERHUB_REGISTRY,
						WizardMessages.getString(
								"RegistryAccountDialog.add.explanation")); ///$NON-NLS-1$
				if (dialog.open() == Window.OK) {
					final List<IRegistryAccount> updatedRegistryAccounts = RegistryAccountManager
							.getInstance().getAccounts();
					registryAccountComboViewer
							.setInput(updatedRegistryAccounts);
					// reset selection in combo if current selection is gone
					if (!updatedRegistryAccounts
							.contains(model.getSelectedRegistryAccount())) {
						model.setSelectedRegistryAccount(null);
					}
				}
			}
		};
	}

	private static final class RegistryAccountLabelProvider
			extends ColumnLabelProvider {
	
		@Override
		public String getText(Object element) {
			if (element instanceof IRegistryAccount) {
				final IRegistryAccount registryAccount = (IRegistryAccount) element;
				final StringBuilder textBuilder = new StringBuilder();
				// only display account username if it is set.
				if (registryAccount.getUsername() != null) {
					textBuilder.append(registryAccount.getUsername())
							.append('@'); // $NON-NLS-1$
				}
				textBuilder.append(registryAccount.getServerAddress());
				return textBuilder.toString();
			}
			return null;
		}
	}

}
