package org.eclipse.linuxtools.profiling.time;

import org.eclipse.linuxtools.internal.profiling.provider.AbstractProviderPreferencesPage;

/**
 * The preferences page for this plug-in, contributing to the global profiling
 * preference page.
 *
 */
public class TimePreferencesPage extends AbstractProviderPreferencesPage {

	@Override
	protected String getProfilingType() {
		return TimeProviderPlugin.PROFILING_TYPE;
	}
}
