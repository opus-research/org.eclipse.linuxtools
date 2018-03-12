/*******************************************************************************
 * Copyright (c) 2014 Inria
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Generoso Pagano, Inria - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * A <code>FilteredTree</code> wrapping a <code>CheckboxTreeViewer</code>.
 *
 * This tree stores all the tree elements internally, and keeps the check state
 * in sync. This way, even if an element is filtered, the caller can get and set
 * the checked state. The internal representation is additive; that is, elements
 * are never removed from the internal representation.
 *
 * The class is not public because it is customized in order to be used with the
 * <code>TimeGraphFilterDialog</code>. The code is inspired from the non public
 * <code>org.eclipse.pde.internal.ui.launcher.FilteredCheckboxTree</code>.
 *
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * @since 3.1
 */
class FilteredCheckboxTree extends FilteredTree {

    /**
     * Create a new instance of the receiver.
     *
     * @param parent
     *            the parent <code>Composite</code>
     * @param treeStyle
     *            the style bits for the <code>Tree</code>
     * @param filter
     *            the filter to be used
     * @param useNewLook
     *            <code>true</code> if the new <code>FilteredTree</code> look
     *            should be used
     */
    public FilteredCheckboxTree(Composite parent, int treeStyle, PatternFilter filter,
            boolean useNewLook) {
        super(parent, treeStyle, filter, useNewLook);
    }

    @Override
    protected TreeViewer doCreateTreeViewer(Composite parentComposite, int style) {
        treeViewer = new FilterableCheckboxTreeViewer(parentComposite, style);
        return treeViewer;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if ((filterText.getStyle() & SWT.ICON_CANCEL) == 0) {
            // filter uses FilteredTree new look, not native
            int filterColor = enabled ? SWT.COLOR_LIST_BACKGROUND : SWT.COLOR_WIDGET_BACKGROUND;
            filterComposite.setBackground(getDisplay().getSystemColor(filterColor));
        }
        filterText.setEnabled(enabled);
        treeViewer.getTree().setEnabled(enabled);
    }

    /**
     * A <code>CheckboxTreeViewer</code> that keeps the check state of all the
     * nodes.
     */
    private class FilterableCheckboxTreeViewer extends CheckboxTreeViewer {

        /**
         * Map each tree element with the corresponding metadata
         */
        private Map<Object, CheckboxTreeItem> fObjects = new HashMap<>();

        /**
         * Constructor
         *
         * @param parent
         *            parent composite
         * @param style
         *            tree style
         */
        public FilterableCheckboxTreeViewer(Composite parent, int style) {
            super(parent, style);
            addCheckStateListener(new ICheckStateListener() {
                @Override
                public void checkStateChanged(CheckStateChangedEvent event) {
                    if (fObjects.containsKey(event.getElement())) {
                        fObjects.get(event.getElement()).checked = event.getChecked();
                    }
                }
            });
        }

        @Override
        protected void unmapAllElements() {
            fObjects = new HashMap<>();
            super.unmapAllElements();
        }

        @Override
        public boolean getChecked(Object element) {
            if (fObjects.containsKey(element)) {
                return fObjects.get(element).checked;
            }
            return super.getChecked(element);
        }

        @Override
        public Object[] getCheckedElements() {
            List<Object> checked = new ArrayList<>();
            for (CheckboxTreeItem item : fObjects.values()) {
                if (item.checked) {
                    checked.add(item.element);
                }
            }
            return checked.toArray();
        }

        @Override
        public boolean setChecked(Object element, boolean state) {
            if (fObjects.containsKey(element)) {
                if (!state || (testFindItem(element) != null)) {
                    CheckboxTreeItem checkboxTreeItem = fObjects.get(element);
                    checkboxTreeItem.checked = state;
                }
            }
            return super.setChecked(element, state);
        }

        @Override
        public void setCheckedElements(Object[] elements) {
            Set<Object> s = new HashSet<>(fObjects.keySet());
            s.removeAll(new HashSet<>(Arrays.asList(elements)));
            for (int i = 0; i < elements.length; i++) {
                CheckboxTreeItem checkboxTreeItem = fObjects.get(elements[i]);
                if (checkboxTreeItem != null) {
                    checkboxTreeItem.checked = true;
                }
            }
            for (Iterator<Object> iterator = s.iterator(); iterator.hasNext();) {
                Object object = iterator.next();
                CheckboxTreeItem checkboxTreeItem = fObjects.get(object);
                if (checkboxTreeItem != null) {
                    checkboxTreeItem.checked = false;
                }
            }
            super.setCheckedElements(elements);
        }

