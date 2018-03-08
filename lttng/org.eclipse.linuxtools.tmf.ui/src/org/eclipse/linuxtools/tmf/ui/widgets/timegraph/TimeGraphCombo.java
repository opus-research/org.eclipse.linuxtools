/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson, others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   François Rajotte - Filter implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.dialogs.TimeGraphFilterDialog;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Time graph "combo" view (with the list/tree on the left and the gantt chart
 * on the right)
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TimeGraphCombo extends Composite {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final Object FILLER = new Object();

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    // The tree viewer
    private TreeViewer fTreeViewer;

    // The time viewer
    private TimeGraphViewer fTimeGraphViewer;

    // The top-level input (children excluded)
    private List<? extends ITimeGraphEntry> fTopInput;

    // The selection listener map
    private final Map<ITimeGraphSelectionListener, SelectionListenerWrapper> fSelectionListenerMap = new HashMap<ITimeGraphSelectionListener, SelectionListenerWrapper>();

    // The map of viewer filters
    private final Map<ViewerFilter, ViewerFilter> fViewerFilterMap = new HashMap<ViewerFilter, ViewerFilter>();

    // Flag to block the tree selection changed listener when triggered by the time graph combo
    private boolean fInhibitTreeSelection = false;

    // Number of filler rows used by the tree content provider
    private int fNumFillerRows;

    // Calculated item height for Linux workaround
    private int fLinuxItemHeight = 0;

    // The button that opens the filter dialog
    private Action showFilterAction;

    // The filter dialog
    private TimeGraphFilterDialog fFilterDialog;

    // The filter generated from the filter dialog
    private RawViewerFilter fFilter;

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    /**
     * The TreeContentProviderWrapper is used to insert filler items after
     * the elements of the tree's real content provider.
     */
    private class TreeContentProviderWrapper implements ITreeContentProvider {
        private final ITreeContentProvider contentProvider;

        public TreeContentProviderWrapper(ITreeContentProvider contentProvider) {
            this.contentProvider = contentProvider;
        }

        @Override
        public void dispose() {
            contentProvider.dispose();
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            contentProvider.inputChanged(viewer, oldInput, newInput);
        }

        @Override
        public Object[] getElements(Object inputElement) {
            Object[] elements = contentProvider.getElements(inputElement);
            // add filler elements to ensure alignment with time analysis viewer
            Object[] oElements = Arrays.copyOf(elements, elements.length + fNumFillerRows, Object[].class);
            for (int i = 0; i < fNumFillerRows; i++) {
                oElements[elements.length + i] = FILLER;
            }
            return oElements;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof ITimeGraphEntry) {
                return contentProvider.getChildren(parentElement);
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof ITimeGraphEntry) {
                return contentProvider.getParent(element);
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof ITimeGraphEntry) {
                return contentProvider.hasChildren(element);
            }
            return false;
        }
    }

    /**
     * The TreeLabelProviderWrapper is used to intercept the filler items
     * from the calls to the tree's real label provider.
     */
    private class TreeLabelProviderWrapper implements ITableLabelProvider {
        private final ITableLabelProvider labelProvider;

        public TreeLabelProviderWrapper(ITableLabelProvider labelProvider) {
            this.labelProvider = labelProvider;
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            labelProvider.addListener(listener);
        }

        @Override
        public void dispose() {
            labelProvider.dispose();
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            if (element instanceof ITimeGraphEntry) {
                return labelProvider.isLabelProperty(element, property);
            }
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            labelProvider.removeListener(listener);
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof ITimeGraphEntry) {
                return labelProvider.getColumnImage(element, columnIndex);
            }
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof ITimeGraphEntry) {
                return labelProvider.getColumnText(element, columnIndex);
            }
            return null;
        }

    }

    /**
     * The SelectionListenerWrapper is used to intercept the filler items from
     * the time graph combo's real selection listener, and to prevent double
     * notifications from being sent when selection changes in both tree and
     * time graph at the same time.
     */
    private class SelectionListenerWrapper implements ISelectionChangedListener, ITimeGraphSelectionListener {
        private final ITimeGraphSelectionListener listener;
        private ITimeGraphEntry selection = null;

        public SelectionListenerWrapper(ITimeGraphSelectionListener listener) {
            this.listener = listener;
        }

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            if (fInhibitTreeSelection) {
                return;
            }
            Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
            if (element instanceof ITimeGraphEntry) {
                ITimeGraphEntry entry = (ITimeGraphEntry) element;
                if (entry != selection) {
                    selection = entry;
                    listener.selectionChanged(new TimeGraphSelectionEvent(event.getSource(), selection));
                }
            }
        }

        @Override
        public void selectionChanged(TimeGraphSelectionEvent event) {
            ITimeGraphEntry entry = event.getSelection();
            if (entry != selection) {
                selection = entry;
                listener.selectionChanged(new TimeGraphSelectionEvent(event.getSource(), selection));
            }
        }
    }

    /**
     * The ViewerFilterWrapper is used to intercept the filler items from
     * the time graph combo's real ViewerFilters. These filler items should
     * always be visible.
     */
    private class ViewerFilterWrapper extends ViewerFilter {

        private ViewerFilter fWrappedFilter;

        ViewerFilterWrapper(ViewerFilter filter) {
            super();
            this.fWrappedFilter = filter;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (element instanceof ITimeGraphEntry) {
                return fWrappedFilter.select(viewer, parentElement, element);
            }
            return true;
        }

    }

    /**
     * This filter simply keeps a list of elements that should be filtered out.
     * All the other elements will be shown.
     * By default and when the list is set to null, all elements are shown.
     */
    private class RawViewerFilter extends ViewerFilter {

        private List<Object> fFiltered = null;

        public void setFiltered(List<Object> objects) {
            fFiltered = objects;
        }

        public List<Object> getFiltered() {
            return fFiltered;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (fFiltered == null) {
                return true;
            }
            return !fFiltered.contains(element);
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new instance of this class given its parent
     * and a style value describing its behavior and appearance.
     *
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style the style of widget to construct
     */
    public TimeGraphCombo(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());

        final SashForm sash = new SashForm(this, SWT.NONE);

        fTreeViewer = new TreeViewer(sash, SWT.FULL_SELECTION | SWT.H_SCROLL);
        final Tree tree = fTreeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        fTimeGraphViewer = new TimeGraphViewer(sash, SWT.NONE);
        fTimeGraphViewer.setItemHeight(getItemHeight(tree));
        fTimeGraphViewer.setHeaderHeight(tree.getHeaderHeight());
        fTimeGraphViewer.setBorderWidth(tree.getBorderWidth());
        fTimeGraphViewer.setNameWidthPref(0);

        fFilter = new RawViewerFilter();
        addFilter(fFilter);

        fFilterDialog = new TimeGraphFilterDialog(getShell());

        // Feature in Windows. The tree vertical bar reappears when
        // the control is resized so we need to hide it again.
        // Bug in Linux. The tree header height is 0 in constructor,
        // so we need to reset it later when the control is resized.
        tree.addControlListener(new ControlAdapter() {
            private int depth = 0;
            @Override
            public void controlResized(ControlEvent e) {
                if (depth == 0) {
                    depth++;
                    tree.getVerticalBar().setEnabled(false);
                    // this can trigger controlResized recursively
                    tree.getVerticalBar().setVisible(false);
                    depth--;
                }
                fTimeGraphViewer.setHeaderHeight(tree.getHeaderHeight());
            }
        });

        // ensure synchronization of expanded items between tree and time graph
        fTreeViewer.addTreeListener(new ITreeViewerListener() {
            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                fTimeGraphViewer.setExpandedState((ITimeGraphEntry) event.getElement(), false);
                List<TreeItem> treeItems = getVisibleExpandedItems(tree);
                if (treeItems.size() == 0) {
                    return;
                }
                TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                tree.setTopItem(treeItem);
            }

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                ITimeGraphEntry entry = (ITimeGraphEntry) event.getElement();
                fTimeGraphViewer.setExpandedState(entry, true);
                for (ITimeGraphEntry child : entry.getChildren()) {
                    boolean expanded = fTreeViewer.getExpandedState(child);
                    fTimeGraphViewer.setExpandedState(child, expanded);
                }
                List<TreeItem> treeItems = getVisibleExpandedItems(tree);
                if (treeItems.size() == 0) {
                    return;
                }
                final TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                // queue the top item update because the tree can change its top item
                // autonomously immediately after the listeners have been notified
                getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        tree.setTopItem(treeItem);
                    }});
            }
        });

        // ensure synchronization of expanded items between tree and time graph
        fTimeGraphViewer.addTreeListener(new ITimeGraphTreeListener() {
            @Override
            public void treeCollapsed(TimeGraphTreeExpansionEvent event) {
                fTreeViewer.setExpandedState(event.getEntry(), false);
            }

            @Override
            public void treeExpanded(TimeGraphTreeExpansionEvent event) {
                ITimeGraphEntry entry = event.getEntry();
                fTreeViewer.setExpandedState(entry, true);
                for (ITimeGraphEntry child : entry.getChildren()) {
                    boolean expanded = fTreeViewer.getExpandedState(child);
                    fTimeGraphViewer.setExpandedState(child, expanded);
                }
            }
        });

        // prevent mouse button from selecting a filler tree item
        tree.addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                TreeItem treeItem = tree.getItem(new Point(event.x, event.y));
                if (treeItem == null || treeItem.getData() == FILLER) {
                    event.doit = false;
                    List<TreeItem> treeItems = getVisibleExpandedItems(tree);
                    if (treeItems.size() == 0) {
                        fTreeViewer.setSelection(new StructuredSelection());
                        fTimeGraphViewer.setSelection(null);
                        return;
                    }
                    // this prevents from scrolling up when selecting
                    // the partially visible tree item at the bottom
                    tree.select(treeItems.get(treeItems.size() - 1));
                    fTreeViewer.setSelection(new StructuredSelection());
                    fTimeGraphViewer.setSelection(null);
                }
            }
        });

        // prevent mouse wheel from scrolling down into filler tree items
        tree.addListener(SWT.MouseWheel, new Listener() {
            @Override
            public void handleEvent(Event event) {
                event.doit = false;
                Slider scrollBar = fTimeGraphViewer.getVerticalBar();
                fTimeGraphViewer.setTopIndex(scrollBar.getSelection() - event.count);
                List<TreeItem> treeItems = getVisibleExpandedItems(tree);
                if (treeItems.size() == 0) {
                    return;
                }
                TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                tree.setTopItem(treeItem);
            }
        });

        // prevent key stroke from selecting a filler tree item
        tree.addListener(SWT.KeyDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                List<TreeItem> treeItems = getVisibleExpandedItems(tree);
                if (treeItems.size() == 0) {
                    fTreeViewer.setSelection(new StructuredSelection());
                    event.doit = false;
                    return;
                }
                if (event.keyCode == SWT.ARROW_DOWN) {
                    int index = Math.min(fTimeGraphViewer.getSelectionIndex() + 1, treeItems.size() - 1);
                    fTimeGraphViewer.setSelection((ITimeGraphEntry) treeItems.get(index).getData());
                    event.doit = false;
                } else if (event.keyCode == SWT.PAGE_DOWN) {
                    int height = tree.getSize().y - tree.getHeaderHeight() - tree.getHorizontalBar().getSize().y;
                    int countPerPage = height / getItemHeight(tree);
                    int index = Math.min(fTimeGraphViewer.getSelectionIndex() + countPerPage - 1, treeItems.size() - 1);
                    fTimeGraphViewer.setSelection((ITimeGraphEntry) treeItems.get(index).getData());
                    event.doit = false;
                } else if (event.keyCode == SWT.END) {
                    fTimeGraphViewer.setSelection((ITimeGraphEntry) treeItems.get(treeItems.size() - 1).getData());
                    event.doit = false;
                }
                TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                tree.setTopItem(treeItem);
                if (fTimeGraphViewer.getSelectionIndex() >= 0) {
                    fTreeViewer.setSelection(new StructuredSelection(fTimeGraphViewer.getSelection()));
                } else {
                    fTreeViewer.setSelection(new StructuredSelection());
                }
            }
        });

        // ensure alignment of top item between tree and time graph
        fTimeGraphViewer.getTimeGraphControl().addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                List<TreeItem> treeItems = getVisibleExpandedItems(tree);
                if (treeItems.size() == 0) {
                    return;
                }
                TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                tree.setTopItem(treeItem);
            }
        });

        // ensure synchronization of selected item between tree and time graph
        fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (fInhibitTreeSelection) {
                    return;
                }
                if (event.getSelection() instanceof IStructuredSelection) {
                    Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
                    if (selection instanceof ITimeGraphEntry) {
                        fTimeGraphViewer.setSelection((ITimeGraphEntry) selection);
                    }
                    List<TreeItem> treeItems = getVisibleExpandedItems(tree);
                    if (treeItems.size() == 0) {
                        return;
                    }
                    TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                    tree.setTopItem(treeItem);
                }
            }
        });

        // ensure synchronization of selected item between tree and time graph
        fTimeGraphViewer.addSelectionListener(new ITimeGraphSelectionListener() {
            @Override
            public void selectionChanged(TimeGraphSelectionEvent event) {
                ITimeGraphEntry entry = fTimeGraphViewer.getSelection();
                fInhibitTreeSelection = true; // block the tree selection changed listener
                if (entry != null) {
                    StructuredSelection selection = new StructuredSelection(entry);
                    fTreeViewer.setSelection(selection);
                } else {
                    fTreeViewer.setSelection(new StructuredSelection());
                }
                fInhibitTreeSelection = false;
                List<TreeItem> treeItems = getVisibleExpandedItems(tree);
                if (treeItems.size() == 0) {
                    return;
                }
                TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                tree.setTopItem(treeItem);
            }
        });

        // ensure alignment of top item between tree and time graph
        fTimeGraphViewer.getVerticalBar().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                List<TreeItem> treeItems = getVisibleExpandedItems(tree);
                if (treeItems.size() == 0) {
                    return;
                }
                TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                tree.setTopItem(treeItem);
            }
        });

        // ensure alignment of top item between tree and time graph
        fTimeGraphViewer.getTimeGraphControl().addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent e) {
                List<TreeItem> treeItems = getVisibleExpandedItems(tree);
                if (treeItems.size() == 0) {
                    return;
                }
                TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
                tree.setTopItem(treeItem);
            }
        });

        // ensure the tree has focus control when mouse is over it if the time graph had control
        fTreeViewer.getControl().addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(MouseEvent e) {
                if (fTimeGraphViewer.getTimeGraphControl().isFocusControl()) {
                    fTreeViewer.getControl().setFocus();
                }
            }
        });

        // ensure the time graph has focus control when mouse is over it if the tree had control
        fTimeGraphViewer.getTimeGraphControl().addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(MouseEvent e) {
                if (fTreeViewer.getControl().isFocusControl()) {
                    fTimeGraphViewer.getTimeGraphControl().setFocus();
                }
            }
        });
        fTimeGraphViewer.getTimeGraphScale().addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(MouseEvent e) {
                if (fTreeViewer.getControl().isFocusControl()) {
                    fTimeGraphViewer.getTimeGraphControl().setFocus();
                }
            }
        });

        // The filler rows are required to ensure alignment when the tree does not have a
        // visible horizontal scroll bar. The tree does not allow its top item to be set
        // to a value that would cause blank space to be drawn at the bottom of the tree.
        fNumFillerRows = Display.getDefault().getBounds().height / getItemHeight(tree);

        sash.setWeights(new int[] { 1, 1 });
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns this time graph combo's tree viewer.
     *
     * @return the tree viewer
     */
    public TreeViewer getTreeViewer() {
        return fTreeViewer;
    }

    /**
     * Returns this time graph combo's time graph viewer.
     *
     * @return the time graph viewer
     */
    public TimeGraphViewer getTimeGraphViewer() {
        return fTimeGraphViewer;
    }

    /**
     * Callback for the show filter action
     *
     * @since 2.0
     */
    public void showFilterDialog() {
        if(fTopInput != null) {
            List<? extends ITimeGraphEntry> allElements = listAllInputs(fTopInput);
            fFilterDialog.setInput(fTopInput.toArray(new ITimeGraphEntry[0]));
            fFilterDialog.setTitle(Messages.TmfTimeFilterDialog_WINDOW_TITLE);
            fFilterDialog.setMessage(Messages.TmfTimeFilterDialog_MESSAGE);
            fFilterDialog.setExpandedElements(allElements.toArray());
            if (fFilter.getFiltered() != null) {
                ArrayList<? extends ITimeGraphEntry> nonFilteredElements = new ArrayList<ITimeGraphEntry>(allElements);
                nonFilteredElements.removeAll(fFilter.getFiltered());
                fFilterDialog.setInitialElementSelections(nonFilteredElements);
            } else {
                fFilterDialog.setInitialElementSelections(allElements);
            }
            fFilterDialog.create();
            fFilterDialog.open();
            // Process selected elements
            if (fFilterDialog.getResult() != null) {
                fInhibitTreeSelection = true;
                if (fFilterDialog.getResult().length != allElements.size()) {
                    ArrayList<Object> filteredElements = new ArrayList<Object>(allElements);
                    filteredElements.removeAll(Arrays.asList(fFilterDialog.getResult()));
                    fFilter.setFiltered(filteredElements);
                } else {
                    fFilter.setFiltered(null);
                }
                fTreeViewer.refresh();
                fTreeViewer.expandAll();
                fTimeGraphViewer.refresh();
                fInhibitTreeSelection = false;
                // Reset selection to first entry
                if (fFilterDialog.getResult().length > 0) {
                    setSelection((ITimeGraphEntry) fFilterDialog.getResult()[0]);
                }
            }
        }
    }

    /**
     * Get the show filter action.
     *
     * @return The Action object
     * @since 2.0
     */
    public Action getShowFilterAction() {
        if (showFilterAction == null) {
            // showFilter
            showFilterAction = new Action() {
                @Override
                public void run() {
                    showFilterDialog();
                }
            };
            showFilterAction.setText(Messages.TmfTimeGraphCombo_FilterActionNameText);
            showFilterAction.setToolTipText(Messages.TmfTimeGraphCombo_FilterActionToolTipText);
            // TODO find a nice, distinctive icon
            showFilterAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_FILTERS));
        }

        return showFilterAction;
    }

    // ------------------------------------------------------------------------
    // Control
    // ------------------------------------------------------------------------

    @Override
    public void redraw() {
        fTimeGraphViewer.getControl().redraw();
        super.redraw();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Sets the tree content provider used by this time graph combo.
     *
     * @param contentProvider the tree content provider
     */
    public void setTreeContentProvider(ITreeContentProvider contentProvider) {
        fTreeViewer.setContentProvider(new TreeContentProviderWrapper(contentProvider));
    }

    /**
     * Sets the tree label provider used by this time graph combo.
     *
     * @param labelProvider the tree label provider
     */
    public void setTreeLabelProvider(ITableLabelProvider labelProvider) {
        fTreeViewer.setLabelProvider(new TreeLabelProviderWrapper(labelProvider));
    }

    /**
     * Sets the tree content provider used by the filter dialog
     *
     * @param contentProvider the tree content provider
     * @since 2.0
     */
    public void setFilterContentProvider(ITreeContentProvider contentProvider) {
        fFilterDialog.setContentProvider(contentProvider);
    }

    /**
     * Sets the tree label provider used by the filter dialog
     *
     * @param labelProvider the tree label provider
     * @since 2.0
     */
    public void setFilterLabelProvider(ITableLabelProvider labelProvider) {
        fFilterDialog.setLabelProvider(labelProvider);
    }

    /**
     * Sets the tree columns for this time graph combo.
     *
     * @param columnNames the tree column names
     */
    public void setTreeColumns(String[] columnNames) {
        final Tree tree = fTreeViewer.getTree();
        for (String columnName : columnNames) {
            TreeColumn column = new TreeColumn(tree, SWT.LEFT);
            column.setText(columnName);
            column.pack();
        }
    }

    /**
     * Sets the tree columns for this time graph combo's filter dialog.
     *
     * @param columnNames the tree column names
     * @since 2.0
     */
    public void setFilterColumns(String[] columnNames) {
        fFilterDialog.setColumnNames(columnNames);
    }

    /**
     * Sets the time graph provider used by this time graph combo.
     *
     * @param timeGraphProvider the time graph provider
     */
    public void setTimeGraphProvider(ITimeGraphPresentationProvider timeGraphProvider) {
        fTimeGraphViewer.setTimeGraphProvider(timeGraphProvider);
    }

    /**
     * Sets or clears the input for this time graph combo.
     * The input array should only contain top-level elements.
     *
     * @param input the input of this time graph combo, or <code>null</code> if none
     */
    public void setInput(ITimeGraphEntry[] input) {
        fTopInput = new ArrayList<ITimeGraphEntry>(Arrays.asList(input));
        fFilter.setFiltered(null);
        fInhibitTreeSelection = true;
        fTreeViewer.setInput(input);
        for (SelectionListenerWrapper listenerWrapper : fSelectionListenerMap.values()) {
            listenerWrapper.selection = null;
        }
        fInhibitTreeSelection = false;
        fTreeViewer.expandAll();
        fTreeViewer.getTree().getVerticalBar().setEnabled(false);
        fTreeViewer.getTree().getVerticalBar().setVisible(false);
        fTimeGraphViewer.setItemHeight(getItemHeight(fTreeViewer.getTree()));
        fTimeGraphViewer.setInput(input);
    }

    /**
     * @param filter The filter object to be attached to the view
     * @since 2.0
     */
    public void addFilter(ViewerFilter filter) {
        ViewerFilter wrapper = new ViewerFilterWrapper(filter);
        fTreeViewer.addFilter(wrapper);
        fTimeGraphViewer.addFilter(wrapper);
        fViewerFilterMap.put(filter, wrapper);
    }

    /**
     * @param filter The filter object to be removed from the view
     * @since 2.0
     */
    public void removeFilter(ViewerFilter filter) {
        ViewerFilter wrapper = fViewerFilterMap.get(filter);
        fTreeViewer.removeFilter(wrapper);
        fTimeGraphViewer.removeFilter(wrapper);
        fViewerFilterMap.remove(filter);
    }

    /**
     * Refreshes this time graph completely with information freshly obtained from its model.
     */
    public void refresh() {
        fInhibitTreeSelection = true;
        fTreeViewer.refresh();
        fTimeGraphViewer.refresh();
        fInhibitTreeSelection = false;
    }

    /**
     * Adds a listener for selection changes in this time graph combo.
     *
     * @param listener a selection listener
     */
    public void addSelectionListener(ITimeGraphSelectionListener listener) {
        SelectionListenerWrapper listenerWrapper = new SelectionListenerWrapper(listener);
        fTreeViewer.addSelectionChangedListener(listenerWrapper);
        fSelectionListenerMap.put(listener, listenerWrapper);
        fTimeGraphViewer.addSelectionListener(listenerWrapper);
    }

    /**
     * Removes the given selection listener from this time graph combo.
     *
     * @param listener a selection changed listener
     */
    public void removeSelectionListener(ITimeGraphSelectionListener listener) {
        SelectionListenerWrapper listenerWrapper = fSelectionListenerMap.remove(listener);
        fTreeViewer.removeSelectionChangedListener(listenerWrapper);
        fTimeGraphViewer.removeSelectionListener(listenerWrapper);
    }

    /**
     * Sets the current selection for this time graph combo.
     *
     * @param selection the new selection
     */
    public void setSelection(ITimeGraphEntry selection) {
        fTimeGraphViewer.setSelection(selection);
        fInhibitTreeSelection = true; // block the tree selection changed listener
        if (selection != null) {
            StructuredSelection structuredSelection = new StructuredSelection(selection);
            fTreeViewer.setSelection(structuredSelection);
        } else {
            fTreeViewer.setSelection(new StructuredSelection());
        }
        fInhibitTreeSelection = false;
        List<TreeItem> treeItems = getVisibleExpandedItems(fTreeViewer.getTree());
        if (treeItems.size() == 0) {
            return;
        }
        TreeItem treeItem = treeItems.get(fTimeGraphViewer.getTopIndex());
        fTreeViewer.getTree().setTopItem(treeItem);
    }

    /**
     * Set the expanded state of an entry
     *
     * @param entry
     *            The entry to expand/collapse
     * @param expanded
     *            True for expanded, false for collapsed
     *
     * @since 2.0
     */
    public void setExpandedState(ITimeGraphEntry entry, boolean expanded) {
        fTimeGraphViewer.setExpandedState(entry, expanded);
        fTreeViewer.setExpandedState(entry, expanded);
    }

    /**
     * Collapses all nodes of the viewer's tree, starting with the root.
     *
     * @since 2.0
     */
    public void collapseAll() {
        fTimeGraphViewer.collapseAll();
        fTreeViewer.collapseAll();
    }

    /**
     * Expands all nodes of the viewer's tree, starting with the root.
     *
     * @since 2.0
     */
    public void expandAll() {
        fTimeGraphViewer.expandAll();
        fTreeViewer.expandAll();
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    private List<TreeItem> getVisibleExpandedItems(Tree tree) {
        ArrayList<TreeItem> items = new ArrayList<TreeItem>();
        for (TreeItem item : tree.getItems()) {
            if (item.getData() == FILLER) {
                break;
            }
            items.add(item);
            if (item.getExpanded()) {
                items.addAll(getVisibleExpandedItems(item));
            }
        }
        return items;
    }

    private List<TreeItem> getVisibleExpandedItems(TreeItem treeItem) {
        ArrayList<TreeItem> items = new ArrayList<TreeItem>();
        for (TreeItem item : treeItem.getItems()) {
            items.add(item);
            if (item.getExpanded()) {
                items.addAll(getVisibleExpandedItems(item));
            }
        }
        return items;
    }

    /**
     * Explores the list of top-level inputs and returns all the inputs
     *
     * @param inputs The top-level inputs
     * @return All the inputs
     */
    private List<? extends ITimeGraphEntry> listAllInputs(List<? extends ITimeGraphEntry> inputs) {
        ArrayList<ITimeGraphEntry> items = new ArrayList<ITimeGraphEntry>();
        for (ITimeGraphEntry entry : inputs) {
            items.add(entry);
            if (entry.hasChildren()) {
                items.addAll(listAllInputs(entry.getChildren()));
            }
        }
        return items;
    }

    private int getItemHeight(final Tree tree) {
        /*
         * Bug in Linux.  The method getItemHeight doesn't always return the correct value.
         */
        if (fLinuxItemHeight >= 0 && System.getProperty("os.name").contains("Linux")) { //$NON-NLS-1$ //$NON-NLS-2$
            if (fLinuxItemHeight != 0) {
                return fLinuxItemHeight;
            }
            List<TreeItem> treeItems = getVisibleExpandedItems(tree);
            if (treeItems.size() > 1) {
                final TreeItem treeItem0 = treeItems.get(0);
                final TreeItem treeItem1 = treeItems.get(1);
                PaintListener paintListener = new PaintListener() {
                    @Override
                    public void paintControl(PaintEvent e) {
                        tree.removePaintListener(this);
                        int y0 = treeItem0.getBounds().y;
                        int y1 = treeItem1.getBounds().y;
                        int itemHeight = y1 - y0;
                        if (itemHeight > 0) {
                            fLinuxItemHeight = itemHeight;
                            fTimeGraphViewer.setItemHeight(itemHeight);
                        }
                    }
                };
                tree.addPaintListener(paintListener);
            }
        } else {
            fLinuxItemHeight = -1; // Not Linux, don't perform os.name check anymore
        }
        return tree.getItemHeight();
    }

}
