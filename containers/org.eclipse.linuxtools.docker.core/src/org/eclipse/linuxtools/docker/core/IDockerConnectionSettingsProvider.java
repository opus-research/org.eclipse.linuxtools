package org.eclipse.linuxtools.docker.core;

import java.util.List;

/**
 * @since 2.1
 */
public interface IDockerConnectionSettingsProvider {

	List<IDockerConnectionSettings> getConnectionSettings();

}
