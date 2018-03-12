/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.handlers;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.linuxtools.systemtap.graphing.ui.views.GraphSelectorEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * This handler exports all data in the currently-active {@link GraphSelectorEditor}
 * into an external file, which can be imported back in later with {@link ImportDataSetHandler}.
 */
public class ExportDataSetHandler extends AbstractHandler {

    private GraphSelectorEditor getActiveGraphEditor(ExecutionEvent event) {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        return editor instanceof GraphSelectorEditor ? (GraphSelectorEditor) editor : null;
    }

    @Override
    public Object execute(ExecutionEvent event) {
        GraphSelectorEditor editor = getActiveGraphEditor(event);
        if (editor == null) {
            return null;
        }
        FileDialog dialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE);
        dialog.setOverwrite(true);
        dialog.setFilterExtensions(new String[]{Messages.DataSetFileExtension});
        dialog.setText(MessageFormat.format(Messages.ExportDataSetAction_DialogTitle, editor.getActiveTitle()));
        dialog.setFileName(editor.getActiveTitle().replaceAll(" ", "")); //$NON-NLS-1$ //$NON-NLS-2$
        String path = dialog.open();
        if (path != null) {
            editor.getActiveDisplaySet().getDataSet().writeToFile(new File(path));
        }
        return null;
    }

}
