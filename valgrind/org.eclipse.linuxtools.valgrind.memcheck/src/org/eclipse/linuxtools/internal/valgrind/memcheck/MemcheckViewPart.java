/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.memcheck;

import org.eclipse.jface.action.IAction;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class MemcheckViewPart extends ViewPart implements IValgrindToolView {

	@Override
	public void createPartControl(Composite parent) {
	}

	@Override
	public void setFocus() {
	}

	public void refreshView() {
	}

	public IAction[] getToolbarActions() {
		return null;
	}
	
}
