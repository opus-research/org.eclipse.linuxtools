/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.docker.reddeer.core.ui.wizards;

import org.eclipse.linuxtools.docker.reddeer.ui.DockerExplorerView;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.jface.wizard.WizardPage;
import org.eclipse.reddeer.swt.api.Button;
import org.eclipse.reddeer.swt.api.Table;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.CheckBox;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.button.OkButton;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.button.RadioButton;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.swt.impl.toolbar.DefaultToolItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */
public class NewDockerConnectionPage extends WizardPage {
	private static final String NEW_DOCKER_CONNECTION_SHELL = "New Docker Connection";

	public NewDockerConnectionPage() {
		super(null);
	}

	public void open() {
		new DockerExplorerView().open();
		new DefaultToolItem("Add Connection").click();
		new WaitUntil(new ShellIsAvailable(NEW_DOCKER_CONNECTION_SHELL), TimePeriod.LONG);
	}

	public void finish() {
		new WaitUntil(new ShellIsAvailable(NEW_DOCKER_CONNECTION_SHELL));
		if (new FinishButton().isEnabled()) {
			new FinishButton().click();
		}

		new WaitWhile(new ShellIsAvailable(NEW_DOCKER_CONNECTION_SHELL), TimePeriod.LONG);
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
	}

	public void setConnectionName(String name) {
		new LabeledText("Connection name:").setText(name);
	}

	public void setUnixSocket(String unixSocket) {
		new CheckBox("Use custom connection settings:").toggle(true);
		new LabeledText("Location:").setText(unixSocket);
	}

	public void setTcpConnection(String uri) {
		setTcpConnection(uri, null, false);
	}

	public void setTcpConnection(String uri, String authentificationCertificatePath, boolean pingConnection) {
		setTcpUri(uri);
		if (authentificationCertificatePath != null) {
			new CheckBox("Enable authentication").toggle(true);
			new LabeledText("Path:").setText(authentificationCertificatePath);
		}
		if (pingConnection) {
			pingConnection();
		}
	}

	public void setTcpUri(String uri) {
		setConnectionName(uri);
		new CheckBox("Use custom connection settings:").toggle(true);
		new LabeledText("Location:").setText("");
		new RadioButton("TCP Connection").toggle(true);
		new LabeledText("URI:").setText(uri);
	}

	public void pingConnection() {
		Button testConnectionButton = new PushButton("Test Connection");
		testConnectionButton.click();
		new WaitUntil(new ShellIsAvailable("Success"));
		new OkButton().click();
	}

	public void search(String connectionName) {
		new PushButton("Search...").click();
		new WaitUntil(new ShellIsAvailable("Docker Connection Selection"));
		Table table = new DefaultTable();
		table.getItem(connectionName).select();
		new OkButton().click();
	}

}
