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
package org.eclipse.linuxtools.internal.rpm.createrepo;

import org.eclipse.osgi.util.NLS;

/**
 * Messages displayed across the plugin.
 */
public final class Messages {

	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.rpm.createrepo.messages"; //$NON-NLS-1$

	// CreaterepoWizard
	/****/
	public static String CreaterepoWizard_errorCreatingProject;
	/****/
	public static String CreaterepoWizard_openFileOnCreation;
	/****/
	public static String CreaterepoWizard_errorOpeningNewlyCreatedFile;
	/****/
	public static String CreaterepoWizard_errorCreatingFolder;

	// CreaterepoNewProjectWizardPage
	/****/
	public static String CreaterepoNewProjectWizardPage_wizardPageName;
	/****/
	public static String CreaterepoNewProjectWizardPage_wizardPageTitle;
	/****/
	public static String CreaterepoNewProjectWizardPage_wizardPageDescription;

	// CreaterepoImportRPMWizardPage
	/****/
	public static String CreaterepoImportRPMWizardPage_wizardPageName;
	/****/
	public static String CreaterepoImportRPMWizardPage_wizardPageTitle;
	/****/
	public static String CreaterepoImportRPMWizardPage_wizardPageDescription;

	// CreaterepoProjectCreator
	/****/
	public static String CreaterepoProjectCreator_errorSettingProjectLocation;

	// RepoMetadataFormEditor
	/****/
	public static String RepoMetadataFormEditor_errorInitializingForm;
	/****/
	public static String RepoMetadataFormEditor_errorInitializingProject;

	// ImportRPMsPage
	/****/
	public static String ImportRPMsPage_title;
	/****/
	public static String ImportRPMsPage_formHeaderText;
	/****/
	public static String ImportRPMsPage_sectionTitle;
	/****/
	public static String ImportRPMsPage_sectionInstruction;
	/****/
	public static String ImportRPMsPage_buttonImportRPMs;
	/****/
	public static String ImportRPMsPage_buttonRemoveRPMs;
	/****/
	public static String ImportRPMsPage_buttonDeleteRPMs;
	/****/
	public static String ImportRPMsPage_buttonCreateRepo;
	/****/
	public static String ImportRPMsPage_errorPopulatingTree;
	/****/
	public static String ImportRPMsPage_errorAcceptingVisitor;

	// ImportRPMsPage$ImportButtonListener
	/****/
	public static String ImportButtonListener_dialogImportTitle;
	/****/
	public static String ImportButtonListener_error;
	/****/
	public static String ImportButtonListener_fileExistsAlready;
	/****/
	public static String ImportButtonListener_errorSavingPkgList;

	// ImportRPMsPage$DeleteButtonListener
	/****/
	public static String DeleteButtonListener_error;
	/****/
	public static String DeleteButtonListener_dialogDeleteTitle;
	/****/
	public static String DeleteButtonListener_dialogDeleteMessage;

	// MainOverviewPage
	/****/
	public static String MainOverviewPage_title;
	/****/
	public static String MainOverviewPage_formHeaderText;

	// CreaterepoProject
	/****/
	public static String CreaterepoProject_executeCreaterepo;
	/****/
	public static String CreaterepoProject_consoleName;
	/****/
	public static String CreaterepoProject_errorExecuting;
	/****/
	public static String CreaterepoProject_errorSettingPreferences;
	/****/
	public static String CreaterepoProject_errorGettingFile;

	// Createrepo
	/****/
	public static String Createrepo_jobName;

	// CreaterepoResourceChangeListener
	/****/
	public static String CreaterepoResourceChangeListener_errorGettingResource;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