        @Override
        public boolean setSubtreeChecked(Object element, boolean state) {
            TreeItem item = (TreeItem) testFindItem(element);
            CheckboxTreeItem checkboxTreeItem = fObjects.get(element);
            if (item != null && checkboxTreeItem != null) {
                checkboxTreeItem.checked = state;
                TreeItem[] items = item.getItems();
                for (int i = 0; i < items.length; i++) {
                    item = items[i];
                    if (item != null) {
                        checkboxTreeItem = fObjects.get(item.getData());
                        if (checkboxTreeItem != null) {
                            checkboxTreeItem.checked = state;
                        }
                    }
                }
            }
            return super.setSubtreeChecked(element, state);
        }

        @Override
        protected void preservingSelection(Runnable updateCode) {
            super.preservingSelection(updateCode);
            // Re-apply the checked state
            ArrayList<TreeItem> allTreeItems = getAllTreeItems(treeViewer.getTree().getItems());
            for (Iterator<TreeItem> iterator = allTreeItems.iterator(); iterator.hasNext();) {
                TreeItem item = iterator.next();
                doApplyCheckedState(item, item.getData());
            }
        }

        @Override
        protected void internalRefresh(Object element, boolean updateLabels) {
            saveCheckedState();
            super.internalRefresh(element, updateLabels);
            treeViewer.expandAll();
        }

        /**
         * Set the checked state to a tree item
         *
         * @param item
         *            the tree item
         * @param element
         *            the element corresponding to the tree item
         */
        private void doApplyCheckedState(Item item, Object element) {

            if (item == null || element == null) {
                return;
            }

            // update the item first
            super.doUpdateItem(item, element);

            // Update the checked state
            TreeItem treeItem = (TreeItem) item;
            if (fObjects.containsKey(element)) {
                treeItem.setChecked(fObjects.get(element).checked);
            }
        }

        /**
         * A helper method to recursively get all the items in the tree
         *
         * @param roots
         *            tree roots
         * @return all the tree items starting from the given roots
         */
        private ArrayList<TreeItem> getAllTreeItems(TreeItem[] roots) {
            ArrayList<TreeItem> list = new ArrayList<>();
            for (int i = 0; i < roots.length; i++) {
                TreeItem item = roots[i];
                list.add(item);
                list.addAll(getAllTreeItems(item.getItems()));
            }
            return list;
        }

        /**
         * Saves the checked state of all the elements in the tree
         */
        private void saveCheckedState() {
            TreeItem[] items = treeViewer.getTree().getItems();
            for (int i = 0; i < items.length; i++) {
                TreeItem item = items[i];
                if (!fObjects.containsKey(item.getData())) {
                    new CheckboxTreeItem(item.getData(), item.getChecked(), null);
                }
                CheckboxTreeItem checkboxTreeItem = fObjects.get(item.getData());
                checkboxTreeItem.checked = item.getChecked();
                saveCheckedState(checkboxTreeItem, item);
            }
        }

        /**
         * Saves the checked state of an item and all its children
         *
         * @param chekboxTreeItem
         *            item whose state must be saved
         * @param parentItem
         *            parent tree item
         */
        private void saveCheckedState(CheckboxTreeItem chekboxTreeItem, TreeItem parentItem) {
            TreeItem[] items = parentItem.getItems();
            for (int i = 0; i < items.length; i++) {
                TreeItem item = items[i];
                if (!fObjects.containsKey(item.getData())) {
                    new CheckboxTreeItem(item.getData(), item.getChecked(), chekboxTreeItem);
                }
                CheckboxTreeItem checkboxTreeItem = fObjects.get(item.getData());
                checkboxTreeItem.checked = item.getChecked();
                saveCheckedState(checkboxTreeItem, item);
            }
        }

        /**
         * Utility class representing the metadata for a filterable checkbox
         * tree item.
         */
        private class CheckboxTreeItem {
            public Object element;
            public boolean checked;
            public List<CheckboxTreeItem> children = new ArrayList<>();

            public CheckboxTreeItem(Object element, boolean checked, CheckboxTreeItem parent) {
                this.element = element;
                this.checked = checked;
                fObjects.put(element, this);
                if (parent != null) {
                    parent.children.add(this);
                }
            }
        }
    }
}
