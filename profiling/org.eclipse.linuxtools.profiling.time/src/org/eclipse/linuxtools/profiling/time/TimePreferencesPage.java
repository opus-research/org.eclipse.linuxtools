package org.eclipse.linuxtools.profiling.time;

import org.eclipse.linuxtools.internal.profiling.provider.AbstractProviderPreferencesPage;

public class TimePreferencesPage extends AbstractProviderPreferencesPage {

	@Override
	protected String getProfilingType() {
		return TimeProviderPlugin.PROFILING_TYPE;
	}
}
