/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.rpm.ui.editor.BuildType;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Builder;
import org.eclipse.linuxtools.internal.rpm.ui.editor.RPMHandlerUtils;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import org.osgi.framework.FrameworkUtil;

/**
 * Common functionality of all build handlers.
 *
 * @since 1.0.0
 */
public abstract class AbstractSpecfileEditorBuildDelegate extends AbstractHandler {

	protected abstract BuildType getBuildType();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IResource resource = RPMHandlerUtils.getResource(event);
		final RPMProject rpj = RPMHandlerUtils.getRPMProject(resource);
		final Builder builder = new Builder(getBuildType());
		Job job = new Job(builder.getExecutionMessage()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IStatus result = null;
				IOConsole myConsole = getConsole(rpj.getSpecFile().getProject().getName());
				IOConsoleOutputStream out = myConsole.newOutputStream();
				myConsole.clearConsole();
				myConsole.activate();
				try {
					monitor.beginTask(builder.getExecutionMessage(), IProgressMonitor.UNKNOWN);
					result = builder.build(rpj, out);
				} catch (CoreException e) {
					result = new Status(IStatus.ERROR, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(),
							e.getMessage(), e);
				} finally {
					monitor.done();
				}
				return result;
			}
		};
		job.setUser(true); // suppress UI. That's done in encapsulated
		job.schedule();
		return null;
	}

	/**
	 * Get the console.
	 *
	 * @param packageName
	 *            The name of the package(RPM) this console will be for.
	 * @return A console instance.
	 */
	protected MessageConsole getConsole(String packageName) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		String projectConsoleName = NLS.bind(Messages.PrepareSources_consoleName, packageName);
		MessageConsole ret = null;
		for (IConsole cons : ConsolePlugin.getDefault().getConsoleManager()
				.getConsoles()) {
			if (cons.getName().equals(projectConsoleName)) {
				ret = (MessageConsole) cons;
			}
		}
		// no existing console, create new one
		if (ret == null) {
			ret = new MessageConsole(projectConsoleName, null, null, true);
		}
		conMan.addConsoles(new IConsole[] { ret });
		ret.clearConsole();
		ret.activate();
		return ret;
	}
}
