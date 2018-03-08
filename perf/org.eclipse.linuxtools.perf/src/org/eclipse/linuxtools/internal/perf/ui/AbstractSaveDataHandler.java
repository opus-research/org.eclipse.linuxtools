package org.eclipse.linuxtools.internal.perf.ui;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractSaveDataHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		InputDialog dialog = new InputDialog(Display.getCurrent()
				.getActiveShell(), Messages.PerfSaveSession_title,
				Messages.PerfSaveSession_msg, "", new IInputValidator() {

					@Override
					public String isValid(String newText) {
						if ("".equals(newText)) {
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

	protected IPath getWorkingDir() {
		return PerfPlugin.getDefault().getWorkingDir();
	}

	protected IPath getNewDataLocation(String filename, String extension) {
		IPath newFilename  = getWorkingDir().append(filename);
		return newFilename.addFileExtension(extension);

	}

	/**
	 * Verify that we can save the specified file.
	 *
	 * @param file <code>File</code> to save
	 * @return true if we can go ahead and save the file, false otherwise
	 */
	protected boolean verifyFile(File file) {
		if (file.exists()) {
			String msg = MessageFormat.format(
					Messages.PerfSaveSession_file_exists_msg,
					new Object[] { file.getName() });
			return MessageDialog.openQuestion(Display.getCurrent()
					.getActiveShell(),
					Messages.PerfSaveSession_file_exists_title, msg);
		}
		return true;
	}

	/**
	 * Save data to file with specified name
	 *
	 * @param filename file name
	 */
	protected abstract void saveData(String filename);

	/**
	 * Verify data to save.
	 *
	 * @return
	 */
	protected abstract boolean verifyData();

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
