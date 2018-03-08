/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Extracted main implementation into a base class
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;


/**
 * ContributionItem for the trace type selection.
 *
 * @author Patrick Tassé
 */
public class SelectTraceTypeContributionItem extends BaseTraceTypeContributionItem {

    private static final String SELECT_TRACE_TYPE_COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.command.select_trace_type"; //$NON-NLS-1$

    @Override
    protected String getCommandId() {
        return SELECT_TRACE_TYPE_COMMAND_ID;
    }
}
