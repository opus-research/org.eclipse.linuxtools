/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Moved bulk of code to new parent class
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.jface.action.IContributionItem;

/**
 * ContributionItem for the trace type selection.
 *
 * @author Patrick Tassé
 */
public class SelectTraceTypeContributionItem extends SelectElementTypeContributionItem {

    //private static final ImageDescriptor SELECTED_ICON = ImageDescriptor.createFromImage(TmfUiPlugin.getDefault().getImageFromPath("icons/elcl16/bullet.gif")); //$NON-NLS-1$
    private static final String BUNDLE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_trace_type.bundle"; //$NON-NLS-1$
    private static final String TYPE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_trace_type.type"; //$NON-NLS-1$
    private static final String ICON_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_trace_type.icon"; //$NON-NLS-1$
    private static final String SELECT_TRACE_TYPE_COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.command.select_trace_type"; //$NON-NLS-1$
    private static final String DEFAULT_TRACE_ICON_PATH = "icons/elcl16/trace.gif"; //$NON-NLS-1$

    @Override
    protected IContributionItem[] getContributionItems() {
        return getContributionItems(false);
    }

    @Override
    protected String getBundleParameter() {
        return BUNDLE_PARAMETER;
    }

    @Override
    protected String getIconParameter() {
        return ICON_PARAMETER;
    }

    @Override
    protected String getTypeParameter() {
        return TYPE_PARAMETER;
    }

    @Override
    protected String getCommandId() {
        return SELECT_TRACE_TYPE_COMMAND_ID;
    }

    @Override
    protected String getDefaultIconPath() {
        return DEFAULT_TRACE_ICON_PATH;
    }

}
