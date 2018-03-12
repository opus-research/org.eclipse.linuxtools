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

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/**
 * @author Matthew Khouzam
 *
 */
public class OffsetWizardPage extends WizardPage {

    private static final String START_TIME = "Start Time";

    private static final String OFFSET_IN_NS = "Offset in ns";

    private static final String TRACE_NAME = "Trace name";

    private final class ViewerFilterExtension extends ViewerFilter {
        private String search;

        public void setSearchText(String s) {
            search = s;
        }

        @Override
        public boolean select(Viewer ignored, Object parentElement, Object element) {
            if (search == null || search.isEmpty()) {
                return true;
            }
            if (element instanceof Entry) {
                @SuppressWarnings("unchecked")
                Object key = ((Entry<ITmfTrace, Long>) element).getKey();
                if (key instanceof ITmfTrace) {
                    ITmfTrace iTmfTrace = (ITmfTrace) key;
                    String string = iTmfTrace.getName();
                    return string.contains(search);
                }

            }
            return false;
        }
    }

    private static final String ID = "org.eclipse.linuxtools.internal.tmf.ui.project.wizards.OffsetWizardPage"; //$NON-NLS-1$

    private final Map<ITmfTrace, Long> traceMap;

    private ViewerFilterExtension filter;
    private Text searchText;
    private TableViewer viewer;

    /**
     * Constructor
     *
     * @param result
     *            the map of traces to offset
     */
    public OffsetWizardPage(Map<ITmfTrace, Long> result) {
        super(ID);
        traceMap = result;
        setTitle("Offset traces"); //$NON-NLS-1$
        setDescription("Offset traces to make the time make more sense"); //$NON-NLS-1$
    }

    @Override
    public void createControl(Composite parent) {
        Composite c = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        parent.setLayout(layout);
        searchText = new Text(parent, SWT.BORDER | SWT.SEARCH);
        searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        createViewer(parent);

        // New to support the search
        searchText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                filter.setSearchText(searchText.getText());
                viewer.refresh();
            }

        });
        filter = new ViewerFilterExtension();
        viewer.addFilter(filter);

        c.pack();
        setControl(c);
    }

    private void createViewer(Composite parent) {

        // Define the TableViewer
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

        // Make lines and make header visible
        final Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // Set the ContentProvider
        viewer.setContentProvider(new OffsetMapContentProvider());

        // Get the content for the Viewer,
        // setInput will call getElements in the ContentProvider
        viewer.setInput(traceMap);

        viewer.setColumnProperties(new String[] { TRACE_NAME, OFFSET_IN_NS , START_TIME});

        TableViewerColumn column = new TableViewerColumn(viewer, SWT.FULL_SELECTION);
        column.getColumn().setWidth(100);
        column.getColumn().setText(TRACE_NAME);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {

                return super.getText(((Map.Entry<ITmfTrace, Long>) element).getKey().getName());
            }
        });

        column = new TableViewerColumn(viewer, SWT.FULL_SELECTION);
        column.getColumn().setWidth(100);
        column.getColumn().setText(OFFSET_IN_NS);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {

                return super.getText(((Map.Entry<ITmfTrace, Long>) element).getValue());
            }
        });

        column.setEditingSupport(new EditingSupport(viewer) {

            private final CellEditor editor = new TextCellEditor(viewer.getTable());

            @Override
            protected void setValue(Object element, Object value) {
                if (value instanceof String) {

                    String string = (String) value;
                    if (!string.isEmpty()) {
                        Long newVal = Long.parseLong(string);
                        if (element instanceof Entry) {
                            @SuppressWarnings("unchecked")
                            Object key = ((Entry<ITmfTrace, Long>) element).getKey();
                            if (key instanceof ITmfTrace) {
                                ITmfTrace iTmfTrace = (ITmfTrace) key;
                                traceMap.put(iTmfTrace, newVal);
                                viewer.update(element, null);
                            }
                        }
                    }
                }
            }

            @Override
            protected Object getValue(Object element) {
                if (element instanceof Entry) {
                    @SuppressWarnings("unchecked")
                    Object key = ((Entry<ITmfTrace, Long>) element).getKey();
                    if (key instanceof ITmfTrace) {
                        ITmfTrace iTmfTrace = (ITmfTrace) key;
                        return traceMap.get(iTmfTrace);}
                }
                return null;
            }

            @Override
            protected CellEditor getCellEditor(Object element) {
                return editor;
            }

            @Override
            protected boolean canEdit(Object element) {
                return true;
            }
        });
        column = new TableViewerColumn(viewer, SWT.FULL_SELECTION);
        column.getColumn().setWidth(100);
        column.getColumn().setText(START_TIME);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Map.Entry<ITmfTrace, Long> element2 = (Map.Entry<ITmfTrace, Long>) element;
                ITmfTrace trace = element2.getKey();
                try {
                    trace.initTrace(null, trace.getPath(), ITmfEvent.class);
                    ITmfContext ctx = trace.seekEvent(0);
                    ITmfEvent evt = trace.getNext(ctx);
                    return super.getText(evt.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue());
                } catch (TmfTraceException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return super.getText("N/A");
            }
        });
        // Layout the viewer
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        viewer.getControl().setLayoutData(gridData);
    }

    Long getOffset(ITmfTrace t) {
        return traceMap.get(t);
    }

    void setOffset(ITmfTrace t, Long value) {
        traceMap.put(t, value);
    }
}
