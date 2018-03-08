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

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.linuxtools.internal.rpm.createrepo.Activator;
import org.eclipse.linuxtools.internal.rpm.createrepo.Messages;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.menus.IMenuService;

/**
 * This page will allow the user to view/edit some of the repo
 * xml metadata (i.e., repo, revision, etc.).
 *
 * This will look into the existing repomd.xml and get transferrable information
 * and place it onto the forms, otherwise the forms will be empty.
 *
 * Executing the createrepo command would also use the data in this form. This
 * will be the main "control center" of the plugin (i.e., would also have a switch
 * to toggle whether or not user wants to update the repo, or allow the user
 * to specfiy where to output the repodata directory (? maybe)).
 */
public class MainOverviewPage extends FormPage {

	private FormToolkit toolkit;
	private ScrolledForm form;

	private static final String MENU_URI = "toolbar:formsToolbar"; 	//$NON-NLS-1$
	private static final String HEADER_ICON = "/icons/repository_rep.gif"; //$NON-NLS-1$

	/** Default constructor. */
	public MainOverviewPage(FormEditor editor) {
		super(editor, Messages.MainOverviewPage_title, Messages.MainOverviewPage_title);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		toolkit = managedForm.getToolkit();
		form = managedForm.getForm();
		form.setText(Messages.MainOverviewPage_formHeaderText);
		toolkit = managedForm.getToolkit();
		form = managedForm.getForm();
		form.setText(Messages.MainOverviewPage_formHeaderText);
		form.setImage(Activator.getImageDescriptor(HEADER_ICON).createImage());
		ToolBarManager toolbarManager = (ToolBarManager) form.getToolBarManager();
		toolkit.decorateFormHeading(form.getForm());

		// add the menuContribution from MANIFEST.MF to the form
		IMenuService menuService = (IMenuService) getSite().getService(IMenuService.class);
		menuService.populateContributionManager(toolbarManager, MENU_URI);
		toolbarManager.update(true);
	}

}
