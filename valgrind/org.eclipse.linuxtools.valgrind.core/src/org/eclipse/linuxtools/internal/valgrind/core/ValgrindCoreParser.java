/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Alena Laskavaia - Bug 482947 - Valgrind Message API's: get rid of launch dependency
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.IPersistableSourceLocator2;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.linuxtools.valgrind.core.CommandLineConstants;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.linuxtools.valgrind.core.ValgrindParserUtils;

public class ValgrindCoreParser {
	private static final String AT = "at"; //$NON-NLS-1$
	private static final String BY = "by"; //$NON-NLS-1$
	private List<IValgrindMessage> messages;
	private int pid;
	private ILaunch launch;

	public ValgrindCoreParser(File inputFile, ILaunch launch) throws IOException {
		this.launch = launch;
		parse(inputFile, copyLaunchSourceLocator(launch));
	}

	/**
	 * When using this method make sure locator passed to this method can
	 * outlive disposal of launch object if it was derived from it,
	 * use {@link #copyLaunchSourceLocator(ILaunch)} if needed
	 * @param inputFile - file to parse
	 * @param locator - source locator
	 * @throws IOException
	 */
	public ValgrindCoreParser(File inputFile, ISourceLocator locator) throws IOException {
		parse(inputFile, locator);
	}

	public static ISourceLocator copyLaunchSourceLocator(ILaunch launch) {
		ISourceLocator sourceLocator = launch.getSourceLocator();
		ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
		// sourceLocator from launch object will be disposed when launch is gone,
		// since we want to use it later we need to create a copy of it
		if (sourceLocator instanceof ISourceLookupDirector) {
			try {
				ISourceLookupDirector director = (ISourceLookupDirector) sourceLocator;

				String id = director.getId();
				String memento = director.getMemento();
				IPersistableSourceLocator sourceLocatorCopy = DebugPlugin.getDefault().getLaunchManager().newSourceLocator(id);
				if (sourceLocatorCopy instanceof IPersistableSourceLocator2) {
					((IPersistableSourceLocator2) sourceLocatorCopy).initializeFromMemento(memento, launchConfiguration);
				} else
					sourceLocatorCopy.initializeFromMemento(memento);
				return sourceLocatorCopy;
			} catch (CoreException e) {
				// ignore
			}
		}
		return sourceLocator;
	}


	private void parse(File inputFile, ISourceLocator locator) throws IOException, FileNotFoundException {
		// keep track of nested messages and their corresponding indents
		Stack<IValgrindMessage> messageStack = new Stack<>();
		Stack<Integer> indentStack = new Stack<>();
		messages = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
			pid = ValgrindParserUtils.parsePID(inputFile.getName(), CommandLineConstants.LOG_PREFIX);
			String line;
			while ((line = br.readLine()) != null) {
				// remove PID string
				// might encounter warnings also #325130
				// fixed #423371 - handle timestamp (e.g. ==00:00:00:01.175 52756728==)
				line = line.replaceFirst("==([\\d:\\.]+\\s)?\\d+==|\\*\\*\\d+\\*\\*", ""); //$NON-NLS-1$ //$NON-NLS-2$
				int indent;
				for (indent = 0; indent < line.length()
						&& line.charAt(indent) == ' '; indent++) {
				}
				line = line.trim();
				if (!line.isEmpty()) {
					/*
					 * indent == 1 -> top level message
					 * indent > 1 -> child message
					 * indent == 0 -> should not occur
					 */
					if (indent == 1) {
						// top-level message, clear stacks
						IValgrindMessage message = getMessage(null, line, locator);
						messages.add(message);
						messageStack.clear();
						messageStack.push(message);
						indentStack.clear();
						indentStack.push(indent);
					} else if (indent > 1) {
						/**
						 * We assume that an indented child message has a
						 * parent, but this may not be the case.
						 * See BZ #360225
						 */
						if (indentStack.isEmpty()) {
							// pretend this is a top level message
							IValgrindMessage message = getMessage(null, line, locator);
							messages.add(message);
							messageStack.clear();
							messageStack.push(message);
							indentStack.clear();
							indentStack.push(1);
						} else {
							// find this message's parent
							while (indent <= indentStack.peek()) {
								messageStack.pop();
								indentStack.pop();
							}
							messageStack.push(getMessage(messageStack.peek(), line, locator));
							indentStack.push(indent);
						}
					}
				}
			}
		}
	}

	private IValgrindMessage getMessage(IValgrindMessage message, String line, ISourceLocator locator) {
		if (line.startsWith(AT) || line.startsWith(BY)) {
			Object[] parsed = ValgrindParserUtils.parseFilename(line);
			String filename = (String) parsed[0];
			int lineNo = (Integer) parsed[1];
			return new ValgrindStackFrame(message, line, locator, filename, lineNo);
		}
		return new ValgrindError(message, line, launch, pid);
	}

	public IValgrindMessage[] getMessages() {
		return messages.toArray(new IValgrindMessage[messages.size()]);
	}
}
