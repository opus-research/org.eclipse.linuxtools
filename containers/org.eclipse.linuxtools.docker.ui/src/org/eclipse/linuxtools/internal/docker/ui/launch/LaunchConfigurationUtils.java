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

package org.eclipse.linuxtools.internal.docker.ui.launch;

import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.ALLOCATE_PSEUDO_CONSOLE;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.AUTO_REMOVE;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.COMMAND;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.CONTAINER_NAME;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.CPU_PRIORITY;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.CREATION_DATE;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.DATA_VOLUMES;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.ENABLE_LIMITS;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.ENTRYPOINT;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.ENV_VARIABLES;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.IMAGE_ID;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.IMAGE_NAME;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.INTERACTIVE;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.LINKS;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.MEMORY_LIMIT;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.PUBLISHED_PORTS;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.PUBLISH_ALL_PORTS;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.wizards.DataVolumeModel;

/**
 * Utility class to manage {@link ILaunchConfiguration}
 */
public class LaunchConfigurationUtils {

	public static final String RUN_IMAGE_CONFIGURATION_TYPE = "org.eclipse.linuxtools.docker.ui.runDockerImageLaunchConfigurationType"; //$NON-NLS-1$

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"YYYY-MM-dd HH:mm:ss");

	/**
	 * Private constructor for this utility class.
	 */
	private LaunchConfigurationUtils() {
		// empty
	}
	
	/**
	 * @return the ILaunchConfigurationType for the given configuration type.
	 * @param configType
	 *            the id of the configuration type
	 */
	public static ILaunchConfigurationType getLaunchConfigType(
			final String configType) {
		return DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(configType);
	}

	/**
	 * Creates a new {@link ILaunchConfiguration} for the given
	 * {@link IDockerContainer}.
	 * 
	 * @param image
	 *            the {@link IDockerImage} used to create the container
	 * @param containerName
	 *            the actual container name (given by the user or generated by
	 *            the Docker daemon)
	 * @param containerConfig
	 * @param hostConfig
	 *            the user-provided {@link IDockerHostConfig} (created
	 *            container's one)
	 * @param removeWhenExits
	 *            flag to indicate if container should be removed when exited
	 * @return the generated {@link ILaunchConfiguration}
	 * 
	 */
	public static ILaunchConfiguration createLaunchConfiguration(
			final IDockerImage image,
			final IDockerContainerConfig containerConfig,
			final IDockerHostConfig hostConfig, final String containerName,
			final boolean removeWhenExits) {
		try {
			final ILaunchManager manager = DebugPlugin.getDefault()
					.getLaunchManager();
			final String configurationName = manager
					.generateLaunchConfigurationName(containerName);
			final ILaunchConfigurationType type = manager
					.getLaunchConfigurationType(RUN_IMAGE_CONFIGURATION_TYPE);
			final ILaunchConfigurationWorkingCopy workingCopy = type
					.newInstance(null, configurationName);
			workingCopy.setAttribute(CREATION_DATE,
					DATE_FORMAT.format(new Date()));
			workingCopy.setAttribute(CONNECTION_NAME,
					image.getConnection().getName());
			workingCopy.setAttribute(IMAGE_ID, image.id());
			workingCopy.setAttribute(IMAGE_NAME, image.repoTags().get(0));
			workingCopy.setAttribute(CONTAINER_NAME, containerName);
			workingCopy.setAttribute(COMMAND, toString(containerConfig.cmd()));
			workingCopy.setAttribute(ENTRYPOINT,
					toString(containerConfig.entrypoint()));
			// selected ports
			workingCopy.setAttribute(PUBLISH_ALL_PORTS,
					hostConfig.publishAllPorts());
			// format: <containerPort><type>:<hostIP>:<hostPort>
			if (hostConfig.publishAllPorts()) {
				final IDockerImageInfo imageInfo = image.getConnection()
						.getImageInfo(image.id());
				workingCopy.setAttribute(PUBLISHED_PORTS, serializePortBindings(
						imageInfo.containerConfig().exposedPorts()));
			} else {
				workingCopy.setAttribute(PUBLISHED_PORTS,
						serializePortBindings(hostConfig.portBindings()));
			}
			// links (with format being: "<containerName>:<containerAlias>")
			workingCopy.setAttribute(LINKS, hostConfig.links());
			// env variables
			workingCopy.setAttribute(ENV_VARIABLES, containerConfig.env());
			// volumes
			final List<String> volumes = new ArrayList<>();
			// volumes from other containers
			for (String volumeFrom : hostConfig.volumesFrom()) {
				final DataVolumeModel volume = DataVolumeModel
						.parseVolumeFrom(volumeFrom);
				if (volume != null) {
					volumes.add(volume.toString());
				}
			}
			// bindings to host directory or file
			for (String bind : hostConfig.binds()) {
				final DataVolumeModel volume = DataVolumeModel
						.parseHostBinding(bind);
				if (volume != null) {
					volumes.add(volume.toString());
				}
			}
			// TODO: container path declaration
			
			workingCopy.setAttribute(DATA_VOLUMES, volumes);
			// options
			workingCopy.setAttribute(AUTO_REMOVE, removeWhenExits);
			workingCopy.setAttribute(ALLOCATE_PSEUDO_CONSOLE,
					containerConfig.tty());
			workingCopy.setAttribute(INTERACTIVE,
					containerConfig.attachStdin());
			// resources limitations
			if (containerConfig.memory() != null) {
				workingCopy.setAttribute(ENABLE_LIMITS, true);
				workingCopy.setAttribute(MEMORY_LIMIT,
						containerConfig.memory().toString());
			}
			if (containerConfig.cpuShares() != null) {
				workingCopy.setAttribute(ENABLE_LIMITS, true);
				workingCopy.setAttribute(CPU_PRIORITY,
						containerConfig.cpuShares().toString());
			}
			return workingCopy.doSave();
		} catch (CoreException e) {
			Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					LaunchMessages.getString(
							"RunDockerImageLaunchConfiguration.creation.failure"), //$NON-NLS-1$
					e));
		}
		return null;
	}

	private static String toString(final List<String> input) {
		final StringBuilder command = new StringBuilder();
		for (Iterator<String> iterator = input.iterator(); iterator
				.hasNext();) {
			String fragment = iterator.next();
			command.append(fragment);
			if (iterator.hasNext()) {
				command.append(" ");
			}
		}
		return command.toString();
	}

	/**
	 * Serializes the given Port Bindings to save them in an
	 * {@link ILaunchConfiguration}
	 * 
	 * @param bindings
	 * @return a {@link List} of port bindings serialized in the following
	 *         format:
	 *         <code>&lt;containerPort&gt;&lt;type&gt;:&lt;hostIP&gt;:&lt;hostPort&gt;</code>
	 *         Note that the <code>&lt;hostIP&gt;</code> part may be empty if
	 *         undefined by the user.
	 */
	public static List<String> serializePortBindings(
			final Map<String, List<IDockerPortBinding>> bindings) {
		final List<String> serializedBindings = new ArrayList<>();
		if (bindings != null) {
			for (Entry<String, List<IDockerPortBinding>> entry : bindings
					.entrySet()) {
				for (IDockerPortBinding portBinding : entry.getValue()) {
					final StringBuilder portBindingBuilder = new StringBuilder();
					portBindingBuilder.append(entry.getKey());
					portBindingBuilder.append(':'); // $NON-NLS-1$
					if (portBinding.hostIp() != null) {
						portBindingBuilder.append(portBinding.hostIp());
					}
					portBindingBuilder.append(':'); // $NON-NLS-1$
					portBindingBuilder.append(portBinding.hostPort());
					serializedBindings.add(portBindingBuilder.toString());
				}
			}
		}
		return serializedBindings;
	}

	/**
	 * Serializes the given Port Bindings to save them in an
	 * {@link ILaunchConfiguration}
	 * 
	 * @param bindings
	 * @return a {@link List} of port bindings serialized in the following
	 *         format:
	 *         <code>&lt;containerPort&gt;:&lt;type&gt;:&lt;hostIP&gt;:&lt;hostPort&gt;</code>
	 *         Note that the <code>&lt;hostIP&gt;</code> part may be empty if
	 *         undefined by the user.
	 */
	public static List<String> serializePortBindings(
			final Set<String> bindings) {
		final List<String> serializedBindings = new ArrayList<>();
		if (bindings != null) {
			for (String portBinding : bindings) {
				final StringBuilder portBindingBuilder = new StringBuilder();
				portBindingBuilder.append(portBinding);
				portBindingBuilder.append(':'); // $NON-NLS-1$
				portBindingBuilder.append(':'); // $NON-NLS-1$
				final String[] containerPort = portBinding.split("/"); // $NON-NLS-1$
				portBindingBuilder.append(containerPort[0]);
				serializedBindings.add(portBindingBuilder.toString());
			}
		}
		return serializedBindings;
	}

	/**
	 * Looks-up the {@link ILaunchConfiguration} with the given type and
	 * <strong>IDockerImage's name</strong>.
	 * 
	 * @param type
	 *            the configuration type
	 * @param imageName
	 *            the associated {@link IDockerImage} name
	 * @return the first matching {@link ILaunchConfiguration} or
	 *         <code>null</code> if none was found.
	 * @throws CoreException
	 */
	public static ILaunchConfiguration getLaunchConfigurationByImageName(
			final ILaunchConfigurationType type, final String imageName)
					throws CoreException {
		final ILaunchManager manager = DebugPlugin.getDefault()
				.getLaunchManager();
		ILaunchConfiguration lastLaunchConfiguration = null;
		String lastCreationDate = ""; //$NON-NLS-1$
		for (ILaunchConfiguration launchConfiguration : manager
				.getLaunchConfigurations(type)) {
			final String launchConfigImageName = launchConfiguration
					.getAttribute(IMAGE_NAME, ""); //$NON-NLS-1$
			final String launchConfigCreationDate = launchConfiguration
					.getAttribute(CREATION_DATE, ""); //$NON-NLS-1$
			if (launchConfigImageName.equals(imageName)
					&& launchConfigCreationDate
							.compareTo(lastCreationDate) > 0) {
				lastCreationDate = launchConfigCreationDate;
				lastLaunchConfiguration = launchConfiguration;
			}
		}
		return lastLaunchConfiguration;
	}
	
	/**
	 * Looks-up the {@link ILaunchConfiguration} with the given type and
	 * <strong>name</strong>.
	 * 
	 * @param type
	 *            the configuration type
	 * @param name
	 *            the name
	 * @return the first matching {@link ILaunchConfiguration} or
	 *         <code>null</code> if none was found.
	 * @throws CoreException
	 */
	public static ILaunchConfiguration getLaunchConfigurationByName(
			final ILaunchConfigurationType type, final String name)
					throws CoreException {
		final ILaunchManager manager = DebugPlugin.getDefault()
				.getLaunchManager();
		for (ILaunchConfiguration launchConfiguration : manager
				.getLaunchConfigurations(type)) {
			final String launchConfigName = launchConfiguration.getName(); // $NON-NLS-1$
			if (launchConfigName.equals(name)) {
				return launchConfiguration;
			}
		}
		return null;
	}

	public static String convertToUnixPath(String path) {
		String unixPath = path;

		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			// replace backslashes with slashes
			unixPath = unixPath.replaceAll("\\\\", "/");

			// replace "C:/" with "/c/"
			Matcher m = Pattern.compile("([a-zA-Z]):/").matcher(unixPath);
			if (m.find()) {
				StringBuffer b = new StringBuffer();
				b.append('/');
				m.appendReplacement(b, m.group(1).toLowerCase());
				b.append('/');
				m.appendTail(b);
				unixPath = b.toString();
			}
		}

		return unixPath;
	}

}
