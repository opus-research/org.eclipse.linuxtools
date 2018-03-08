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
package org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.actions;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.AbstractSTAnnotatedSourceEditorInput;
import org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.STAnnotatedSourceEditorActivator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public abstract class AbstractOpenSourceFileAction extends Action {
    public static final String EDITOR_ID = "org.eclipse.linuxtools.dataviewers.annotatedsourceeditor.editor";
    private long ts;

    public AbstractOpenSourceFileAction(String filepath, long ts) {
        super.setText("Open =>" + filepath);
        this.ts = ts;
    }

    @Override
    public void run() {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        IFileStore fileStore = getFileStore();

        if (fileStore != null && !fileStore.fetchInfo().isDirectory()) {
            if (fileStore.fetchInfo().exists()) {
                long timeStamp = fileStore.fetchInfo().getLastModified();

                if (timeStamp > ts) {
                    MessageBox msg = new MessageBox(window.getShell(), SWT.ICON_WARNING | SWT.APPLICATION_MODAL
                            | SWT.YES | SWT.NO);
                    msg.setText(fileStore.toString());
                    msg.setMessage("The file "
                            + fileStore
                            + " is newer than the analysis result, \n"
                            + " if you continue opening it the visualization could result inconsistent. \n Do you want to continue?");
                }

                try {
                    IWorkbenchPage page = window.getActivePage();
                    if (page != null) {
                        IFileStore fs = EFS.getStore(fileStore.toURI());
                        IEditorInput input = getInput(fs);
                        page.openEditor(input, EDITOR_ID, false);
                    }
                } catch (Exception e) {
                    Status s = new Status(IStatus.ERROR, STAnnotatedSourceEditorActivator.PLUGIN_ID,
                            IStatus.ERROR, "Error when opening annotated source view", e);
                    STAnnotatedSourceEditorActivator.getDefault().getLog().log(s);
                }
            } else {
                showMessage("The selected file does not exist: " + fileStore, window.getShell());
            }
        }

    }

    private void showMessage(String message, Shell shell) {
        MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
        msgBox.setText("Error");
        msgBox.setMessage(message);
        msgBox.open();
    }

    public abstract AbstractSTAnnotatedSourceEditorInput getInput(IFileStore fs);

    public abstract IFileStore getFileStore();
}
