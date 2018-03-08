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
package org.eclipse.linuxtools.internal.dataviewers.charts.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.dataviewers.charts.Activator;
import org.eclipse.linuxtools.internal.dataviewers.charts.Messages;
import org.eclipse.linuxtools.internal.dataviewers.charts.view.ChartView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.swtchart.Chart;

/**
 * An action to save a chart as an image (jpeg, gif, png)
 * 
 * @author Marzia Maugeri <marzia.maugeri@st.com>
 */
public class SaveChartAction extends Action {

    private final FileDialog dialog;
    private final Shell shell;
    private Chart cm;

    /**
     * Constructor
     * 
     * @param shell
     *            the shell used by the dialogs
     */
    public SaveChartAction(Shell shell, ChartView cview) {
        super(Messages.ChartConstants_SAVE_CHART_AS_TITLE, Activator.getImageDescriptor("icons/save_chart.gif")); //$NON-NLS-1$
        this.setEnabled(false);
        this.shell = shell;
        this.dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFileName(Messages.DEFAULT_IMG_FILE_NAME);
        dialog.setFilterPath(Messages.DEFAULT_IMG_FILTER_PATH);
        dialog.setFilterExtensions(Messages.saveAsImageExt);
        dialog.setFilterNames(Messages.saveAsImageExtNames);
        dialog.setText(Messages.ChartConstants_SAVE_CHART_DIALOG_TEXT);
        // restore state if there is one saved
        restoreState();
    }

    /**
     * Sets the image plugins on the chart and enables the action if chart is not null.
     * 
     * @param chart
     */
    public void setChart(Chart chart) {
        if (chart != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
        cm = chart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        String path = dialog.open();
        if (path == null) {
            // cancel pressed
            return;
        }
        final File file = new File(path);
        if (file.exists()) {
            boolean overwrite = MessageDialog.openQuestion(shell, Messages.ChartConstants_CONFIRM_OVERWRITE_TITLE,
                    Messages.ChartConstants_CONFIRM_OVERWRITE_MSG);
            if (overwrite) {
                file.delete();
            } else {
                return;
            }
        }

        final String ext = dialog.getFilterNames()[dialog.getFilterIndex()];

        UIJob saveAsImage = new UIJob(Messages.ChartConstants_SAVE_CHART_AS + " " + file.getName()) { //$NON-NLS-1$
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                int extention;
                if (Messages.EXT_GIF.equals(ext)) {
                    extention = SWT.IMAGE_GIF;
                } else if (Messages.EXT_JPEG.equals(ext) || Messages.EXT_JPG.equals(ext)) {
                    extention = SWT.IMAGE_JPEG;
                } else {
                    extention = SWT.IMAGE_PNG;
                }

                try {
                    monitor.beginTask(Messages.ChartConstants_SAVE_CHART_AS + " " + file.getName() + "...", //$NON-NLS-1$//$NON-NLS-2$
                            IProgressMonitor.UNKNOWN);
                    file.createNewFile();
                    generateImageFile(file, extention);
                    return Status.OK_STATUS;
                } catch (IOException e) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.ChartConstants_ERROR_SAVING_CHART
                            + " (" + file.getAbsolutePath() + "):\n" + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        };
        saveAsImage.setUser(true);
        saveAsImage.schedule();

        // save the state of the dialog
        saveState();
    }

    /**
     * Restores the state of this action (file dialog)
     */
    public void restoreState() {
        try {
            IDialogSettings settings = Activator.getDefault().getDialogSettings()
                    .getSection(Messages.TAG_SECTION_CHARTS_SAVEACTION_STATE);
            if (settings == null) {
                settings = Activator.getDefault().getDialogSettings()
                        .addNewSection(Messages.TAG_SECTION_CHARTS_SAVEACTION_STATE);
                return;
            }

            dialog.setFileName(settings.get(Messages.TAG_IMG_FILE_NAME));
            dialog.setFilterPath(settings.get(Messages.TAG_IMG_FILTER_PATH));
        } catch (Exception e) {
            Status s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e);
            Activator.getDefault().getLog().log(s);
        }
    }

    /**
     * Saves the state of this action (file dialog)
     */
    public void saveState() {
        try {
            IDialogSettings settings = Activator.getDefault().getDialogSettings()
                    .getSection(Messages.TAG_SECTION_CHARTS_SAVEACTION_STATE);
            if (settings == null) {
                settings = Activator.getDefault().getDialogSettings()
                        .addNewSection(Messages.TAG_SECTION_CHARTS_SAVEACTION_STATE);
            }

            settings.put(Messages.TAG_IMG_FILE_NAME, dialog.getFileName());
            settings.put(Messages.TAG_IMG_FILTER_PATH, dialog.getFilterPath());
        } catch (Exception e) {
            Status s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e);
            Activator.getDefault().getLog().log(s);
        }
    }

    protected void generateImageFile(File file, int extention) {
        Display dsp = Display.getCurrent();
        GC gc = new GC(cm);
        Image img = new Image(dsp, cm.getSize().x, cm.getSize().y);
        gc.copyArea(img, 0, 0);
        gc.dispose();
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.data = new ImageData[] { img.getImageData() };
        imageLoader.save(file.getAbsolutePath(), extention);
    }

}
