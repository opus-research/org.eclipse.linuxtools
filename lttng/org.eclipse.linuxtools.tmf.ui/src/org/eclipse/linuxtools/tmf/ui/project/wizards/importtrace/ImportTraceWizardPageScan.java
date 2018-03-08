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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.IWorkbench;

/**
 * <b>Import page that scans files, can be cancelled</b> this page is the third
 * of three pages shown. This one selects the traces to be imported that are to
 * be scanned.
 *
 * @author Matthew Khouzam
 * @since 2.0
 *
 */
public class ImportTraceWizardPageScan extends AbstractImportTraceWizardPage {

    private static final int MAX_TRACES = 65536;
    private CheckboxTreeViewer traceTypeViewer;
    private ProgressBar pb;

    private int position = 0;

    private Thread fScanThread;
    final private BlockingQueue<TraceToValidate> fTracesToScan = new ArrayBlockingQueue<TraceToValidate>(MAX_TRACES);
    private volatile boolean fCanRun = true;

    /*
     * Constructor and destructor
     */

    /**
     * Import page that scans files, can be cancelled.
     *
     * @param name
     *            The name of the page.
     * @param selection
     *            The current selection
     */
    protected ImportTraceWizardPageScan(String name, IStructuredSelection selection) {
        super(name, selection);
    }

    /**
     * Import page that scans files, can be cancelled
     *
     * @param workbench
     *            The workbench reference.
     * @param selection
     *            The current selection
     */
    public ImportTraceWizardPageScan(IWorkbench workbench, IStructuredSelection selection) {
        super(workbench, selection);
    }

    @Override
    public void dispose() {
        fCanRun = false;
        try {
            fScanThread.interrupt();// break the wait
            fScanThread.join(); // it will join
        } catch (InterruptedException e) {
        }
        super.dispose();
    }

    /*
     * Init
     */

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace.
     * AbstractImportTraceWizardPage
     * #createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        final Composite control = (Composite) this.getControl();
        fScanThread = getScanThread();
        fScanThread.start();
        traceTypeViewer = new CheckboxTreeViewer(control, SWT.CHECK);
        traceTypeViewer.setContentProvider(getBatchWizard().getScannedTraces());
        traceTypeViewer.getTree().setHeaderVisible(true);
        traceTypeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        traceTypeViewer.setInput(getBatchWizard().getScannedTraces());
        traceTypeViewer.addCheckStateListener(new ImportTraceCheckStateListener());

        TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(traceTypeViewer, new FocusCellOwnerDrawHighlighter(traceTypeViewer));
        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(traceTypeViewer) {
        };
        TreeViewerEditor.create(traceTypeViewer, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
                | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

        final TextCellEditor textCellEditor = new TextCellEditor(traceTypeViewer.getTree());
        // --------------------
        // Column 1
        // --------------------
        TreeViewerColumn column = new TreeViewerColumn(traceTypeViewer, SWT.NONE);
        column.getColumn().setWidth(200);
        column.getColumn().setText(Messages.ImportTraceWizardImportCaption);
        column.setLabelProvider(new FirstColumnLabelProvider());
        column.setEditingSupport(new ColumnEditorSupport(traceTypeViewer, textCellEditor));

        // --------------------
        // Column 2
        // --------------------

        column = new TreeViewerColumn(traceTypeViewer, SWT.NONE);
        column.getColumn().setWidth(200);
        column.getColumn().setText(Messages.ImportTraceWizardTraceDisplayName);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof FileAndName) {
                    FileAndName elem = (FileAndName) element;
                    return elem.getFile().getPath();
                }
                return null;
            }
        });


        pb = new ProgressBar(control, SWT.SMOOTH);
        pb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        pb.setMaximum(1);
        pb.setMinimum(0);
        init();
        getBatchWizard().setTracesToScan(fTracesToScan);
        getBatchWizard().setTraceFolder(fTargetFolder);
    }

    private void init() {
        Composite optionPane = (Composite) this.getControl();

        optionPane.setLayout(new GridLayout());
        optionPane.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));

        final SelectionListener linkedListener = new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getBatchWizard().setLinked(((Button) e.widget).getSelection());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        };

        Button fLink = new Button(optionPane, SWT.RADIO);
        fLink.setText(Messages.ImportTraceWizardLinkTraces);
        fLink.setSelection(true);
        fLink.setLayoutData(new GridData());
        fLink.addSelectionListener(linkedListener);

        Button fCopy = new Button(optionPane, SWT.RADIO);
        fCopy.setText(Messages.ImportTraceWizardCopyTraces);
        fCopy.setLayoutData(new GridData());
        fCopy.addSelectionListener(linkedListener);

        Button fOverwrite = new Button(optionPane, SWT.CHECK);
        fOverwrite.setText(Messages.ImportTraceWizardOverwriteTraces);
        fOverwrite.setLayoutData(new GridData());
        fOverwrite.setSelection(true);
        fOverwrite.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getBatchWizard().setOverwrite(((Button) e.widget).getSelection());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    /*
     * get and set
     */

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        pb.getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                pb.setMaximum(getBatchWizard().getFilesToScan().size());
            }
        });

    }

    /**
     * @return a new scanThread
     */
    private Thread getScanThread() {
        if (fScanThread == null) {
            fScanThread = new Thread(new scanRunnable());
        }
        return fScanThread;
    }

    /*
     * Helper classes
     */

    private final class ColumnEditorSupport extends EditingSupport {
        private final TextCellEditor textCellEditor;

        private ColumnEditorSupport(ColumnViewer viewer, TextCellEditor textCellEditor) {
            super(viewer);
            this.textCellEditor = textCellEditor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return element instanceof FileAndName;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return textCellEditor;
        }

        @Override
        protected Object getValue(Object element) {
            if (element instanceof FileAndName) {
                return ((FileAndName) element).getName();
            }
            return null;
        }

        @Override
        protected void setValue(Object element, Object value) {
            FileAndName fan = (FileAndName) element;
            fan.setName((String) value);
            getBatchWizard().updateConflicts();
            traceTypeViewer.update(element, null);
            traceTypeViewer.refresh();
        }
    }

    private final class FirstColumnLabelProvider extends ColumnLabelProvider {
        Image fConflict;

        @Override
        public Image getImage(Object element) {
            if (element instanceof FileAndName) {
                final FileAndName fan = (FileAndName) element;
                if (fan.isConflictingName()) {
                    if (fConflict == null) {
                        fConflict = Activator.getDefault().getImageFromImageRegistry(ITmfImageConstants.IMG_UI_CONFLICT);
                    }
                    return fConflict;
                }
            }
            return null;
        }

        @Override
        public String getText(Object element) {
            if (element instanceof FileAndName) {
                FileAndName elem = (FileAndName) element;
                return elem.getName();
            }
            if (element instanceof String) {
                return (String) element;
            }
            return null;
        }
    }

    private final class ImportTraceCheckStateListener implements ICheckStateListener {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
//            final CheckboxTreeViewer tv = (CheckboxTreeViewer) event.getSource();
//            if (event.getElement() instanceof FileAndName) {
//                final FileAndName element = (FileAndName) event.getElement();
//                if (event.getChecked()) {
//                    // Set the trace type
//
//                    String parentElement = (String) getBatchWizard().getScannedTraces().getParent(element);
//                    if (ImportUtils.getTraceAttributes(parentElement) != null) {
//                        element.setTraceTypeId(ImportUtils.getTraceAttributes(parentElement));
//                    }
//                    else {
//                        if (ImportUtils.getCustomTraceTypes(ImportUtils.CUSTOM_TXT_CATEGORY).contains(parentElement)) {
//
//                            element.setTraceTypeId(ImportUtils.getCustomConfigElement(ImportUtils.CUSTOM_TXT_CATEGORY, parentElement));
//                        } else if (ImportUtils.getCustomTraceTypes(ImportUtils.CUSTOM_XML_CATEGORY).contains(parentElement)) {
//                            element.setTraceTypeId(ImportUtils.getCustomConfigElement(ImportUtils.CUSTOM_XML_CATEGORY, parentElement));
//                        }
//                    }
//                    getBatchWizard().addFileToImport(element);
//                    traceTypeViewer.update(element, null);
//                }
//                else {
//                    getBatchWizard().removeFileToImport(element);
//                    traceTypeViewer.update(element, null);
//                }
//                maintainCheckIntegrity(tv, element);
//            }
//            if (event.getElement() instanceof String) {
//
//                tv.setSubtreeChecked(event.getElement(), event.getChecked());
//                final Object[] children = getBatchWizard().getScannedTraces().getChildren(event.getElement());
//                if (event.getChecked()) {
//                    for (int i = 0; i < children.length; i++) {
//                        final FileAndName element = (FileAndName) children[i];
//                        getBatchWizard().addFileToImport(element);
//                        element.setTraceTypeId(ImportUtils.getTraceAttributes((String) event.getElement()));
//                        traceTypeViewer.update(children[i], null);
//                    }
//                }
//                else {
//                    for (int i = 0; i < children.length; i++) {
//                        getBatchWizard().removeFileToImport((FileAndName) children[i]);
//
//                    }
//                }
//
//            }
            getBatchWizard().updateConflicts();
            getBatchWizard().getContainer().updateButtons();
            traceTypeViewer.update(event.getElement(), null);
        }

        private void maintainCheckIntegrity(final CheckboxTreeViewer viewer, final FileAndName element) {
            final ImportTraceContentProvider scannedTraces = getBatchWizard().getScannedTraces();
            String parentElement = (String) scannedTraces.getParent(element);
            boolean allChecked = true;
            final FileAndName[] siblings = scannedTraces.getSiblings(element);
            for (FileAndName child : siblings) {
                allChecked &= viewer.getChecked(child);
            }
            viewer.setChecked(parentElement, allChecked);
        }
    }

    private final class scanRunnable implements Runnable {
        private final class UpdateUIRunnable implements Runnable {
            private final boolean treeUpdated;

            private UpdateUIRunnable(boolean treeUpdated) {
                this.treeUpdated = treeUpdated;
            }

            @Override
            public void run() {
                if (treeUpdated) {
                    traceTypeViewer.refresh();
                }
                // we need a way of knowing the total number of
                // files to scan
                int maximum = Math.max(pb.getMaximum(), getBatchWizard().getScannedTraces().getSize() + fTracesToScan.size());
                pb.setMaximum(maximum);
                pb.setSelection(position);
            }
        }

        @Override
        public void run() {
            while (fCanRun == true) {
                boolean updated = false;
                try {
                    boolean validCombo;
                    TraceToValidate traceToScan = fTracesToScan.take();
                    if (!getBatchWizard().hasScanned(traceToScan)) {
                        getBatchWizard().addResult(traceToScan, ImportUtils.validate(traceToScan));
                    }
                    validCombo = getBatchWizard().getResult(traceToScan);
                    if (validCombo) {
                        // Synched on it's parent

                        getBatchWizard().getScannedTraces().addCandidate(traceToScan.getTraceType(), new File(traceToScan.getTraceToScan()));
                        updated = true;
                    }
                    position++;
                    final boolean treeUpdated = updated;
                    if( !pb.isDisposed() ) {
                        pb.getDisplay().asyncExec(new UpdateUIRunnable(treeUpdated));
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
