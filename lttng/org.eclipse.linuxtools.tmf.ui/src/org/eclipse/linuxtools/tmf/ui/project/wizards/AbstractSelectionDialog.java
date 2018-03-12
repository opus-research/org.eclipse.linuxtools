/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Copied and adapted from NewFolderDialog
 *   Geneviève Bastien - Moved the actual copy code to model element's class
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

/**
 * @author Matthew Khouzam
 * @since 3.1
 */
public abstract class AbstractSelectionDialog extends SelectionStatusDialog {

    /**
     * Creates an instance of a SelectionStatusDialog.
     *
     * @param parent
     *            parent shell
     */
    public AbstractSelectionDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void computeResult() {
    }

    @Override
    public void create() {
        super.create();
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

}