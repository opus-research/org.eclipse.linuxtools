/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;


/**
 * ContributionItem for the trace type selection.
 * @author Bernd Hufmann
 */
public class OpenAsContributionItem extends BaseTraceTypeContributionItem {

    private static final String OPEN_AS_COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.command.openas"; //$NON-NLS-1$

    @Override
    protected String getCommandId() {
        return OPEN_AS_COMMAND_ID;
    }
}
