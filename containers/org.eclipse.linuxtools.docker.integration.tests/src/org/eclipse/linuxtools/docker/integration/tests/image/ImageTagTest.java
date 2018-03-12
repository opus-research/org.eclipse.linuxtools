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

package org.eclipse.linuxtools.docker.integration.tests.image;

import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.docker.integration.tests.mock.MockUtils;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerExplorerView;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.jboss.reddeer.common.exception.WaitTimeoutExpiredException;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.swt.impl.button.CancelButton;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author jkopriva@redhat.com
 * @contributor adietish@redhat.com
 *
 */

public class ImageTagTest extends AbstractImageBotTest {

	private static final String IMAGE_NAME = IMAGE_BUSYBOX;
	private static final String IMAGE_NAME_TO_PULL = IMAGE_BUSYBOX_LATEST;
	private static final String IMAGE_TAG = "testtag";
	private static final String IMAGE_TAG_UPPERCASE = "UPPERCASETAG";

	@Before
	public void before() {
		deleteAllConnections();
		if (!mockitoIsUsed()) {
			deleteImageIfExists(IMAGE_NAME);
			pullImage(IMAGE_NAME_TO_PULL);
		} else {
			MockUtils.pullImage(DEFAULT_CONNECTION_NAME, IMAGE_NAME, IMAGE_TAG_LATEST);
		}
		new WaitWhile(new JobIsRunning());
		assertTrue("Image has not been deployed!", imageIsDeployed(IMAGE_NAME));
	}

	@Test
	public void testAddRemoveTagToImage() {
		DockerImagesTab imagesTab = openDockerImagesTab();
		imagesTab.addTagToImage(IMAGE_NAME, IMAGE_TAG);
		if (mockitoIsUsed()) {
			MockUtils.pullImage(DEFAULT_CONNECTION_NAME, IMAGE_NAME, IMAGE_TAG);
		}
		new WaitWhile(new JobIsRunning());
		assertTrue("Image tag has not been added", imagesTab.getImageTags(IMAGE_NAME).contains(IMAGE_TAG));
		imagesTab.activate();
		if (mockitoIsUsed()) {
			MockUtils.pullImage(DEFAULT_CONNECTION_NAME, IMAGE_NAME, IMAGE_TAG_LATEST);
		} else {
			imagesTab.removeTagFromImage(IMAGE_NAME, IMAGE_TAG);
		}
		new WaitWhile(new JobIsRunning());
		assertTrue("ImageTaghasNotBeenRemoved", !imagesTab.getImageTags(IMAGE_NAME).contains(IMAGE_TAG));
	}

	/**
	 * Tries to add an uppercase tag to an image. This errors in docker daemon
	 * >= 1.11 while it succeeds in older versions.
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=509223
	 */
	@Test
	public void testAddUpperCaseTagToImage() {
		DockerExplorerView explorer = new DockerExplorerView();
		explorer.open();

		// DockerImagesTab imagesTab = openDockerImagesTab();
		try {
			explorer.getDockerConnectionByName(DEFAULT_CONNECTION_NAME).getImage(IMAGE_NAME)
					.addTagToImage(IMAGE_TAG_UPPERCASE);

			// imagesTab.addTagToImage(IMAGE_NAME, IMAGE_TAG_UPPERCASE);
		} catch (WaitTimeoutExpiredException ex) {
			new CancelButton().click();
			// swallowing, it is not possible to tag image with upper case
		}
	}

	@After
	public void after() {
		deleteImageContainerAfter(IMAGE_NAME);
		cleanUpWorkspace();
	}
}