/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockDockerConnectionManager;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunResourceVolumesVariablesPage;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunSelectionPage;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerContainersTab;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageInfoFactory;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
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

public class LabelsTest extends AbstractImageBotTest {

	private static final String CONTAINER_LABEL_VALUE = "bar";
	private static final String CONTAINER_LABEL_KEY = "foo";
	private static final String IMAGE_NAME = IMAGE_BUSYBOX;
	private static final String IMAGE_TAG = "latest";
	private static final String CONTAINER_NAME = "test_run_busybox_label";
	
	private DockerClient client;
	private Container createdContainer;
	private ContainerInfo containerInfo;
	

	@Before
	public void before() throws DockerException, InterruptedException {
		if(!mockitoIsUsed()){
			pullImage(IMAGE_NAME, IMAGE_TAG);
		} else {
			setUpForMockito();
		}
	}

	@Test
	public void testLabels() {
		DockerImagesTab imagesTab = openDockerImagesTab();
		imagesTab.runImage(IMAGE_NAME + ":" + IMAGE_TAG);

		ImageRunSelectionPage firstPage = new ImageRunSelectionPage();
		firstPage.setContainerName(CONTAINER_NAME);
		firstPage.setAllocatePseudoTTY();
		firstPage.setKeepSTDINOpen();
		firstPage.setGiveExtendedPrivileges();
		firstPage.next();
		ImageRunResourceVolumesVariablesPage secondPage = new ImageRunResourceVolumesVariablesPage();
		secondPage.addLabel(CONTAINER_LABEL_KEY, CONTAINER_LABEL_VALUE);
		secondPage.finish();
		new WaitWhile(new JobIsRunning());
		if (mockitoIsUsed()) {
			MockDockerClientFactory.addContainer(this.client, this.createdContainer, this.containerInfo);
		}
		DockerContainersTab containerTab = new DockerContainersTab();
		containerTab.searchContainer(CONTAINER_NAME);
		containerTab.select(CONTAINER_NAME);
		PropertiesView propertiesView = new PropertiesView();
		propertiesView.open();
		getConnection().getContainer(CONTAINER_NAME).select();
		propertiesView.selectTab("Inspect");
		String labelProp = propertiesView.getProperty("Config", "Labels", CONTAINER_LABEL_KEY).getPropertyValue();
		assertTrue("Container does not have label " + CONTAINER_LABEL_KEY + "!",
				labelProp.equals(CONTAINER_LABEL_VALUE));
	}

	@After
	public void after() {
		killRunningImageJobs();
		deleteContainerIfExists(CONTAINER_NAME);
	}
	
	@SuppressWarnings("restriction")
	private void setUpForMockito () throws DockerException, InterruptedException {
		Map<String, String> labels = new HashMap<String, String>();
		labels.put(CONTAINER_LABEL_KEY,CONTAINER_LABEL_VALUE);
		// images to use
		final Image image = MockImageFactory.id("1a2b3c4d5e6f7g").name(IMAGE_NAME + ":" + IMAGE_TAG_LATEST).build();
		final ImageInfo imageInfo = MockImageInfoFactory.volume("/foo/bar")
				.command(Arrays.asList("the", "command")).build();
		// container to be created
		this.createdContainer = MockContainerFactory.id("1MockContainer").name(CONTAINER_NAME)
				.imageName("1a2b3c4d5e6f7g").status("Started 1 second ago").build();
		this.containerInfo = MockContainerInfoFactory.privilegedMode(true).id("TestTestTestTestTest").ipAddress("127.0.0.1").labels(labels).build();
		this.client = MockDockerClientFactory.image(image, imageInfo)
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from(DEFAULT_CONNECTION_NAME, client).withDefaultTCPConnectionSettings();
		// configure the Connection Manager
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);

	}
	
}