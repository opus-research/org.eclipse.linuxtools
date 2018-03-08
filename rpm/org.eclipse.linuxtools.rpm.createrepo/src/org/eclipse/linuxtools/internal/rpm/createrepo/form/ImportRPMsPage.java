/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo.form;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.menus.IMenuService;

/**
 * This page will allow the user to import RPMs from either the
 * file system or the workspace. The RPMs imported will be the
 * RPMs used when executing the createrepo command.
 */
public class ImportRPMsPage extends FormPage {

	private CreaterepoProject project;

	private FormToolkit toolkit;
	private ScrolledForm form;

	private Composite buttonList;
	private Tree tree;

	private static final String MENU_URI = "toolbar:formsToolbar"; //$NON-NLS-1$
	private static final String HEADER_ICON = "/icons/repository_rep.gif"; //$NON-NLS-1$

	/**
	 * Default constructor.
	 *
	 * @param editor The editor.
	 * @param project The project.
	 */
	public ImportRPMsPage(FormEditor editor, CreaterepoProject project) {
		super(editor, Messages.ImportRPMsPage_title, Messages.ImportRPMsPage_title);
		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		// setting up the form page
		super.createFormContent(managedForm);
		GridLayout layout = new GridLayout();
		GridData data = new GridData();
		toolkit = managedForm.getToolkit();
		form = managedForm.getForm();
		form.setText(Messages.ImportRPMsPage_formHeaderText);
		form.setImage(Activator.getImageDescriptor(HEADER_ICON).createImage());
		ToolBarManager toolbarManager = (ToolBarManager) form.getToolBarManager();
		toolkit.decorateFormHeading(form.getForm());

		// add the menuContribution from MANIFEST.MF to the form
		IMenuService menuService = (IMenuService) getSite().getService(IMenuService.class);
		menuService.populateContributionManager(toolbarManager, MENU_URI);
		toolbarManager.update(true);

		layout = new GridLayout(2, true);
		layout.marginWidth = 6; layout.marginHeight = 12;
		form.getBody().setLayout(layout);

		// Section and its client area to manage importing the RPMs
		Section rpmSection = toolkit.createSection(form.getBody(), Section.DESCRIPTION
				| ExpandableComposite.TITLE_BAR);
		layout = new GridLayout();
		rpmSection.setText(Messages.ImportRPMsPage_sectionTitle);
		rpmSection.setDescription(Messages.ImportRPMsPage_sectionInstruction);
		rpmSection.setLayoutData(expandComposite());

		// the client area containing the tree + buttons
		Composite sectionClient = toolkit.createComposite(rpmSection);
		layout = new GridLayout(2, false);
		layout.marginWidth = 1; layout.marginHeight = 7;
		sectionClient.setLayout(layout);
		tree = toolkit.createTree(sectionClient, SWT.BORDER | SWT.MULTI | SWT.HORIZONTAL
				| SWT.VERTICAL | SWT.LEFT_TO_RIGHT | SWT.SMOOTH);
		tree.setLayoutData(expandComposite());

		buttonList = toolkit.createComposite(sectionClient);
		layout = new GridLayout();
		data = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
		layout.marginWidth = 0; layout.marginHeight = 0;
		buttonList.setLayout(layout);
		buttonList.setLayoutData(data);

		createPushButton(buttonList, Messages.ImportRPMsPage_buttonImportRPMs,
				toolkit).addSelectionListener(new ImportButtonListener());
		createPushButton(buttonList, Messages.ImportRPMsPage_buttonRemoveRPMs,
				toolkit).addSelectionListener(new RemoveButtonListener());
		createSpace(buttonList);

		createPushButton(buttonList, Messages.ImportRPMsPage_buttonCreateRepo,
				toolkit).addSelectionListener(new CreaterepoButtonListener());

		refreshTree();
		rpmSection.setClient(sectionClient);
		managedForm.refresh();
	}

	/**
	 * Make a GridData that expands to fill both horizontally
	 * and vertically.
	 *
	 * @return The created GridData.
	 */
	private static GridData expandComposite() {
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;
		data.grabExcessHorizontalSpace = true;
		return data;
	}

	/**
	 * Create a push style button.
	 *
	 * @param parent The parent the button will belong to.
	 * @param buttonText The text show on the button.
	 * @param toolkit The form toolkit used in creating a button.
	 * @return The button created.
	 */
	private Button createPushButton(Composite parent, String buttonText, FormToolkit toolkit) {
		Button button = toolkit.createButton(parent, buttonText, SWT.PUSH | SWT.FLAT
				| SWT.CENTER | SWT.LEFT_TO_RIGHT);
		button.setFont(parent.getFont());
		GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		button.setLayoutData(gd);
		return button;
	}

	/**
	 * Create space between composites, such as buttons within a button list.
	 *
	 * @param parent The composite to attach a space to.
	 */
	private void createSpace(Composite parent) {
		new Label(buttonList, SWT.NONE).setLayoutData(new GridData(0,0));
	}

	/**
	 * On creating the form content the tree will be populated with
	 * RPMs found in the root of the current project.
	 *
	 * @throws CoreException Thrown when getting rpms from project fails.
	 */
	private void refreshTree() {
		tree.removeAll();
		try {
			for (IResource rpm : project.getRPMs()) {
				addItemToTree(rpm.getName());
			}
		} catch (CoreException e) {
			Activator.logError(Messages.ImportRPMsPage_errorRefreshingTree, e);
		}
		tree.setFocus();
	}

	/**
	 * Add a new item to the tree if it does not yet exist. A null or empty
	 * string will be ignored.
	 *
	 * @param itemName The name of the new item.
	 * @return True if it does not exist and has been added, false otherwise.
	 */
	private boolean addItemToTree(String itemName) {
		boolean exists = false;
		if (itemName == null || itemName.isEmpty())
			return false;
		// check to see if the tree item exists in the tree
		if (tree.getItemCount() > 0) {
			for (TreeItem item : tree.getItems()) {
				if (item.getText().equals(itemName)) {
					exists = true;
				}
			}
		}
		// if the tree item doesnt exists or the tree is empty
		if (!exists || tree.getItemCount() == 0) {
			TreeItem treeItem = new TreeItem(tree, SWT.NONE);
			treeItem.setText(itemName);
			return true;
		}
		return false;
	}

	/**
	 * Handle the import button execution on the Import RPMs page.
	 */
	public class ImportButtonListener extends SelectionAdapter {
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) { }

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent e) { }
	}

	/**
	 * Handle the remove button execution on the Import RPMs page.
	 */
	public class RemoveButtonListener extends SelectionAdapter {
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) { }

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent e) { }
	}

	/**
	 * Handle the createrepo button execution on the Import RPMs page.
	 */
	public class CreaterepoButtonListener extends SelectionAdapter {
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) { }

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent e) { }
	}

}
