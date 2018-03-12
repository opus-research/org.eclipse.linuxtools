/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.dataviewers.STDataViewersActivator;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTTreeViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersMessages;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This action collapse all the tree
 *
 */
public class STCollapseAllTreeAction extends Action {

    private final AbstractSTTreeViewer stViewer;

    /**
     * Constructor
     *
     * @param stViewer
     *            the stViewer to collapse
     */
    public STCollapseAllTreeAction(AbstractSTTreeViewer stViewer) {
        super(STDataViewersMessages.collapseAllAction_title,
        		AbstractUIPlugin.imageDescriptorFromPlugin(STDataViewersActivator.PLUGIN_ID,
        				"icons/collapse_all.gif")); //$NON-NLS-1$
        this.stViewer = stViewer;
    }

    @Override
    public void run() {
        Object input = stViewer.getViewer().getInput();
        if (input != null) {
            stViewer.getViewer().collapseAll();
        }
    }
}
