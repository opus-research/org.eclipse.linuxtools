/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;

/**
 * Databinding model for the {@link ImageRunNetworkPage}
 * 
 * @author jjohnstn
 *
 */
public class ImageRunNetworkModel extends BaseDatabindingModel {

	public static final String NETWORK_MODE = "networkMode"; //$NON-NLS-1$

	public static final String DEFAULT_MODE = ""; //$NON-NLS-1$

	public static final String BRIDGE_MODE = "bridge"; //$NON-NLS-1$

	public static final String HOST_MODE = "host"; //$NON-NLS-1$

	public static final String NONE_MODE = "none"; //$NON-NLS-1$

	public static final String OTHER_MODE = "other";

	public static final String CONTAINER_MODE = "container"; //$NON-NLS-1$

	public static final String SELECTED_CONTAINER = "selectedContainer"; //$NON-NLS-1$

	public static final String OTHER_STRING = "otherString";

	private String networkMode;

	private final IDockerConnection connection;

	private String selectedContainer;

	private String otherString;

	private List<String> containerNames = new ArrayList<>();

	public ImageRunNetworkModel(
			final IDockerConnection connection) {
		this.connection = connection;
		this.networkMode = DEFAULT_MODE;
		refreshContainerNames();
	}

	public ImageRunNetworkModel(final IDockerImage selectedImage) {
		this.connection = selectedImage.getConnection();
		this.networkMode = DEFAULT_MODE;
		refreshContainerNames();
	}

	public void refreshContainerNames() {
		final List<String> refreshedContainerNames = new ArrayList<>();
		final IDockerConnection connection = this.connection;
		if (connection != null && connection.isOpen()) {
			connection.getContainers().stream()
					.filter(container -> !container.status().contains("Stopped")
							&& !container.status().contains("Paused"))
					.forEach(container -> {
						refreshedContainerNames.add(container.name());
					});
			Collections.sort(refreshedContainerNames);
		}
		containerNames = refreshedContainerNames;
	}

	public String getNetworkMode() {
		return networkMode;
	}

	public void setNetworkMode(final String networkMode) {
		firePropertyChange(NETWORK_MODE, this.networkMode,
				this.networkMode = networkMode);
	}

	public String getSelectedContainer() {
		return selectedContainer;
	}

	public void setSelectedContainer(String selectedContainer) {
		firePropertyChange(SELECTED_CONTAINER, this.selectedContainer,
				this.selectedContainer = selectedContainer);
	}

	public String getOtherString() {
		return otherString;
	}

	public void setOtherString(String otherString) {
		firePropertyChange(OTHER_STRING, this.otherString,
				this.otherString = otherString);
	}

	public List<String> getContainerNames() {
		return containerNames;
	}

}
