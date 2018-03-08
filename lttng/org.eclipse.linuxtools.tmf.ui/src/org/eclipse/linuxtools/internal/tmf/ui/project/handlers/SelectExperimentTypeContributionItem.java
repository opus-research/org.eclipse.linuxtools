/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.jface.action.IContributionItem;

/**
 * ContributionItem for the experiment type selection.
 *
 * @author Geneviève Bastien
 */
public class SelectExperimentTypeContributionItem extends SelectElementTypeContributionItem {

    private static final String BUNDLE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_experiment_type.bundle"; //$NON-NLS-1$
    private static final String TYPE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_experiment_type.type"; //$NON-NLS-1$
    private static final String ICON_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_experiment_type.icon"; //$NON-NLS-1$
    private static final String SELECT_TRACE_TYPE_COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.command.select_experiment_type"; //$NON-NLS-1$
    private static final String DEFAULT_TRACE_ICON_PATH = "icons/elcl16/trace.gif"; //$NON-NLS-1$

    @Override
    protected IContributionItem[] getContributionItems() {
        return getContributionItems(true);
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
