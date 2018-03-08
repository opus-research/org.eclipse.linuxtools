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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.IWorkbench;

/**
 * Import page that scans files, can be cancelled (3/3)
 *
 * @author Matthew Khouzam
 * @since 2.0
 *
 */
public class ImportTraceWizardPageScan extends AbstractImportTraceWizardPage {

    private CheckboxTreeViewer traceTypeViewer;
    private ProgressBar pb;

    private Iterator<String> pos;
    private int position = 0;

    private Thread scanThread;

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
    public boolean canFlipToNextPage() {
        // TODO Auto-generated method stub
        return super.canFlipToNextPage() && getBatchWizard().canFinish();
    }

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

    boolean canRun = false;

    private boolean canScan() {
        return super.isCurrentPage();
    }

    /**
     * @return
     */
    private Thread createScanThread() {
        return new Thread(new scanRunnable());
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        final Composite control = (Composite) this.getControl();
        scanThread = createScanThread();
        scanThread.start();
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
        // --------------------
        // Column 1
        // --------------------
        TreeViewerColumn column = new TreeViewerColumn(traceTypeViewer, SWT.NONE);
        column.getColumn().setWidth(200);
        column.getColumn().setText("Trace to import");
        column.setLabelProvider(new FirstColumnLabelProvider());

        // --------------------
        // Column 2
        // --------------------
        final TextCellEditor textCellEditor = new TextCellEditor(traceTypeViewer.getTree());

        column = new TreeViewerColumn(traceTypeViewer, SWT.NONE);
        column.getColumn().setWidth(200);
        column.getColumn().setText("Trace display name");
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof FileAndName) {
                    FileAndName elem = (FileAndName) element;
                    return elem.getName();
                }
                return null;
            }
        });
        column.setEditingSupport(new ColumnEditorSupport(traceTypeViewer, textCellEditor));

        pb = new ProgressBar(control, SWT.SMOOTH);
        pb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        pb.setMaximum(100);
        pb.setMinimum(0);
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
            if( element instanceof FileAndName ){
                final FileAndName fan = (FileAndName) element;
                if( fan.isConflictingName() ) {
                    if( fConflict == null ) {
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
                return elem.getFile().getPath();
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
            final CheckboxTreeViewer tv = (CheckboxTreeViewer) event.getSource();
            if (event.getElement() instanceof FileAndName) {
                final FileAndName element = (FileAndName) event.getElement();
                if (event.getChecked()) {
                    String parentElement = (String) getBatchWizard().getScannedTraces().getParent(element);
                    element.setConfigurationElement(ImportUtils.getTraceAttributes(parentElement));
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
                final Object[] children = getBatchWizard().getScannedTraces().getChildren(event.getElement());
                if( event.getChecked()){
                    for( int i =0 ; i < children.length; i++ ) {
                        final FileAndName element = (FileAndName) children[i];
                        getBatchWizard().addFileToImport(element);
                        element.setConfigurationElement(ImportUtils.getTraceAttributes((String)event.getElement()));
                        traceTypeViewer.update(children[i], null);
                    }
                }
                else{
                    for( int i =0 ; i < children.length; i++ ) {
                        getBatchWizard().removeFileToImport((FileAndName) children[i]);

                    }
                }

            }
            getBatchWizard().updateConflicts();
            getBatchWizard().getContainer().updateButtons();
            traceTypeViewer.update(event.getElement(), null);
        }

        private void maintainCheckIntegrity(final CheckboxTreeViewer viewer, final FileAndName element) {
            String parentElement = (String) getBatchWizard().getScannedTraces().getParent(element);
            boolean allChecked = true;
            for (FileAndName child : getBatchWizard().getScannedTraces().getSiblings(element)) {
                allChecked &= viewer.getChecked(child);
            }
            viewer.setChecked(parentElement, allChecked);
        }
    }

    private final class scanRunnable implements Runnable {

        @Override
        public void run() {
            while (!canScan()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (pos == null) {
                pos = getBatchWizard().getFilesToScan().iterator();
            }
            while (pos.hasNext()) {
                if (getBatchWizard().isReset()) {
                    getBatchWizard().set();
                    pos = getBatchWizard().getFilesToScan().iterator();
                    getBatchWizard().getScannedTraces().clearCandidates();
                }
                if (canScan()) {
                    String fileName = pos.next();
                    boolean updated = false;
                    final List<String> setToReset = new ArrayList<String>();
                    for (String traceCat : getBatchWizard().getTracesToScan()) {
                        if (ImportUtils.validate(traceCat, fileName)) {
                            getBatchWizard().getScannedTraces().addCandidate(traceCat, new File(fileName));
                            setToReset.add(traceCat);
                            updated = true;
                        }
                    }
                    position++;
                    final boolean treeUpdated = updated;
                    pb.getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            if (treeUpdated) {
                                for (String traceCat : setToReset) {
                                    traceTypeViewer.setChecked(traceCat, false);
                                }
                                traceTypeViewer.refresh();
                            }
                            pb.setSelection(position);
                        }
                    });
                }
                else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
