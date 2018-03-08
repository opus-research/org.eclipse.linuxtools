/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;


import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.swt.widgets.Shell;

/**
 * <p>
 * Confirmation dialog implementation.
 * </p>
 * 
 * @author Bernd Hufmann
 */

public class ConfirmDialog implements IConfirmDialog {

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs.IConfirmDialog#openConfirm(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String)
     */
    @Override
    public boolean openConfirm(Shell parent, String title, String message) {
        return MessageDialog.openConfirm(parent, 
                Messages.TraceControl_DestroyConfirmationTitle, 
                Messages.TraceControl_DestroyConfirmationMessage);
    }
}
