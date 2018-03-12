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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerExplorerViewAssertion;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTBotTreeItemAssertions;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.TestLoggerRule;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.spotify.docker.client.DockerClient;

/**
 * Testing the {@link DockerExplorerView} {@link Viewer}
 */
@RunWith(SWTBotJunit4ClassRunner.class) 
public class DockerExplorerViewSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private SWTBotView dockerExplorerViewBot;
	private DockerExplorerView dockerExplorerView;
	private SWTBotTree dockerExplorerViewTreeBot;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(); 
	
	@Rule
	public TestLoggerRule watcher = new TestLoggerRule();
	
	@Rule
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();
	
	@Before
	public void setup() {
		this.bot = new SWTWorkbenchBot();
		SWTUtils.asyncExec(() -> {try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Failed to open Docker Explorer view: " + e.getMessage());
		}});
		this.dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		this.dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(true));
		this.bot.views().stream()
				.filter(v -> v.getReference().getId().equals("org.eclipse.linuxtools.docker.ui.dockerContainersView")
						|| v.getReference().getId().equals("org.eclipse.linuxtools.docker.ui.dockerImagesView"))
				.forEach(v -> v.close());
	}

	@Test
	public void shouldDisplayExplanationPane() {
		// given
		DockerConnectionManagerUtils.configureConnectionManager();
		// then
		DockerExplorerViewAssertion.assertThat(dockerExplorerView).isEmpty();
	}

	@Test
	public void shouldDisplayConnectionsPane() {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// then
		DockerExplorerViewAssertion.assertThat(dockerExplorerView).isNotEmpty();
	}

	@Test
	public void shouldRefreshImagesAndShowChanges() {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem[] allItems = dockerExplorerViewBot.bot().tree().getAllItems();
		Assertions.assertThat(allItems).hasSize(1);
		Assertions.assertThat(allItems[0].getItems()).hasSize(2);
		final SWTBotTreeItem imagesTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images");
		imagesTreeItem.expand();
		Conditions.waitForJobs(DockerExplorerView.class, "Docker Explorer View jobs");
		Assertions.assertThat(imagesTreeItem.getItems().length).isEqualTo(0);
		
		// update the client 
		final DockerClient updatedClient = MockDockerClientFactory.image(MockDockerImageFactory.name("foo/bar").build())
				.build();
		dockerConnection.setClient(updatedClient);
		// when locating the 'Images' node and hit refresh
		dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(imagesTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Refresh").click();
		SWTUtils.wait(2, TimeUnit.SECONDS);
		imagesTreeItem.expand();
		Conditions.waitForJobs(DockerExplorerView.class, "Docker Explorer View jobs");
		// then check that there are images now
		Assertions.assertThat(imagesTreeItem.isExpanded()).isTrue();
		Assertions.assertThat(imagesTreeItem.getItems().length).isEqualTo(1);
	}

	@Test
	public void shouldRefreshContainersAndShowChanges() {
		// given
		final DockerClient client = MockDockerClientFactory.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		Conditions.waitForJobs(DockerExplorerView.class, "Docker Explorer View jobs");
		final SWTBotTreeItem[] allItems = dockerExplorerViewBot.bot().tree().getAllItems();
		// one connection
		Assertions.assertThat(allItems).hasSize(1);
		// "containers" and "images" items
		Assertions.assertThat(allItems[0].getItems()).hasSize(2);
		final SWTBotTreeItem containersTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Containers");
		containersTreeItem.expand();
		Assertions.assertThat(containersTreeItem.getItems().length).isEqualTo(0);
		
		// update the client 
		final DockerClient updatedClient = MockDockerClientFactory.container(MockDockerContainerFactory.name("foo_bar").build()).build();
		dockerConnection.setClient(updatedClient);
		dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(containersTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Refresh").click();
		SWTUtils.asyncExec(() -> containersTreeItem.expand());

		// then check that there are images now
		Assertions.assertThat(containersTreeItem.isExpanded()).isTrue();
		Assertions.assertThat(containersTreeItem.getItems().length).isEqualTo(1);
	}

	@Test
	public void shouldShowContainerPortMapping() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build(), MockDockerContainerInfoFactory
						.port("8080/tcp", "0.0.0.0", "8080").port("8787/tcp", "0.0.0.0", "8787").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// when a second call to expand the container is done (because the first
		// expandAll stopped with a "Loading..." job that retrieved the
		// containers)
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers", "foo_bar");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		final SWTBotTreeItem containerPortsTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers", "foo_bar", "Ports");
		SWTUtils.asyncExec(() -> containerPortsTreeItem.expand());
		// then
		SWTUtils.syncAssert(() -> {
			SWTBotTreeItemAssertions.assertThat(containerPortsTreeItem).isExpanded().hasChildItems(2);
			SWTBotTreeItemAssertions.assertThat(containerPortsTreeItem.getNode(0))
					.hasText("0.0.0.0:8080 -> 8080 (tcp)");
			SWTBotTreeItemAssertions.assertThat(containerPortsTreeItem.getNode(1))
					.hasText("0.0.0.0:8787 -> 8787 (tcp)");
		});
	}

	@Test
	public void shouldShowContainerLinks() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build(), MockDockerContainerInfoFactory
						.link("/postgres-demo:/foo_bar/postgres1").link("/postgres-demo:/foo_bar/postgres2").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// when a second call to expand the container is done (because the first
		// expandAll stopped with a "Loading..." job that retrieved the
		// containers)
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers", "foo_bar");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		final SWTBotTreeItem containerLinksTreeItem = SWTUtils.getTreeItem(containerTreeItem, "Links");
		SWTUtils.asyncExec(() -> containerLinksTreeItem.expand());
		// then
		SWTUtils.syncAssert(() -> {
			SWTBotTreeItemAssertions.assertThat(containerLinksTreeItem).isExpanded().hasChildItems(2);
			SWTBotTreeItemAssertions.assertThat(containerLinksTreeItem.getNode(0)).hasText("postgres-demo (postgres1)");
			SWTBotTreeItemAssertions.assertThat(containerLinksTreeItem.getNode(1)).hasText("postgres-demo (postgres2)");
		});
	}

	@Test
	public void shouldShowContainerVolumes() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build(),
						MockDockerContainerInfoFactory.volume("/path/to/container")
								.volume("/path/to/host:/path/to/container")
								.volume("/path/to/host:/path/to/container:Z,ro").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// when a second call to expand the container is done (because the first
		// expandAll stopped with a "Loading..." job that retrieved the
		// containers)
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers", "foo_bar");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		final SWTBotTreeItem containerVolumesItem = SWTUtils.getTreeItem(containerTreeItem, "Volumes");
		SWTUtils.asyncExec(() -> containerVolumesItem.expand());
		// then
		SWTUtils.syncAssert(() -> {
			SWTBotTreeItemAssertions.assertThat(containerVolumesItem).isExpanded().hasChildItems(3);
			SWTBotTreeItemAssertions.assertThat(containerVolumesItem.getNode(0)).hasText("/path/to/container");
			SWTBotTreeItemAssertions.assertThat(containerVolumesItem.getNode(1))
					.hasText("/path/to/host -> /path/to/container");
			SWTBotTreeItemAssertions.assertThat(containerVolumesItem.getNode(2))
					.hasText("/path/to/host -> /path/to/container (Z,ro)");
		});
	}

	@Test
	public void shouldRemainExpandedAfterRefresh() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("foo_bar").build(),
						MockDockerContainerInfoFactory.volume("/path/to/container")
								.port("8080/tcp", "0.0.0.0", "8080")
								.link("/foo:/bar/foo")
								.volume("/path/to/host:/path/to/container:Z,ro").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem containersTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test",
				"Containers");
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(containersTreeItem, "foo_bar");
		SWTUtils.asyncExec(() -> containerTreeItem.expand());
		SWTUtils.asyncExec(() -> SWTUtils.getTreeItem(containerTreeItem, "Links").expand());
		SWTUtils.asyncExec(() -> SWTUtils.getTreeItem(containerTreeItem, "Ports").expand());
		SWTUtils.asyncExec(() -> SWTUtils.getTreeItem(containerTreeItem, "Volumes").expand());
		// ensure items are actually expanded before calling the 'refresh' command
		SWTUtils.syncAssert(() -> {
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Links")).isExpanded();
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Ports")).isExpanded();
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Volumes")).isExpanded();
		});
		// when refreshing the container
		dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(containersTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Refresh").click();
		SWTUtils.asyncExec(() -> containersTreeItem.expand());
		// then all items should remain expanded (after they were reloaded)
		SWTUtils.syncAssert(() -> {
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Links")).isExpanded();
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Ports")).isExpanded();
			SWTBotTreeItemAssertions.assertThat(SWTUtils.getTreeItem(containerTreeItem, "Volumes")).isExpanded();
		});
	}

	@Test
	public void shouldProvideEnabledStartCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Stopped").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectMultipleContainersInTreeView();
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Start");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(true);
	}

	@Test
	public void shouldProvideDisabledStartCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Stopped").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectMultipleContainersInTreeView();
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Start");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(false);
	}
	
	@Test
	public void shouldProvideEnabledStopCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectMultipleContainersInTreeView();
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Stop");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(true);
	}

	@Test
	public void shouldProvideDisabledStopCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectMultipleContainersInTreeView();
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Stop");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(false);
	}
	
	@Test
	public void shouldProvideEnabledPauseCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectMultipleContainersInTreeView();
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Pause");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(true);
	}

	@Test
	public void shouldProvideDisabledPauseCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Up (Paused)").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectMultipleContainersInTreeView();
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Pause");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(false);
	}
	
	@Test
	public void shouldProvideEnabledUnpauseCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Up (Paused)").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Up (Paused)").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectMultipleContainersInTreeView();
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Unpause");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(true);
	}

	@Test
	public void shouldProvideDisabledUnpauseCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Up (Paused)").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectMultipleContainersInTreeView();
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Unpause");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(false);
	}
	
	@Test
	public void shouldProvideEnabledKillCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Running").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectMultipleContainersInTreeView();
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Kill");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(true);
	}

	@Test
	public void shouldProvideDisabledKillCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectMultipleContainersInTreeView();
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Kill");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(false);
	}
	
	@Test
	public void shouldProvideEnabledRemoveCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Stopped").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectMultipleContainersInTreeView();
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Remove");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(true);
	}

	@Test
	public void shouldProvideRemoveCommandOnMultipleContainersAtOnce() {
		// given
		final DockerClient client = MockDockerClientFactory
				.container(MockDockerContainerFactory.name("gentle_foo").status("Running").build())
				.container(MockDockerContainerFactory.name("angry_bar").status("Stopped").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// open the context menu on one of the containers
		selectMultipleContainersInTreeView();
		final SWTBotMenu menuCommand = dockerExplorerViewBot.bot().tree().contextMenu("Remove");
		// then
		assertThat(menuCommand.isVisible()).isEqualTo(true);
		assertThat(menuCommand.isEnabled()).isEqualTo(false);
	}
	
	private SWTBotTreeItem selectMultipleContainersInTreeView() {
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		// when a second call to expand the container is done (because the first
		// expandAll stopped with a "Loading..." job that retrieved the
		// containers)
		final SWTBotTreeItem containersTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Containers");
		SWTUtils.asyncExec(() -> containersTreeItem.expand());
		// select both containers
		SWTUtils.select(containersTreeItem, "gentle_foo", "angry_bar");
		final SWTBotTreeItem containerTreeItem = SWTUtils.getTreeItem(containersTreeItem, "gentle_foo");
		return containerTreeItem;
	}

	@Test
	public void shouldShowAllImageVariants() {
		// given
		final DockerClient client = MockDockerClientFactory.image(MockDockerImageFactory.id("1a2b3c4d5e6f7g")
				.name("foo:1.0", "foo:latest", "bar:1.0", "bar:latest").build()).build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		SWTUtils.asyncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem imagesTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test (null)",
				"Images");
		// when
		SWTUtils.asyncExec(() -> imagesTreeItem.expand());
		// then 2 images should be displayed
		SWTUtils.syncAssert(() -> {
			final SWTBotTreeItem[] images = imagesTreeItem.getItems();
			assertThat(images).hasSize(2);
			assertThat(images[0].getText()).startsWith("bar: 1.0, latest");
			assertThat(images[1].getText()).startsWith("foo: 1.0, latest");
		});
	}

}
