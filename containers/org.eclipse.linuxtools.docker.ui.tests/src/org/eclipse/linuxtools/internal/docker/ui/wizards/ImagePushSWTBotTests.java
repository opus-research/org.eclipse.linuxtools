/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.util.concurrent.TimeUnit;

import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountManager;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountStorageManager;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockRegistryAccountFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockRegistryAccountManagerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWizardRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ComboAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;

/**
 *
 */
public class ImagePushSWTBotTests {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private DockerExplorerView dockerExplorerView;
	private SWTBotView dockerExplorerViewBot;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule();

	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Rule
	public CloseWizardRule closeWizard = new CloseWizardRule();
	private RegistryAccountStorageManager defaultRegistryAccountStorageManager;
	private DockerClient client;

	@Before
	public void lookupDockerExplorerView() {
		this.dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		this.dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(true));
		this.dockerExplorerViewBot.show();
		this.dockerExplorerViewBot.setFocus();
		this.defaultRegistryAccountStorageManager = RegistryAccountManager.getInstance().getStorageManager();
	}

	@Before
	public void setupDockerClient() {
		this.client = MockDockerClientFactory.image(MockImageFactory.name("bar:latest", "foo/bar:latest").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client)
				.withDefaultTCPConnectionSettings();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
	}

	@After
	public void restoreRegistryAccountStorageManager() {
		RegistryAccountManager.getInstance().setStorageManager(this.defaultRegistryAccountStorageManager);
	}

	@Test
	public void shouldPrefillWizardWithSelectedImageTag() {
		// given
		MockRegistryAccountManagerFactory.registryAccount(MockRegistryAccountFactory.url("http://foo.com").build())
				.build();
		// expand the 'Images' node
		SWTUtils.syncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem imageTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images", "foo/bar");

		// when opening the "Push Image..." wizard
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(imageTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Push...").click();

		// then the "Image Name" combo should have a selection
		ComboAssertion.assertThat(bot.comboBox(0)).itemSelected("foo/bar:latest");
		// and the registry should be set with the first element available, too
		ComboAssertion.assertThat(bot.comboBox(1)).itemSelected("http://foo.com");
	}

	@Test
	public void shouldPushImage() throws DockerException, InterruptedException {
		// given
		MockRegistryAccountManagerFactory.registryAccount(MockRegistryAccountFactory.url("http://foo.com").build())
				.build();
		// expand the 'Images' node
		SWTUtils.syncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem imageTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images", "foo/bar");

		// when opening the "Push Image..." wizard
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(imageTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Push...").click();

		// when click on Finish
		bot.button("Finish").click();
		// wait for the push job to complete
		// then the 'push()' method on the client should have been called
		SWTUtils.wait(1, TimeUnit.SECONDS);
		Mockito.verify(client, Mockito.times(1)).tag("foo/bar:latest", "foo.com/foo/bar:latest", false);
		Mockito.verify(client, Mockito.times(1)).push(Matchers.any(), Matchers.any());
		Mockito.verify(client, Mockito.times(1)).removeImage("foo.com/foo/bar:latest", false, false);
	}

	@Test
	public void shouldPushImageToDockerHub() throws DockerException, InterruptedException {
		// given
		MockRegistryAccountManagerFactory.registryAccount(MockRegistryAccountFactory.url("docker.io").build()).build();
		// expand the 'Images' node
		SWTUtils.syncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem imagesTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images");
		final SWTBotTreeItem imageTreeItem = SWTUtils.getTreeItem(imagesTreeItem, "foo/bar");

		// when opening the "Push Image..." wizard
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(imageTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Push...").click();

		// when click on Finish
		bot.button("Finish").click();
		// wait for the push job to complete
		// then the 'push()' method on the client should have been called
		SWTUtils.wait(1, TimeUnit.SECONDS);
		Mockito.verify(client, Mockito.times(1)).tag("foo/bar:latest", "docker.io/foo/bar:latest", false);
		Mockito.verify(client, Mockito.times(1)).push(Matchers.any(), Matchers.any());
		Mockito.verify(client, Mockito.times(1)).removeImage("docker.io/foo/bar:latest", false, false);
	}

	@Test
	public void shouldPushImageWithForceTagging() throws DockerException, InterruptedException {
		// given
		MockRegistryAccountManagerFactory.registryAccount(MockRegistryAccountFactory.url("http://foo.com").build())
				.build();
		// expand the 'Images' node
		SWTUtils.syncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem imageTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images", "foo/bar");

		// when opening the "Push Image..." wizard
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(imageTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Push...").click();
		bot.checkBox(0).select();

		// when click on Finish
		bot.button("Finish").click();
		// wait for the push job to complete
		// then the 'push()' method on the client should have been called
		SWTUtils.wait(1, TimeUnit.SECONDS);
		Mockito.verify(client, Mockito.times(1)).tag("foo/bar:latest", "foo.com/foo/bar:latest", true);
		Mockito.verify(client, Mockito.times(1)).push(Matchers.any(), Matchers.any());
		Mockito.verify(client, Mockito.times(1)).removeImage("foo.com/foo/bar:latest", false, false);
	}

	@Test
	public void shouldPushImageAndKeepTaggedImage() throws DockerException, InterruptedException {
		// given
		MockRegistryAccountManagerFactory.registryAccount(MockRegistryAccountFactory.url("http://foo.com").build())
				.build();
		// expand the 'Images' node
		SWTUtils.syncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem imageTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images", "foo/bar");

		// when opening the "Push Image..." wizard
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(imageTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Push...").click();
		bot.checkBox(1).select();

		// when click on Finish
		bot.button("Finish").click();
		// wait for the push job to complete
		// then the 'push()' method on the client should have been called
		SWTUtils.wait(1, TimeUnit.SECONDS);
		Mockito.verify(client, Mockito.times(1)).tag("foo/bar:latest", "foo.com/foo/bar:latest", false);
		Mockito.verify(client, Mockito.times(1)).push(Matchers.any(), Matchers.any());
		Mockito.verify(client, Mockito.never()).removeImage(Mockito.anyString(), Mockito.anyBoolean(),
				Mockito.anyBoolean());
	}

}
