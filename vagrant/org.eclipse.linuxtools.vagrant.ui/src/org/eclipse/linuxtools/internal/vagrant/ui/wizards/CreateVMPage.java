/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.vagrant.ui.wizards;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.vagrant.ui.SWTImagesFactory;
import org.eclipse.linuxtools.vagrant.core.IVagrantBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CreateVMPage extends WizardPage {

	private final CreateVMPageModel model;
	private final DataBindingContext dbc;
	private boolean isCustomVMFileSelected = false;

	public CreateVMPage(IVagrantBox box) {
		super("createVMPage", //$NON-NLS-1$
				WizardMessages.getString("CreateVM.label"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage(WizardMessages.getString("CreateVM.desc")); //$NON-NLS-1$
		this.model = new CreateVMPageModel();
		this.model.setBoxName(box.getName());
		this.dbc = new DataBindingContext();
	}

	@Override
	public void dispose() {
		dbc.dispose();
		super.dispose();
	}

	public String getVMName() {
		return isCustomVMFileSelected ? null : this.model.getVMName();
	}

	public String getBoxName() {
		return isCustomVMFileSelected ? null : this.model.getBoxName();
	}

	public String getVMFile() {
		return isCustomVMFileSelected ? this.model.getVMFile() : null;
	}

	@Override
	public void createControl(Composite parent) {
		parent.setLayout(new GridLayout());
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).margins(6, 6)
				.applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(1, 1)
				.grab(true, false).applyTo(container);

		// VM Name
		final Label vmNameLabel = new Label(container, SWT.NONE);
		vmNameLabel
				.setText(WizardMessages.getString("CreateVM.name.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(vmNameLabel);

		final Text vmNameText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(2, 1).applyTo(vmNameText);
		vmNameText.setToolTipText(
				WizardMessages.getString("CreateVM.name.tooltip")); //$NON-NLS-1$
		// VM Name binding
		final IObservableValue vmmNameObservable = BeanProperties
				.value(CreateVMPageModel.class, CreateVMPageModel.VM_NAME)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(vmNameText),
				vmmNameObservable, new UpdateValueStrategy(), null);

		// Box name
		final Label boxNameLabel = new Label(container, SWT.NONE);
		boxNameLabel
				.setText(WizardMessages.getString("CreateVM.boxName.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(boxNameLabel);

		final Text boxNameText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(2, 1).applyTo(boxNameText);
		boxNameText.setToolTipText(
				WizardMessages.getString("CreateVM.boxName.tooltip")); //$NON-NLS-1$
		// Box Name binding
		final IObservableValue boxNameObservable = BeanProperties
				.value(CreateVMPageModel.class, CreateVMPageModel.BOX_NAME)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(boxNameText),
				boxNameObservable, new UpdateValueStrategy(), null);

		// VM File Checkbox
		final Button customVMFileButton = new Button(container, SWT.CHECK);
		customVMFileButton
				.setText(WizardMessages.getString("CreateVM.File.CheckBox")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(3, 1).applyTo(customVMFileButton);

		// VM File
		final Label boxLocLabel = new Label(container, SWT.NONE);
		boxLocLabel
				.setText(WizardMessages.getString("CreateVM.loc.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(boxLocLabel);

		final Text boxLocText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(boxLocText);
		boxLocText.setToolTipText(
				WizardMessages.getString("CreateVM.loc.tooltip")); //$NON-NLS-1$
		boxLocText.setEnabled(false);
		// Location binding
		final IObservableValue imgeNameObservable = BeanProperties
				.value(CreateVMPageModel.class, CreateVMPageModel.VM_FILE)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(boxLocText),
				imgeNameObservable, new UpdateValueStrategy(), null);

		// search
		final Button searchButton = new Button(container, SWT.NONE);
		searchButton
				.setText(WizardMessages.getString("CreateVM.search.label")); //$NON-NLS-1$
		searchButton.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(searchButton);
		searchButton.addSelectionListener(onSearchImage());

		customVMFileButton.addSelectionListener(
				onCheckCustomVMFile(vmNameText, boxNameText, boxLocText, searchButton));

		// setup validation support
		WizardPageSupport.create(this, dbc);
		setControl(container);
	}

	private SelectionListener onCheckCustomVMFile(Text vmNameText,
			Text boxNameText, Text boxLocText, Button searchButton) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (e.widget instanceof Button) {
					Button bt = (Button) e.widget;
					if (bt.getSelection()) {
						vmNameText.setEnabled(false);
						boxNameText.setEnabled(false);
						searchButton.setEnabled(true);
						boxLocText.setEnabled(true);
						isCustomVMFileSelected = true;
					} else {
						vmNameText.setEnabled(true);
						boxNameText.setEnabled(true);
						searchButton.setEnabled(false);
						boxLocText.setEnabled(false);
						isCustomVMFileSelected = false;
					}
				}
			}
		};
	}

	/**
	 * Opens the {@link ImageSearch} dialog with current image name pre-filled.
	 * 
	 * @return
	 */
	private SelectionListener onSearchImage() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				FileDialog fd = new FileDialog(getShell());
				String location = fd.open();
				if (location != null && !location.isEmpty()) {
					model.setVMFile(location);
				}
			}
		};
	}

}
