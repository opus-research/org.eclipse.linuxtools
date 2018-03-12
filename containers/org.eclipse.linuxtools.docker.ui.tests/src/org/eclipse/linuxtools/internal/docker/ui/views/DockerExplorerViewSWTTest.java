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

package org.eclipse.linuxtools.internal.docker.ui.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionStorageManager;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionStorageManagerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerExplorerViewAssertion;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testing the {@link DockerExplorerView} {@link Viewer}
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class DockerExplorerViewSWTTest {

	private static SWTWorkbenchBot bot;
	private SWTBotView dockerExplorerViewBot;
	private DockerExplorerView dockerExplorerView;

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		bot.viewByTitle("Welcome").close();
		bot.perspectiveById("org.eclipse.linuxtools.docker.ui.perspective").activate();
	}

	@Before
	public void lookupDockerExplorerView() {
		bot.views().stream()
				.filter(v -> v.getReference().getId().equals("org.eclipse.linuxtools.docker.ui.dockerContainersView")
						|| v.getReference().getId().equals("org.eclipse.linuxtools.docker.ui.dockerImagesView"))
				.forEach(v -> v.close());
		dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		dockerExplorerViewBot.show();
		dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(false));
	}

	
	@AfterClass
	public static void closeWizard() {
		DockerConnectionManager.getInstance().setConnectionStorageManager(new DefaultDockerConnectionStorageManager());
	}

	private void configureConnectionManager(final IDockerConnection... connections) {
		DockerConnectionManager.getInstance()
				.setConnectionStorageManager(MockDockerConnectionStorageManagerFactory.load(connections));
		Display.getDefault().syncExec(() -> DockerConnectionManager.getInstance().reloadConnections());
	}

	@Test
	public void shouldDisplayExplanationPane() {
		// given
		configureConnectionManager();
		// when
		dockerExplorerViewBot.getToolbarButtons().get(1).click();
		// then
		DockerExplorerViewAssertion.assertThat(dockerExplorerView).isEmpty();
	}

	@Test
	public void shouldDisplayConnectionsPane() {
		// given
		configureConnectionManager(MockDockerConnectionFactory.noImageNoContainer("Empty"));
		// when
		dockerExplorerViewBot.getToolbarButtons().get(1).click();
		// then
		DockerExplorerViewAssertion.assertThat(dockerExplorerView).isNotEmpty();
	}

}
