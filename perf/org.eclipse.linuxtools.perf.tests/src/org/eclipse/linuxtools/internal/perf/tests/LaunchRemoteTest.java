/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Rodrigo Fraxino De Araujo <rfaraujo@br.ibm.com>
 *******************************************************************************/

package org.eclipse.linuxtools.internal.perf.tests;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.remote.launch.PerfEventsTab;
import org.eclipse.linuxtools.internal.perf.remote.launch.PerfLaunchConfigDelegate;
import org.eclipse.linuxtools.internal.perf.remote.launch.PerfOptionsTab;
import org.eclipse.linuxtools.profiling.tests.AbstractRemoteTest;
import org.osgi.framework.FrameworkUtil;

public class LaunchRemoteTest extends AbstractRemoteTest {

	protected ILaunchConfiguration config;
	protected PerfLaunchConfigDelegate delegate;
	protected ILaunch launch;
	protected ILaunchConfigurationWorkingCopy wc;
	private IProject project;

	private final String CONNECTION_NAME = "localhost"; //$NON-NLS-1$
	private final String CONNECTION_DIR = "/tmp/eclipse-perf-ext_project_test/"; //$NON-NLS-1$
	private final String EXTERNAL_PROJECT_PATH = "remotetools://"+ CONNECTION_NAME + CONNECTION_DIR; //$NON-NLS-1$
	private final String PROJECT_NAME = "fibTest"; //$NON-NLS-1$
	private final String SOURCE_FILE = "fib.cpp"; //$NON-NLS-1$

	@Override
	protected void setUp() throws Exception {
		if ((!(AbstractRemoteTest.USERNAME.equals("")))) {
			project = createRemoteExternalProjectAndBuild(FrameworkUtil.getBundle(this.getClass()),
					PROJECT_NAME, EXTERNAL_PROJECT_PATH, SOURCE_FILE);

			config = createConfiguration(project);
			delegate = new PerfLaunchConfigDelegate();
			launch = new Launch(config, ILaunchManager.PROFILE_MODE, null);
			wc = config.getWorkingCopy();
			setProfileAttributes(wc);
		}
	}

	@Override
	protected void tearDown() {
		if (!(AbstractRemoteTest.USERNAME.equals(""))) 
			deleteResource(CONNECTION_DIR);
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(PerfPlugin.LAUNCHCONF_ID);
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
		PerfEventsTab eventsTab = new PerfEventsTab();
		PerfOptionsTab optionsTab = new PerfOptionsTab();
		wc.setAttribute(PerfPlugin.ATTR_SourceLineNumbers, false);
		eventsTab.setDefaults(wc);
		optionsTab.setDefaults(wc);
	}

	public void testDefaultRun () {
		if (!(AbstractRemoteTest.USERNAME.equals(""))) {
			try {
				delegate.launch(wc, ILaunchManager.PROFILE_MODE, launch, null);
			} catch (Exception e) {
				e.printStackTrace();
				fail();
			}
		}
	}

	public void testClockEventRun () {
		if (!(AbstractRemoteTest.USERNAME.equals(""))) {
			try {
				ArrayList<String> list = new ArrayList<String>();
				list.addAll(Arrays.asList(new String [] {"cpu-clock", "task-clock", "cycles"}));
				wc.setAttribute(PerfPlugin.ATTR_DefaultEvent, false);
				wc.setAttribute(PerfPlugin.ATTR_SelectedEvents, list);
				delegate.launch(wc, ILaunchManager.PROFILE_MODE, launch, null);
			} catch (Exception e) {
				fail();
			}
		}
	}

}
