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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.AbstractRegistry;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IRegistry;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountManager;
import org.eclipse.linuxtools.internal.docker.core.RegistryInfo;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ImagePushPage extends WizardPage {

	private final static String NAME = "ImagePush.name"; //$NON-NLS-1$
	private final static String TITLE = "ImagePush.title"; //$NON-NLS-1$
	private final static String DESC = "ImagePush.desc"; //$NON-NLS-1$
	private final static String NAME_LABEL = "ImagePushName.label"; //$NON-NLS-1$
	private final static String NAME_TOOLTIP = "ImagePushName.toolTip"; //$NON-NLS-1$
	private Text nameText;
	private Combo nameCombo;
	private Combo accountCombo;
	private IDockerImage image;

	private String tag;
	private IRegistry info;

	public ImagePushPage() {
		this(null);
	}

	public ImagePushPage(IDockerImage image) {
		super(WizardMessages.getString(NAME));
		this.image = image;
		setDescription(WizardMessages.getString(DESC));
		setTitle(WizardMessages.getString(TITLE));
		setImageDescriptor(SWTImagesFactory.DESC_WIZARD);
	}

	public String getImageTag() {
		return tag;
	}

	public IRegistry getRegistry() {
		return info;
	}

	private ModifyListener Listener = e -> validate();

	private void validate() {
		boolean complete = true;
		boolean error = false;
		String name = null;
		if (nameText != null) {
			name = nameText.getText();
		} else {
			name = nameCombo.getText();
		}
		if (accountCombo != null) {
			String account = accountCombo.getText();
			final String pattern = "(.*)@(.*)"; //$NON-NLS-1$
			Matcher m = Pattern.compile(pattern).matcher(account);
			if (m.matches()) {
				info = RegistryAccountManager.getInstance().getAccount(m.group(2), m.group(1));
			} else {
				info = new RegistryInfo(account);
			}
		} else {
			complete = false;
			error = true;
			setErrorMessage(WizardMessages.getString("ImagePushPage.empty.registry.account")); //$NON-NLS-1$
		}

		if (name.length() == 0) {
			complete = false;
		}
		if (!error) {
			setErrorMessage(null);
			tag = name;
		}

		setPageComplete(complete && !error);
	}

	@Override
	public void createControl(final Composite parent) {
		parent.setLayout(new GridLayout());
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).margins(6, 6)
				.applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(1, 1)
				.grab(true, false).applyTo(container);
		final Label nameLabel = new Label(container, SWT.NULL);
		nameLabel.setText(WizardMessages.getString(NAME_LABEL));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(nameLabel);
		if (image == null || image.repoTags().size() == 0) {
			nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
			nameText.addModifyListener(Listener);
			nameText.setToolTipText(WizardMessages.getString(NAME_TOOLTIP));
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
					.grab(true, false).span(2, 1).applyTo(nameText);
		} else {
			nameCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			nameCombo.addModifyListener(Listener);
			nameCombo.setToolTipText(WizardMessages.getString(NAME_TOOLTIP));
			nameCombo.setItems(image.repoTags().toArray(new String[0]));
			nameCombo.setText(image.repoTags().get(0));
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
					.grab(true, false).span(2, 1).applyTo(nameCombo);
		}

		final Label accountLabel = new Label(container, SWT.NULL);
		accountLabel.setText(WizardMessages.getString("ImagePushPage.registry.account.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(accountLabel);

		accountCombo = new Combo(container, SWT.DROP_DOWN);
		accountCombo.addModifyListener(Listener);
		accountCombo.setToolTipText(WizardMessages.getString("ImagePushPage.registry.account.desc")); //$NON-NLS-1$
		accountCombo.setItems(getAccountComboItems());
		if (accountCombo.getItems().length > 0) {
			accountCombo.select(0);
		}

		// Add
		final Button addButton = new Button(container, SWT.NONE);
		addButton.setText(
				WizardMessages.getString("ImagePullPushPage.add.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(addButton);
		addButton.addSelectionListener(onAdd(accountCombo));

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(accountCombo);
		setControl(container);
		validate();
	}

	private SelectionListener onAdd(Combo combo) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				String selected = combo.getText();
				RegistryAccountDialog dialog = new RegistryAccountDialog(
						getShell(),
						WizardMessages.getString(
								"ImagePullPushPage.login.title"), //$NON-NLS-1$
						AbstractRegistry.DOCKERHUB_REGISTRY,
						WizardMessages.getString(
								"RegistryAccountDialog.add.explanation")); ///$NON-NLS-1$
				if (dialog.open() == Window.OK) {
					IRegistryAccount acc = dialog.getSignonInformation();
					RegistryAccountManager.getInstance().add(acc);
					selected = acc.getUsername() + "@" + acc.getServerAddress(); //$NON-NLS-1$
				}
				combo.setItems(getAccountComboItems());
				combo.setText(selected);
			}
		};
	}

	private String[] getAccountComboItems() {
		List<String> items = RegistryAccountManager.getInstance().getAccounts()
				.stream().map(e -> e.getUsername() + "@" + e.getServerAddress()) //$NON-NLS-1$
				.collect(Collectors.toList());
		return items.toArray(new String[0]);
	}

}
