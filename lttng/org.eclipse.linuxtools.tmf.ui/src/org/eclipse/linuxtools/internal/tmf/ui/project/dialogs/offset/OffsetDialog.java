/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.dialogs.offset;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.tmf.core.signal.TmfEventSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.viewers.ArrayTreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Offset wizard dialog
 *
 * @author Matthew Khouzam
 *
 */
public class OffsetDialog extends Dialog {

    private static final int TREE_EDITOR_MIN_WIDTH = 50;

    private static final String TITLE = Messages.OffsetDialog_Title;
    private static final String TRACE_NAME = Messages.OffsetDialog_TraceName;
    private static final String REFERENCE_TIME = Messages.OffsetDialog_ReferenceTime;
    private static final String OFFSET_IN_SECONDS = Messages.OffsetDialog_OffsetTime;
    private static final String TARGET_TIME = Messages.OffsetDialog_TargetTime;

    private static final TmfTimestampFormat OFFSET_FORMAT = new TmfTimestampFormat("T.SSS SSS SSS"); //$NON-NLS-1$

    private final Map<TmfTraceElement, Long> fOffsetMap;
    private final Map<TmfTraceElement, ITmfTimestamp> fRefTimeMap;
    private final Map<TmfTraceElement, ITmfTimestamp> fTargetTimeMap;

    private Label fMessageLabel;
    private FilteredTree fViewer;

    private abstract class ColumnEditingSupport extends EditingSupport {
        private final TextCellEditor textCellEditor;

        private ColumnEditingSupport(ColumnViewer viewer, TextCellEditor textCellEditor) {
            super(viewer);
            this.textCellEditor = textCellEditor;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return textCellEditor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }
    }

    private class RefTimeEditingSupport extends ColumnEditingSupport {
        private RefTimeEditingSupport(ColumnViewer viewer, TextCellEditor textCellEditor) {
            super(viewer, textCellEditor);
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (value instanceof String) {
                String string = (String) value;
                if (string.trim().isEmpty()) {
                    fRefTimeMap.remove(element);
                } else {
                    try {
                        ITmfTimestamp refTime = fRefTimeMap.get(element);
                        long ref = refTime == null ? 0 : refTime.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                        Long newVal = TmfTimestampFormat.getDefaulTimeFormat().parseValue(string, ref);
                        fRefTimeMap.put((TmfTraceElement) element, new TmfNanoTimestamp(newVal));
                    } catch (ParseException e) {
                        /* Ignore and reload previous value */
                    }
                }
                fViewer.getViewer().update(element, null);
            }
        }

        @Override
        protected Object getValue(Object element) {
            if (fRefTimeMap.get(element) == null) {
                return ""; //$NON-NLS-1$
            }
            return fRefTimeMap.get(element).toString();
        }
    }

    private class OffsetEditingSupport extends ColumnEditingSupport {
        private OffsetEditingSupport(ColumnViewer viewer, TextCellEditor textCellEditor) {
            super(viewer, textCellEditor);
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (value instanceof String) {
                String string = (String) value;
                if (string.trim().isEmpty()) {
                    fOffsetMap.put((TmfTraceElement) element, 0L);
                } else {
                    try {
                        Long newVal = OFFSET_FORMAT.parseValue(string);
                        fOffsetMap.put((TmfTraceElement) element, newVal);
                    } catch (ParseException e) {
                        /* Ignore and reload previous value */
                    }
                }
                fViewer.getViewer().update(element, null);
            }
        }

        @Override
        protected Object getValue(Object element) {
            if (fOffsetMap.get(element) == 0) {
                return ""; //$NON-NLS-1$
            }
            return OFFSET_FORMAT.format((long) fOffsetMap.get(element));
        }
    }

    private class TargetTimeEditingSupport extends ColumnEditingSupport {
        private TargetTimeEditingSupport(ColumnViewer viewer, TextCellEditor textCellEditor) {
            super(viewer, textCellEditor);
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (value instanceof String) {
                String string = (String) value;
                if (string.trim().isEmpty()) {
                    fTargetTimeMap.remove(element);
                } else {
                    try {
                        ITmfTimestamp refTime = fTargetTimeMap.get(element);
                        long ref = refTime == null ? 0 : refTime.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                        Long newVal = TmfTimestampFormat.getDefaulTimeFormat().parseValue(string, ref);
                        fTargetTimeMap.put((TmfTraceElement) element, new TmfNanoTimestamp(newVal));
                    } catch (ParseException e) {
                        /* Ignore and reload previous value */
                    }
                }
                fViewer.getViewer().update(element, null);
            }
        }

