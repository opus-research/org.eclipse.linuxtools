/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.handlers;

import java.net.URI;
import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.perf.BaseDataManipulator;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.swt.widgets.Display;

/**
 * Class for handling general tasks handled by session saving commands:
 * File name creation and validation, command enablement, data file verification.
 */
public abstract class AbstractSaveDataHandler extends BaseDataManipulator implements IHandler {

    @Override
    public Object execute(ExecutionEvent event) {
        InputDialog dialog = new InputDialog(Display.getCurrent()
                .getActiveShell(), Messages.PerfSaveSession_title,
                Messages.PerfSaveSession_msg, "", new IInputValidator() { //$NON-NLS-1$

            @Override
            public String isValid(String newText) {
                if (newText.isEmpty()) {
                    return Messages.PerfSaveSession_invalid_filename_msg;
                }
                return null;
            }
        });

        if (dialog.open() == Window.OK) {
            saveData(dialog.getValue());
        }

        return null;
    }

    @Override
    public boolean isEnabled() {
        IPath curWorkingDirectory = getWorkingDir();
        return curWorkingDirectory != null && !curWorkingDirectory.isEmpty()
                && verifyData();
    }

    /**
     * Get current working directory.
     * @return current working directory.
     */
    protected IPath getWorkingDir() {
        return PerfPlugin.getDefault().getWorkingDir();
    }

    protected URI getWorkingDirURI() {
        return PerfPlugin.getDefault().getWorkingDirURI();
    }

    /**
     * New data location based on specified name, which the specified
     * extension will be appended to.
     *
     * @param filename
     * @param extension
     * @return
     */
    public IPath getNewDataLocation(String filename, String extension) {
        IPath newFilename  = getWorkingDir().append(filename);
        return newFilename.addFileExtension(extension);

    }

    /**
     * Verify that we can save the specified file.
     *
     * @param file <code>File</code> to save
     * @return true if we can go ahead and save the file, false otherwise
     */
    public boolean canSave(IPath file) {
        IRemoteFileProxy proxy = null;
        try {
            proxy = RemoteProxyManager.getInstance().getFileProxy(getWorkingDirURI());
        } catch (CoreException e) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.MsgProxyError, Messages.MsgProxyError);
        }
        IFileStore fileStore = proxy.getResource(file.toPortableString());
        if (fileStore.fetchInfo().exists()) {
            String msg = MessageFormat.format(
                    Messages.PerfSaveSession_file_exists_msg,
                    new Object[] { fileStore.getName() });
            return MessageDialog.openQuestion(Display.getCurrent()
                    .getActiveShell(),
                    Messages.PerfSaveSession_file_exists_title, msg);
        }
        return true;
    }

    /**
     * Open error dialog informing user of saving failure.
     * @param filename
     */
    public void openErroDialog(final String title, String pattern, String arg) {
        final String errorMsg = MessageFormat.format(pattern, new Object[] { arg });
        if (Display.getCurrent() != null) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), title, errorMsg);
        } else {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.openError(Display.getCurrent().getActiveShell(), title, errorMsg);
                }
            });
        }
    }

    /**
     * Save data to file with specified name and return handle
     *
     * @param filename the file name
     */
    public abstract IPath saveData(String filename);

    /**
     * Verify data to save.
     *
     * @return true if data is valid
     */
    public abstract boolean verifyData();

    @Override
    public boolean isHandled() {
        return isEnabled();
    }

    @Override
    public void removeHandlerListener(IHandlerListener handlerListener) {
    }

    @Override
    public void addHandlerListener(IHandlerListener handlerListener) {

    }

    @Override
    public void dispose() {

    }
}
