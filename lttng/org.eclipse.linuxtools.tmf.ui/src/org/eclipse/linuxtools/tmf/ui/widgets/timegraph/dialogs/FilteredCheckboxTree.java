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
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A <code>FilteredTree</code> wrapping a <code>CheckboxTreeViewer</code>.
 *
 * This tree stores all the tree elements internally, and keeps the check state
 * in sync. This way, even if an element is filtered, the caller can get and set
 * the checked state. The internal representation is additive; that is, elements
 * are never removed from the internal representation.
 *
 * The class is not public because it is customized in order to be used with the
 * <code>TimeGraphFilterDialog</code>.
 *
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * @since 3.1
 */
class FilteredCheckboxTree extends FilteredTree implements ICheckable {

    /**
     * Map each tree element with the corresponding metadata
     */
    private Map<Object, Boolean> fObjects = new HashMap<>();

    /**
     * Handle to the tree viewer
     */
    private CheckboxTreeViewer fCheckboxTreeViewer;

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
        fCheckboxTreeViewer = new CheckboxTreeViewer(parentComposite, style);
        fCheckboxTreeViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                fObjects.put(event.getElement(), event.getChecked());
            }
        });
        treeViewer = fCheckboxTreeViewer;
        return treeViewer;
    }

    @Override
    protected WorkbenchJob doCreateRefreshJob() {
        WorkbenchJob job = super.doCreateRefreshJob();
        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                fCheckboxTreeViewer.expandAll();
                fCheckboxTreeViewer.setCheckedElements(getCheckedElements());
            }
        });
        return job;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if ((filterText.getStyle() & SWT.ICON_CANCEL) == 0) {
            // filter uses FilteredTree new look, not native
            int filterColor = enabled ? SWT.COLOR_LIST_BACKGROUND : SWT.COLOR_WIDGET_BACKGROUND;
            filterComposite.setBackground(getDisplay().getSystemColor(filterColor));
        }
        filterText.setEnabled(enabled);
        fCheckboxTreeViewer.getTree().setEnabled(enabled);
    }

    @Override
    public boolean getChecked(Object element) {
        if (fObjects.containsKey(element)) {
            return fObjects.get(element);
        }
        return fCheckboxTreeViewer.getChecked(element);
    }

    @Override
    public boolean setChecked(Object element, boolean state) {
        if (fObjects.containsKey(element)) {
            if (!state || (fCheckboxTreeViewer.testFindItem(element) != null)) {
                fObjects.put(element, state);
            }
        }
        return fCheckboxTreeViewer.setChecked(element, state);
    }

    @Override
    public void addCheckStateListener(ICheckStateListener listener) {
        fCheckboxTreeViewer.addCheckStateListener(listener);
    }

    @Override
    public void removeCheckStateListener(ICheckStateListener listener) {
        fCheckboxTreeViewer.addCheckStateListener(listener);
    }

    public Object[] getCheckedElements() {
        List<Object> checked = new ArrayList<>();
        for (Entry<Object, Boolean> e : fObjects.entrySet()) {
            if (e.getValue()) {
                checked.add(e.getKey());
            }
        }
        return checked.toArray();
    }

    public void setCheckedElements(Object[] elements) {
        Set<Object> s = new HashSet<>(fObjects.keySet());
        s.removeAll(new HashSet<>(Arrays.asList(elements)));
        for (int i = 0; i < elements.length; i++) {
            fObjects.put(elements[i], true);
        }
        for (Iterator<Object> iterator = s.iterator(); iterator.hasNext();) {
            fObjects.put(iterator.next(), false);
        }
        fCheckboxTreeViewer.setCheckedElements(elements);
    }

    public boolean setSubtreeChecked(Object element, boolean state) {
        checkSubtree(element, state);
        return fCheckboxTreeViewer.setSubtreeChecked(element, state);
    }

    private void checkSubtree(Object element, boolean state) {
        TreeItem item = (TreeItem) fCheckboxTreeViewer.testFindItem(element);
        if (!state || (item != null)) {
            fObjects.put(element, state);
            for (Object o : ((ITreeContentProvider) fCheckboxTreeViewer.getContentProvider()).getChildren(element)) {
                checkSubtree(o, state);
            }
        }
    }

}
