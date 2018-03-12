/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.offset;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
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

    private static final String TITLE = Messages.OffsetDialog_Select;

    private static final String NOT_AVAILABLE = Messages.OffsetDialog_NA;

    private static final String START_TIME = Messages.OffsetDialog_StartTime;

    private static final String OFFSET_IN_NS = Messages.OffsetDialog_OffsetTime;

    private static final String TRACE_NAME = Messages.OffsetDialog_TraceName;

    private final Map<ITmfTrace, Long> fResults;
    private final Map<ITmfTrace, Long> fTraceMapCache;

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
                    Long newVal = Long.parseLong(string);
                    if (element instanceof TraceAndOffset) {
                        TraceAndOffset tao = (TraceAndOffset) element;
                        tao.setOffset(newVal);
                        fViewer.getViewer().update(element, null);
                    }
                }
            }
        }

        @Override
        protected Object getValue(Object element) {
            if (element instanceof TraceAndOffset) {
                ITmfTrace iTmfTrace = ((TraceAndOffset) element).getTrace();
                return fTraceMapCache.get(iTmfTrace).toString();
            }
            return null;
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
    public OffsetDialog(Shell parent, Map<ITmfTrace, Long> results) {
        super(parent);
        fResults = results;
        fTraceMapCache = new HashMap<>();
        fTraceMapCache.putAll(fResults);
        parent.setText(TITLE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = new Composite(parent, SWT.FULL_SELECTION);
        Label l = new Label(c, SWT.None);
        GridLayout layout = new GridLayout(1, false);
        parent.setLayout(layout);
        l.setText(TITLE);
        createViewer(parent);
        c.pack();
        return c;
    }

    private void createViewer(Composite parent) {

        // Define the TableViewer
        fViewer = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER, new PatternFilter(){
            @Override
            protected boolean isLeafMatch(Viewer viewer, Object element) {
                if( element instanceof TraceAndOffset){
                    TraceAndOffset traceAndOffset = (TraceAndOffset) element;
                    return wordMatches(traceAndOffset.getTraceName());
                }
                return super.isLeafMatch(viewer, element);
            }
        }, true);

        // Make lines and make header visible
        final Tree table = fViewer.getViewer().getTree();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // Set the ContentProvider
        fViewer.getViewer().setContentProvider(new OffsetMapContentProvider());

        // Get the content for the Viewer,
        // setInput will call getElements in the ContentProvider
        TraceAndOffset[] tao = new TraceAndOffset[fTraceMapCache.size()];
        int i = 0;
        for (Entry<ITmfTrace, Long> entry : fTraceMapCache.entrySet()) {
            tao[i] = new TraceAndOffset(entry.getKey(), fTraceMapCache);
            i++;
        }

        fViewer.getViewer().setInput(tao);

        TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(fViewer.getViewer(), new FocusCellOwnerDrawHighlighter(fViewer.getViewer()));
        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(fViewer.getViewer()) {
        };
        TreeViewerEditor.create(fViewer.getViewer(), focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
                | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

        final TextCellEditor textCellEditor = new TextCellEditor(fViewer.getViewer().getTree());

        fViewer.getViewer().setColumnProperties(new String[] { TRACE_NAME, START_TIME, OFFSET_IN_NS });

        TreeViewerColumn column = createTableViewerColumn(TRACE_NAME, 100);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                final TraceAndOffset traceAndOffset = (TraceAndOffset) element;
                return super.getText(traceAndOffset.getTraceName());
            }
        });
        column = createTableViewerColumn(START_TIME, 180);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                TraceAndOffset element2 = (TraceAndOffset) element;
                ITmfTrace trace = element2.getTrace();
                try {
                    trace.initTrace(trace.getResource(), trace.getPath(), ITmfEvent.class);
                    ITmfContext ctx = trace.seekEvent(0);
                    ITmfEvent evt = trace.getNext(ctx);
                    return super.getText(evt.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue());
                } catch (TmfTraceException e) {
                }
                return super.getText(NOT_AVAILABLE);
            }
        });

        column = createTableViewerColumn(OFFSET_IN_NS, 150);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                final TraceAndOffset traceAndOffset = (TraceAndOffset) element;
                return super.getText(Long.toString(traceAndOffset.getOffset()));
            }
        });
        column.setEditingSupport(new ColumnEditingSupport(fViewer.getViewer(), textCellEditor));

        // Layout the viewer
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        fViewer.getViewer().getControl().setLayoutData(gridData);

    }

    private TreeViewerColumn createTableViewerColumn(String title, int bound) {
        final TreeViewerColumn viewerColumn = new TreeViewerColumn(fViewer.getViewer(),
                SWT.NONE);
        final TreeColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setWidth(bound);
        column.setResizable(true);
        column.setMoveable(true);
        viewerColumn.setEditingSupport(new ColumnEditingSupport(fViewer.getViewer(), new TextCellEditor() {
            @Override
            protected void doSetValue(Object value) {
                super.doSetValue(value.toString());
            }
        }));
        return viewerColumn;
    }

    @Override
    protected void okPressed() {
        fResults.putAll(fTraceMapCache);
        super.okPressed();
    }
}
