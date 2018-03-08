/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *    Severin Gehwolf <sgehwolf@redhat.com> - moved to separate class
 *******************************************************************************/

package org.eclipse.linuxtools.oprofile.launch.tests.utils;

import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.launching.OprofileLaunchConfigurationDelegate;

/**
 * Helper delegate class
 * 
 * @author Red Hat Inc.
 *
 */
public final class TestingOprofileLaunchConfigurationDelegate extends OprofileLaunchConfigurationDelegate {
	public boolean eventsIsNull;
	public OprofileDaemonOptions _options;  
	@Override
	protected void oprofileDumpSamples() { return; }
	@Override
	protected void oprofileReset() { return; }
	@Override
	protected void oprofileShutdown() { return; }
	@Override
	protected boolean oprofileStatus() { return true; }
	@Override
	protected void oprofileStartCollection() { return; }
	@Override
	protected void oprofileSetupDaemon(OprofileDaemonOptions options, OprofileDaemonEvent[] events) { 
		_options = options; 
		eventsIsNull = events == null ? true : false; 
		return; 
	}
	@Override
	protected void postExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents, Process process) {
		super.postExec(options, daemonEvents, process);
		
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
}
