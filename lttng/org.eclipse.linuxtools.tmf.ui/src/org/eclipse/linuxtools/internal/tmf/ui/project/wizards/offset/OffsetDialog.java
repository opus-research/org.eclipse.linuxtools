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

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.offset;

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
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.viewers.ArrayTreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Offset wizard dialog
 *
 * @author Matthew Khouzam
 *
 */
public class OffsetDialog extends Dialog {

    private static final String TITLE = Messages.OffsetDialog_Title;

    private static final String REFERENCE_TIME = Messages.OffsetDialog_ReferenceTime;

    private static final String OFFSET_IN_NS = Messages.OffsetDialog_OffsetTime;

    private static final String TRACE_NAME = Messages.OffsetDialog_TraceName;

    private final Map<TmfTraceElement, Long> fOffsetMap;
    private final Map<TmfTraceElement, TmfTimestamp> fRefTimeMap;

    private FilteredTree fViewer;

    private final class ColumnEditingSupport extends EditingSupport {
        private final TextCellEditor textCellEditor;

        private ColumnEditingSupport(ColumnViewer viewer, TextCellEditor textCellEditor) {
            super(viewer);
            this.textCellEditor = textCellEditor;
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (value instanceof String) {
                String string = (String) value;
                boolean empty = string.isEmpty();
                if (!empty) {
                    try {
                        Long newVal = Long.parseLong(string);
                        fOffsetMap.put((TmfTraceElement) element, newVal);
                    } catch (NumberFormatException e) {
                        /* Ignore and reload previous value */
                    }
                    fViewer.getViewer().update(element, null);
                }
            }
        }

        @Override
        protected Object getValue(Object element) {
            return fOffsetMap.get(element).toString();
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
        setShellStyle(getShellStyle() | SWT.RESIZE);
        fOffsetMap = results;
        fRefTimeMap = new HashMap<>();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(TITLE);
        Composite composite = (Composite) super.createDialogArea(parent);
        createViewer(composite);
        return composite;
    }

    private void createViewer(Composite parent) {

        // Define the TableViewer
        fViewer = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER, new PatternFilter(){
            @Override
            protected boolean isLeafMatch(Viewer viewer, Object element) {
                return wordMatches(((TmfTraceElement) element).getElementPath());
            }
        }, true);

        // Make lines and make header visible
        final Tree table = fViewer.getViewer().getTree();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(fViewer.getViewer(), new FocusCellOwnerDrawHighlighter(fViewer.getViewer()));
        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(fViewer.getViewer());
        TreeViewerEditor.create(fViewer.getViewer(), focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
                | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

        final TextCellEditor textCellEditor = new TextCellEditor(fViewer.getViewer().getTree());

        fViewer.getViewer().setColumnProperties(new String[] { TRACE_NAME, REFERENCE_TIME, OFFSET_IN_NS });

        TreeViewerColumn column = createTreeViewerColumn(TRACE_NAME);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((TmfTraceElement) element).getElementPath();
            }
        });

        column = createTreeViewerColumn(REFERENCE_TIME);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return super.getText(fRefTimeMap.get(element));
            }
        });

        column = createTreeViewerColumn(OFFSET_IN_NS);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return super.getText(fOffsetMap.get(element));
            }
        });
        column.setEditingSupport(new ColumnEditingSupport(fViewer.getViewer(), textCellEditor));

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

        for (TreeColumn treeColumn : fViewer.getViewer().getTree().getColumns()) {
            treeColumn.pack();
        }
    }

    private TreeViewerColumn createTreeViewerColumn(String title) {
        final TreeViewerColumn viewerColumn = new TreeViewerColumn(fViewer.getViewer(),
                SWT.NONE);
        final TreeColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setResizable(true);
        return viewerColumn;
    }
}
