package org.eclipse.linuxtools.systemtap.ui.tests;

import java.io.IOException;

import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;

/**
 * Generic utilities for systemtap tests.
 */
public class SystemtapTestUtil {
	/**
	 * Check that stap is installed
	 * 
	 * @return true if stap is installed, false otherwise.
	 */
	public static boolean stapInstalled() {
		try {
			Process process = RuntimeProcessFactory.getFactory().exec(
					new String[] { "stap", "-V" }, null);
			return (process != null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