        @Override
        protected Object getValue(Object element) {
            if (fTargetTimeMap.get(element) == null) {
                return ""; //$NON-NLS-1$
            }
            return fTargetTimeMap.get(element).toString();
        }
    }

    /**
     * Constructor
     *
     * @param parent
     *            parent shell
     * @param results
     *            results to put the data into
     */
    public OffsetDialog(Shell parent, Map<TmfTraceElement, Long> results) {
        super(parent);
        setShellStyle(getShellStyle() & ~SWT.APPLICATION_MODAL);
        fOffsetMap = results;
        fRefTimeMap = new HashMap<>();
        fTargetTimeMap = new HashMap<>();
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(TITLE);
        Composite composite = (Composite) super.createDialogArea(parent);
        createMessageArea(composite);
        createViewer(composite);

        /* set label width hint equal to tree width */
        GridData gd = (GridData) fMessageLabel.getLayoutData();
        gd.widthHint = fViewer.getViewer().getTree().computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

        TmfSignalManager.register(this);
        composite.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                TmfSignalManager.deregister(this);
            }
        });
        return composite;
    }

    private void createMessageArea(Composite parent) {
        fMessageLabel = new Label(parent, SWT.WRAP);
        fMessageLabel.setText(Messages.OffsetDialog_Message);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.widthHint = 0;
        gd.heightHint = SWT.DEFAULT;
        fMessageLabel.setLayoutData(gd);
    }

    private void createViewer(Composite parent) {

        // Define the TableViewer
        fViewer = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER, new PatternFilter() {
            @Override
            protected boolean isLeafMatch(Viewer viewer, Object element) {
                return wordMatches(((TmfTraceElement) element).getElementPath());
            }
        }, true);

        // Make lines and make header visible
        final Tree tree = fViewer.getViewer().getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(fViewer.getViewer(), new FocusCellOwnerDrawHighlighter(fViewer.getViewer()));
        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(fViewer.getViewer());
        TreeViewerEditor.create(fViewer.getViewer(), focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
                | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

        final TextCellEditor textCellEditor = new TextCellEditor(fViewer.getViewer().getTree(), SWT.RIGHT);

        fViewer.getViewer().setColumnProperties(new String[] { TRACE_NAME, REFERENCE_TIME, OFFSET_IN_SECONDS });

        TreeViewerColumn column = createTreeViewerColumn(TRACE_NAME, SWT.NONE);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((TmfTraceElement) element).getElementPath();
            }
        });

        column = createTreeViewerColumn(REFERENCE_TIME, SWT.RIGHT);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return super.getText(fRefTimeMap.get(element));
            }
        });
        column.setEditingSupport(new RefTimeEditingSupport(fViewer.getViewer(), textCellEditor));

        column = createTreeViewerColumn(OFFSET_IN_SECONDS, SWT.RIGHT);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (fOffsetMap.get(element) != 0) {
                    return super.getText(OFFSET_FORMAT.format((long) fOffsetMap.get(element)));
                }
                return ""; //$NON-NLS-1$
            }
        });
        column.setEditingSupport(new OffsetEditingSupport(fViewer.getViewer(), textCellEditor));

        column = createTreeViewerColumn("", SWT.NONE); //$NON-NLS-1$
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ""; //$NON-NLS-1$
            }
        });
        column.getColumn().setWidth(TREE_EDITOR_MIN_WIDTH);
        column.getColumn().setResizable(false);

        column = createTreeViewerColumn(TARGET_TIME, SWT.RIGHT);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return super.getText(fTargetTimeMap.get(element));
            }
        });
        column.setEditingSupport(new TargetTimeEditingSupport(fViewer.getViewer(), textCellEditor));

        List<TmfTraceElement> traces = new ArrayList<>(fOffsetMap.keySet());
        Collections.sort(traces, new Comparator<TmfTraceElement>() {
            @Override
            public int compare(TmfTraceElement o1, TmfTraceElement o2) {
                IPath folder1 = new Path(o1.getElementPath()).removeLastSegments(1);
                IPath folder2 = new Path(o2.getElementPath()).removeLastSegments(1);
                if (folder1.equals(folder2)) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
                if (folder1.isPrefixOf(folder2)) {
                    return 1;
                } else if (folder2.isPrefixOf(folder1)) {
                    return -1;
                }
                return folder1.toString().compareToIgnoreCase(folder2.toString());
            }
        });

        fViewer.getViewer().setContentProvider(new ArrayTreeContentProvider());
        fViewer.getViewer().setInput(traces);

        /* add button as tree editors to fourth column of every item */
        for (TreeItem treeItem : tree.getItems()) {
            TreeEditor treeEditor = new TreeEditor(tree);
            Button applyButton = new Button(tree, SWT.PUSH);
            applyButton.setText("<<"); //$NON-NLS-1$
            applyButton.setData(treeItem.getData());
            applyButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    TmfTraceElement traceElement = (TmfTraceElement) e.widget.getData();
                    ITmfTimestamp targetTime = fTargetTimeMap.get(traceElement);
                    ITmfTimestamp refTime = fRefTimeMap.get(traceElement);
                    if (targetTime != null && refTime != null) {
                        long offset = new TmfNanoTimestamp(targetTime).getValue() -
                                new TmfNanoTimestamp(refTime).getValue();
                        fOffsetMap.put(traceElement, offset);
                        fViewer.getViewer().update(traceElement, null);
                    }
                }
            });
            treeEditor.grabHorizontal = true;
            treeEditor.minimumWidth = TREE_EDITOR_MIN_WIDTH;
            treeEditor.setEditor(applyButton, treeItem, 3);
        }

        /* put temporary values in maps to pack according to time formats */
        fRefTimeMap.put(traces.get(0), new TmfNanoTimestamp());
        fTargetTimeMap.put(traces.get(0), new TmfNanoTimestamp());
        fViewer.getViewer().update(traces.get(0), null);
        for (TreeColumn treeColumn : tree.getColumns()) {
            if (treeColumn.getResizable()) {
                treeColumn.pack();
            }
        }
        fRefTimeMap.clear();
        fTargetTimeMap.clear();
        fViewer.getViewer().update(traces.get(0), null);

        for (TmfTraceElement traceElement : fOffsetMap.keySet()) {
            for (ITmfTrace trace : TmfTraceManager.getInstance().getOpenedTraces()) {
                if (traceElement.getResource().equals(trace.getResource())) {
                    fRefTimeMap.put(traceElement, trace.getStartTime());
                    fViewer.getViewer().update(traceElement, null);
                }
            }
        }

        /* open trace when double-clicking a tree item */
        tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                TmfTraceElement traceElement = (TmfTraceElement) e.item.getData();
                TmfOpenTraceHelper.openTraceFromElement(traceElement);
            }
        });

        tree.setFocus();
    }

    private TreeViewerColumn createTreeViewerColumn(String title, int style) {
        final TreeViewerColumn viewerColumn = new TreeViewerColumn(fViewer.getViewer(), style);
        final TreeColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setResizable(true);
        return viewerColumn;
    }

    /**
     * Handler for the event selected signal
     *
     * @param signal
     *            the event selected signal
     */
    @TmfSignalHandler
    public void eventSelected(final TmfEventSelectedSignal signal) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                for (TmfTraceElement traceElement : fOffsetMap.keySet()) {
                    if (traceElement.getResource().equals(signal.getEvent().getTrace().getResource())) {
                        fRefTimeMap.put(traceElement, signal.getEvent().getTimestamp());
                        fViewer.getViewer().update(traceElement, null);
                    }
                }
            }
        });
    }

    /**
     * Handler for the time selected signal
     *
     * @param signal
     *            the event selected signal
     */
    @TmfSignalHandler
    public void timeSelected(final TmfTimeSynchSignal signal) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                for (TmfTraceElement traceElement : fOffsetMap.keySet()) {
                    fTargetTimeMap.put(traceElement, signal.getBeginTime());
                    fViewer.getViewer().update(traceElement, null);
                }
            }
        });
    }

    /**
     * Handler for the trace opened signal
     *
     * @param signal
     *            the trace opened signal
     */
    @TmfSignalHandler
    public void traceOpened(final TmfTraceOpenedSignal signal) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                for (TmfTraceElement traceElement : fOffsetMap.keySet()) {
                    if (traceElement.getResource().equals(signal.getTrace().getResource())) {
                        if (fRefTimeMap.get(traceElement) == null) {
                            fRefTimeMap.put(traceElement, signal.getTrace().getStartTime());
                            fViewer.getViewer().update(traceElement, null);
                        }
                    }
                }
            }
        });
    }
}
