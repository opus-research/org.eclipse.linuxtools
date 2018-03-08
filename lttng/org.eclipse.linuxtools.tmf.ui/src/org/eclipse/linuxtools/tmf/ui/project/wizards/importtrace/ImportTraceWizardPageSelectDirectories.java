/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import java.io.File;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;

/**
 * <b>Select the directories to scan for traces</b> this page is the second of
 * three pages shown. This one selects the files to be scanned.
 *
 * @author Matthew Khouzam
 * @since 2.0
 *
 */
public class ImportTraceWizardPageSelectDirectories extends AbstractImportTraceWizardPage {
    /**
     * ID
     */
    public static String ID = "org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace.ImportTraceWizardPagePopulate"; //$NON-NLS-1$

    /**
     * Constructor. Creates the trace wizard page.
     *
     * @param name
     *            The name of the page.
     * @param selection
     *            The current selection
     */
    protected ImportTraceWizardPageSelectDirectories(String name, IStructuredSelection selection) {
        super(name, selection);
    }

    /**
     * Constructor
     *
     * @param workbench
     *            The workbench reference.
     * @param selection
     *            The current selection
     */
    public ImportTraceWizardPageSelectDirectories(IWorkbench workbench, IStructuredSelection selection) {
        super(workbench, selection);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        final Composite control = (Composite) this.getControl();
        control.setLayout(new GridLayout(2, false));
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final Table selectedFiles = new Table(control, SWT.NONE);
        selectedFiles.clearAll();
        selectedFiles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite buttonArea = new Composite(control, SWT.None);
        buttonArea.setLayout(new RowLayout());
        ((RowLayout) buttonArea.getLayout()).type = SWT.VERTICAL;
        buttonArea.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        Button addFile = new Button(buttonArea, SWT.PUSH);
        addFile.setText(Messages.ImportTraceWizardPageSelectDirectories_0);

        addFile.addSelectionListener(new AddFileHandler());
        Button addDirectory = new Button(buttonArea, SWT.PUSH);
        addDirectory.setText(Messages.ImportTraceWizardPageSelectDirectories_1);

        addDirectory.addSelectionListener(new AddDirectoryHandler());

        Button removeFile = new Button(buttonArea, SWT.PUSH);
        removeFile.setText(Messages.ImportTraceWizardPageSelectDirectories_2);
        removeFile.addSelectionListener(new RemoveFileHandler(selectedFiles));
        this.setTitle(Messages.ImportTraceWizardPageSelectDirectories_3);
    }

    private void updateButtons() {
        BatchImportTraceWizard wiz = getBatchWizard();
        updateTable();
        wiz.getContainer().updateButtons();
    }

    private void updateTable() {
        final Table selectedFiles = (Table) ((Composite) getControl()).getChildren()[0];
        selectedFiles.clearAll();
        selectedFiles.setItemCount(0);
        for (String s : ((BatchImportTraceWizard) getWizard()).getFileNames()) {
            TableItem ti = new TableItem(selectedFiles, SWT.None);
            ti.setText(s);
        }
    }

    @Override
    public boolean canFlipToNextPage() {
        final Table selectedFiles = (Table) ((Composite) getControl()).getChildren()[0];
        boolean canLoad = selectedFiles.getItemCount() >= 0;
        if (canLoad) {
            setErrorMessage(null);
        } else {
            setErrorMessage(Messages.ImportTraceWizardPageSelectDirectories_4);
        }
        return canLoad;
    }

    private final class AddFileHandler implements SelectionListener {
        @Override
        public void widgetSelected(SelectionEvent e) {

            FileDialog dialog = new
                    FileDialog(Display.getCurrent().getActiveShell(), SWT.NONE);
            String fn = dialog.open();
            if (null != fn) {
                File f = new File(fn);
                if (f.exists()) {
                    getBatchWizard().AddFileToScan(fn);

                }
            }
            updateButtons();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }
    }

    private final class AddDirectoryHandler implements SelectionListener {
        @Override
        public void widgetSelected(SelectionEvent e) {

            // BUG BUG BUG BUG BUG IN SWT. Cannot read multiple files in a
            // fake directory.

            // FileDialog dialog = new
            // FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN |
            // SWT.MULTI);
            // dialog.setFilterPath(".");
            // if (null != dialog.open()) {
            // for (String fn : dialog.getFileNames()) {
            // final String pathname = dialog.getFilterPath()+
            // File.separator+fn;
            // File f = new File(pathname);
            // if (f.exists()) {
            // ((BatchImportTraceWizard) getWizard()).addFile(fn, f);
            // }
            // }
            // }

            DirectoryDialog dialog = new
                    DirectoryDialog(Display.getCurrent().getActiveShell(), SWT.NONE);
            String fn = dialog.open();
            if (null != fn) {
                File f = new File(fn);
                if (f.exists()) {
                    getBatchWizard().AddFileToScan(fn);

                }
            }
            updateButtons();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }
    }

    private final class RemoveFileHandler implements SelectionListener {
        private final Table selectedFiles;

        private RemoveFileHandler(Table selectedFiles) {
            this.selectedFiles = selectedFiles;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            TableItem selectedToRemove[] = selectedFiles.getSelection();
            for (TableItem victim : selectedToRemove) {
                String victimName = victim.getText();
                ((BatchImportTraceWizard) getWizard()).removeFile(victimName);
            }
            updateButtons();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }
    }
}
