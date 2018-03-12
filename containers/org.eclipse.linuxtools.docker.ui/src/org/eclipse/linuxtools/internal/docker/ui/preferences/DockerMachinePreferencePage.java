/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DockerMachinePreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	/**
	 * The {@link DirectoryFieldEditor} to select the installation directory for
	 * the Docker-Machine command.
	 */
	private DirectoryFieldEditor dockerMachineInstallDir;

	/**
	 * The {@link DirectoryFieldEditor} to select the installation directory for
	 * the underlying VM driver commands used by 'docker-machine'.
	 */
	private DirectoryFieldEditor vmDriverInstallDir;

	public DockerMachinePreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("DockerMachine.message")); //$NON-NLS-1$
	}

	@Override
	public void init(final IWorkbench workbench) {
	}

	/**
	 * Saves the current values in the {@link PreferenceStore}.
	 */
	private void savePreferences() {
		if (this.dockerMachineInstallDir != null) {
			this.dockerMachineInstallDir.store();
		}
		if (this.vmDriverInstallDir != null) {
			this.vmDriverInstallDir.store();
		}
	}

	@Override
	public boolean performOk() {
		savePreferences();
		return true;
	}

	@Override
	protected void performApply() {
		savePreferences();
		super.performApply();
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
	}

	@Override
	protected void createFieldEditors() {
		// installation directory for docker-machine
		this.dockerMachineInstallDir = new DirectoryFieldEditor(
				PreferenceConstants.DOCKER_MACHINE_INSTALLATION_DIRECTORY,
				Messages.getString("DockerMachinePath.label"), //$NON-NLS-1$
				getFieldEditorParent());
		this.dockerMachineInstallDir.setPreferenceStore(getPreferenceStore());
		// allow empty value if docker-machine is not installed
		this.dockerMachineInstallDir.setEmptyStringAllowed(true);
		this.dockerMachineInstallDir
				.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		this.dockerMachineInstallDir.setPage(this);
		this.dockerMachineInstallDir.setErrorMessage(
				Messages.getString("DockerMachinePath.invalid.label")); //$NON-NLS-1$
		this.dockerMachineInstallDir.showErrorMessage();
		this.dockerMachineInstallDir.load();
		addField(this.dockerMachineInstallDir);
		// installation directory for underlying VM driver
		this.vmDriverInstallDir = new DirectoryFieldEditor(
				PreferenceConstants.VM_DRIVER_INSTALLATION_DIRECTORY,
				Messages.getString("VMDriverPath.label"), //$NON-NLS-1$
				getFieldEditorParent());
		this.vmDriverInstallDir.setPreferenceStore(getPreferenceStore());
		// allow empty value if docker-machine is not installed
		this.vmDriverInstallDir.setEmptyStringAllowed(true);
		this.vmDriverInstallDir
				.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		this.vmDriverInstallDir.setPage(this);
		this.vmDriverInstallDir.setErrorMessage(
				Messages.getString("VMDriverPath.invalid.label")); //$NON-NLS-1$
		this.vmDriverInstallDir.showErrorMessage();
		this.vmDriverInstallDir.load();
		addField(this.vmDriverInstallDir);

	}

}