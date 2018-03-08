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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceValidationHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;

/**
 * <b>Import page that scans files, can be cancelled</b> this page is the third
 * of three pages shown. This one selects the traces to be imported that are to
 * be scanned.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class ImportTraceWizardScanPage extends AbstractImportTraceWizardPage {

    private static final int MAX_TRACES = 65536;
    private CheckboxTreeViewer traceTypeViewer;

    // private int position = 0;
    final ScanRunnable fRunnable = new ScanRunnable("Scan Job"); //$NON-NLS-1$
    final private BlockingQueue<TraceValidationHelper> fTracesToScan = new ArrayBlockingQueue<TraceValidationHelper>(MAX_TRACES);
    private volatile boolean fCanRun = true;

    // --------------------------------------------------------------------------------
    // Constructor and destructor
    // --------------------------------------------------------------------------------

    /**
     * Import page that scans files, can be cancelled.
     *
     * @param name
     *            The name of the page.
     * @param selection
     *            The current selection
     */
    protected ImportTraceWizardScanPage(String name, IStructuredSelection selection) {
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
    public ImportTraceWizardScanPage(IWorkbench workbench, IStructuredSelection selection) {
        super(workbench, selection);
    }

    @Override
    public void dispose() {
        fCanRun = false;
        super.dispose();
    }

    /*
     * Init
     */

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        final Composite control = (Composite) this.getControl();
        setTitle(Messages.ImportTraceWizardScanPage_title);
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
        column.getColumn().setText(Messages.ImportTraceWizard_traceDisplayName);
        column.setLabelProvider(new FirstColumnLabelProvider());
        column.setEditingSupport(new ColumnEditorSupport(traceTypeViewer, textCellEditor));

        // --------------------
        // Column 2
        // --------------------

        column = new TreeViewerColumn(traceTypeViewer, SWT.NONE);
        column.getColumn().setWidth(200);
        column.getColumn().setText(Messages.ImportTraceWizard_importCaption);
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

        init();
        getBatchWizard().setTracesToScan(fTracesToScan);
        fRunnable.schedule();
        setErrorMessage(Messages.ImportTraceWizardScanPage_selectAtleastOne);
    }

    private void init() {
        Composite optionPane = (Composite) this.getControl();

        optionPane.setLayout(new GridLayout());
        optionPane.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));

        final Button fLink = new Button(optionPane, SWT.RADIO);
        fLink.setText(Messages.ImportTraceWizard_linkTraces);
        fLink.setSelection(true);
        fLink.setLayoutData(new GridData());

        final Button fCopy = new Button(optionPane, SWT.RADIO);
        fCopy.setText(Messages.ImportTraceWizard_copyTraces);
        fCopy.setLayoutData(new GridData());

        final SelectionListener linkedListener = new RadioChooser(fLink);

        fLink.addSelectionListener(linkedListener);
        fCopy.addSelectionListener(linkedListener);

        Button fOverwrite = new Button(optionPane, SWT.CHECK);
        fOverwrite.setText(Messages.ImportTraceWizard_overwriteTraces);
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
     * Helper classes
     */

    private final class RadioChooser implements SelectionListener {
        final private Button isLinked;

        public RadioChooser(Button desiredButton) {
            isLinked = desiredButton;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {

            final Button widget = (Button) e.widget;
            getBatchWizard().setLinked(widget.equals(isLinked));
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {

        }
    }

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
            final CheckboxTreeViewer tv = (CheckboxTreeViewer)
                    event.getSource();
            if (event.getElement() instanceof FileAndName) {
                final FileAndName element = (FileAndName) event.getElement();
                if (event.getChecked()) {
                    getBatchWizard().addFileToImport(element);
                    traceTypeViewer.update(element, null);
                }
                else {
                    getBatchWizard().removeFileToImport(element);
                    traceTypeViewer.update(element, null);
                }
                maintainCheckIntegrity(tv, element);
            }
            if (event.getElement() instanceof String) {

                tv.setSubtreeChecked(event.getElement(), event.getChecked());
                final Object[] children =
                        getBatchWizard().getScannedTraces().getChildren(event.getElement());
                if (event.getChecked()) {
                    for (int i = 0; i < children.length; i++) {
                        final FileAndName element = (FileAndName) children[i];
                        getBatchWizard().addFileToImport(element);
                        traceTypeViewer.update(children[i], null);
                    }
                }
                else {
                    for (int i = 0; i < children.length; i++) {
                        getBatchWizard().removeFileToImport((FileAndName) children[i]);

                    }
                }

            }
            getBatchWizard().updateConflicts();
            if (getBatchWizard().hasConflicts()) {
                setErrorMessage(Messages.ImportTraceWizardScanPage_renameError);
            } else if (!getBatchWizard().hasTracesToImport()) {
                setErrorMessage(Messages.ImportTraceWizardScanPage_selectAtleastOne);
            } else {
                setErrorMessage(null);
            }
            getWizard().getContainer().updateButtons();
            traceTypeViewer.update(event.getElement(), null);
        }

        private void maintainCheckIntegrity(final CheckboxTreeViewer viewer, final FileAndName element) {
            final ImportTraceContentProvider scannedTraces = getBatchWizard().getScannedTraces();
            String parentElement = (String) scannedTraces.getParent(element);
            boolean allChecked = true;
            final FileAndName[] siblings = scannedTraces.getSiblings(element);
            if (siblings != null) {
                for (FileAndName child : siblings) {
                    allChecked &= viewer.getChecked(child);
                }
            }
            viewer.setChecked(parentElement, allChecked);
        }
    }

    private final class ScanRunnable extends Job {

        private IProgressMonitor fMonitor;

        public ScanRunnable(String name) {
            super(name);
        }

        private synchronized IProgressMonitor getMonitor() {
            return fMonitor;
        }

        @Override
        public IStatus run(IProgressMonitor monitor) {
            fMonitor = monitor;
            final Control control = traceTypeViewer.getControl();
            control.getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    fMonitor = SubMonitor.convert(getMonitor());
                    getMonitor().setTaskName(Messages.ImportTraceWizardPageScan_scanning + " "); //$NON-NLS-1$
                    ((SubMonitor) getMonitor()).setWorkRemaining(IProgressMonitor.UNKNOWN);
                }
            });

            while (fCanRun == true) {
                boolean updated = false;
                boolean validCombo;
                if (fTracesToScan.isEmpty() && !control.isDisposed()) {
                    control.getDisplay().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            if (!control.isDisposed()) {
                                getMonitor().setTaskName(Messages.ImportTraceWizardPageScan_scanning + " "); //$NON-NLS-1$
                                getMonitor().subTask(Messages.ImportTraceWizardPageScan_done);
                                ImportTraceWizardScanPage.this.setMessage(Messages.ImportTraceWizardPageScan_scanning + " " + Messages.ImportTraceWizardPageScan_done); //$NON-NLS-1$
                            }
                        }
                    });
                }
                try {
                    final TraceValidationHelper traceToScan = fTracesToScan.take();

                    if (!getBatchWizard().hasScanned(traceToScan)) {
                        getBatchWizard().addResult(traceToScan, TmfTraceType.getInstance().validate(traceToScan));
                    }

                    /*
                     * The following is to update the UI
                     */
                    validCombo = getBatchWizard().getResult(traceToScan);
                    if (validCombo) {
                        // Synched on it's parent

                        getBatchWizard().getScannedTraces().addCandidate(traceToScan.getTraceType(), new File(traceToScan.getTraceToScan()));
                        updated = true;
                    }
                    final int scanned = getBatchWizard().getNumberOfResults();
                    final int toScan = fTracesToScan.size();
                    final int prevVal = (int) ((scanned - 1) * 100.0 / (scanned - 1 + toScan + 1));
                    final int curVal = (int) ((scanned) * 100.0 / (scanned + toScan));
                    if (curVal != prevVal) {
                        updated = true;
                    }
                    /*
                     * update the progress
                     */
                    if (updated) {
                        if (!control.isDisposed()) {
                            control.getDisplay().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    if (!control.isDisposed()) {
                                        getMonitor().setTaskName(Messages.ImportTraceWizardPageScan_scanning + " "); //$NON-NLS-1$
                                        getMonitor().subTask(traceToScan.getTraceToScan());
                                        getMonitor().worked(1);
                                        ImportTraceWizardScanPage.this.setMessage(Messages.ImportTraceWizardPageScan_scanning + " " //$NON-NLS-1$
                                                + Integer.toString(curVal)
                                                + "%"); //$NON-NLS-1$
                                    }
                                }
                            }
                                    );
                        }
                    }

                    /*
                     * here we update the table
                     */
                    final boolean editing = traceTypeViewer.isCellEditorActive();
                    if (updated && !editing)
                    {
                        if (!control.isDisposed()) {
                            control.getDisplay().asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    if (!control.isDisposed()) {
                                        if (!traceTypeViewer.isCellEditorActive()) {
                                            traceTypeViewer.refresh();
                                        }
                                    }
                                }
                            });
                        }
                    }
                } catch (InterruptedException e) {
                    return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, null);
                }
            }
            return Status.OK_STATUS;
        }
    }

    /**
     * Refresh the view and the corresponding model.
     */
    public void refresh() {
        final Control control = traceTypeViewer.getControl();
        if (!control.isDisposed()) {
            control.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!control.isDisposed()) {
                        traceTypeViewer.refresh();
                    }
                }
            });
        }
    }
}
