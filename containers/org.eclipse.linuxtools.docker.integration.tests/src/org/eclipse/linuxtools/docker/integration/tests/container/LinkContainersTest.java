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

package org.eclipse.linuxtools.docker.integration.tests.container;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockDockerConnectionManager;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockDockerTerminal;
import org.eclipse.linuxtools.docker.reddeer.condition.ContainerIsDeployedCondition;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunSelectionPage;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerTerminal;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageInfoFactory;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.eclipse.condition.ConsoleHasNoChange;
import org.jboss.reddeer.eclipse.ui.views.properties.PropertiesView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageInfo;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class LinkContainersTest extends AbstractImageBotTest {

	private static final String IMAGE_ALPINE_CURL = "byrnedo/alpine-curl";
	private static final String CONTAINER_NAME_HTTP_SERVER = "test_run_httpd";
	private static final String CONTAINER_NAME_CLIENT_ALPINE = "test_connect_httpd";

	private DockerClient client;
	private Container createdContainer;
	private ContainerInfo containerInfo;

	@Before
	public void before() throws DockerException, InterruptedException {
		if (mockitoIsUsed()) {
			setUpForMockito();
		} else {
			pullImage(IMAGE_ALPINE_CURL);
			pullImage(IMAGE_UHTTPD);
		}
	}

	private ImageRunSelectionPage openImageRunSelectionPage(String containerName, boolean publishAllExposedPorts) {
		ImageRunSelectionPage page = new ImageRunSelectionPage();
		page.setContainerName(containerName);
		page.setPublishAllExposedPorts(publishAllExposedPorts);
		return page;
	}

	@Test
	public void testLinkContainers() {
		runUhttpServer(IMAGE_UHTTPD, CONTAINER_NAME_HTTP_SERVER);
		runAlpineLinux(IMAGE_ALPINE_CURL, CONTAINER_NAME_CLIENT_ALPINE);

	}

	public void runUhttpServer(String imageName, String containerName) {
		DockerImagesTab imagesTab = openDockerImagesTab();
		imagesTab.runImage(imageName);
		ImageRunSelectionPage firstPage = openImageRunSelectionPage(containerName, false);
		firstPage.setContainerName(containerName);
		firstPage.setPublishAllExposedPorts(false);
		firstPage.finish();
		if (mockitoIsUsed()) {
			mockServerContainer();
		} else {
			new WaitWhile(new JobIsRunning());
			new WaitWhile(new ConsoleHasNoChange());
		}
		new WaitUntil(new ContainerIsDeployedCondition(containerName, getConnection()));
	}

	public void runAlpineLinux(String imageName, String containerName) {
		String serverAddress = getHttpServerAddress(CONTAINER_NAME_HTTP_SERVER);
		DockerImagesTab imagesTab = openDockerImagesTab();
		imagesTab.runImage(imageName);
		ImageRunSelectionPage firstPage = openImageRunSelectionPage(containerName, false);
		firstPage.setContainerName(containerName);
		firstPage.setCommand(serverAddress + ":80");
		firstPage.addLinkToContainer(CONTAINER_NAME_HTTP_SERVER, "http_server");
		firstPage.setPublishAllExposedPorts(false);
		firstPage.setAllocatePseudoTTY();
		firstPage.setKeepSTDINOpen();
		firstPage.finish();
		new WaitWhile(new JobIsRunning());
		DockerTerminal dt = new DockerTerminal();
		dt.open();
		if (mockitoIsUsed()) {
			mockServerContainer();
			dt = MockDockerTerminal.setText("text").build();
		}
		String terminalText = dt.getTextFromPage("/" + containerName);
		assertTrue("No output from terminal!", !terminalText.isEmpty());
		assertTrue("Containers are not linked!", !terminalText.contains("Connection refused"));
	}

	private String getHttpServerAddress(String containerName) {
		PropertiesView propertiesView = new PropertiesView();
		propertiesView.open();
		getConnection().getContainer(containerName).select();
		propertiesView.selectTab("Inspect");
		return propertiesView.getProperty("NetworkSettings", "IPAddress").getPropertyValue();
	}

	@After
	public void after() {
		deleteContainerIfExists(CONTAINER_NAME_CLIENT_ALPINE);
		deleteContainerIfExists(CONTAINER_NAME_HTTP_SERVER);
		deleteImageIfExists(IMAGE_ALPINE_CURL);
	}

	private void setUpForMockito() throws DockerException, InterruptedException {
		// images to use
		final Image imageUhttpd = MockImageFactory.id("1a2b3c4d5e6f7g").name(IMAGE_UHTTPD + ":" + IMAGE_TAG_LATEST)
				.build();
		final ImageInfo imageInfoUhttpd = MockImageInfoFactory.volume("/foo/bar")
				.command(Arrays.asList("the", "command")).entrypoint(Arrays.asList("the", "entrypoint")).build();
		final Image imageAlpine = MockImageFactory.id("1a2b3c4d5e6f7g").name(IMAGE_ALPINE_CURL + ":" + IMAGE_TAG_LATEST)
				.build();
		final ImageInfo imageInfoAlpine = MockImageInfoFactory.volume("/foo/bar")
				.command(Arrays.asList("the", "command")).entrypoint(Arrays.asList("the", "entrypoint")).build();
		this.client = MockDockerClientFactory.image(imageUhttpd, imageInfoUhttpd).image(imageAlpine, imageInfoUhttpd)
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from(DEFAULT_CONNECTION_NAME, client)
				.withDefaultTCPConnectionSettings();
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);
	}

	private void mockServerContainer() {
		ContainerInfo containerInfo = MockContainerInfoFactory.link(IMAGE_ALPINE).id("TestServerTestServer")
				.ipAddress("127.0.0.1").build();
		Container createdContainer = MockContainerFactory.id("1MockContainer").name(CONTAINER_NAME_HTTP_SERVER)
				.imageName("1a2b3c4d5e6f7g").status("Started 1 second ago").build();
		MockDockerClientFactory.addContainer(this.client, createdContainer, containerInfo);
	}
}