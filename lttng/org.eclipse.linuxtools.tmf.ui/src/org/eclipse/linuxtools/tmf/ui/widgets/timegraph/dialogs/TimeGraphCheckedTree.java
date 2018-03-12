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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * Utility class to keep track of the check status of all the elements of a
 * <code>CheckboxTreeViewer</code>, either they are visible or not. An object of
 * this class is therefore associated with an instance of
 * <code>CheckboxTreeViewer</code>, referred as the 'associated viewer' in the
 * documentation.
 *
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * @since 3.1
 */
public class TimeGraphCheckedTree {

    /**
     * The associated viewer
     */
    private CheckboxTreeViewer fViewer;

    /**
     * Associated viewer content provider
     */
    private ITreeContentProvider fContentProvider;

    /**
     * Associated viewer pattern filter
     */
    private TreePatternFilter fPatternFilter;

    /**
     * Map each tree element with the check status
     */
    private Map<Object, Boolean> fObjects;

    /* Initialization */

    /**
     * Set the associated viewer
     *
     * @param viewer
     *            the associated viewer
     */
    public void setViewer(CheckboxTreeViewer viewer) {
        fViewer = viewer;
    }

    /**
     * Set the associated viewer pattern filter
     *
     * @param filter
     *            the associated viewer pattern filter
     */
    public void setPatternFilter(TreePatternFilter filter) {
        fPatternFilter = filter;
    }

    /**
     * Set the input of the associated viewer. All the elements are unchecked by
     * default.
     *
     * @param input
     *            the input
     */
    public void setInput(Object input) {
        Assert.isTrue(fContentProvider != null, "TimeGraphCheckedTree must have a content provider when input is set."); //$NON-NLS-1$
        fObjects = new HashMap<>();
        for (Object o : fContentProvider.getElements(input)) {
            putObject(o);
        }
    }

    /**
     * Recursively puts into the object map all the elements of the input.
     *
     * @param o
     *            currently processed object
     */
    private void putObject(Object o) {
        fObjects.put(o, false);
        for (Object s : fContentProvider.getChildren(o)) {
            putObject(s);
        }
    }

    /**
     * Set the content provider of the associated viewer.
     *
     * @param contentProvider
     *            the content provider
     */
    public void setContentProvider(ITreeContentProvider contentProvider) {
        fContentProvider = contentProvider;
    }

    /* Check status management */

    /**
     * Set the checked status for the given element. Leave the other elements
     * untouched.
     *
     * @param element
     *            the element
     * @param status
     *            <code>true</code> if the item should be checked, and
     *            <code>false</code> if it should be unchecked
     */
    public void setChecked(Object element, boolean status) {
        fObjects.put(element, status);
    }

    /**
     * Check the given elements. Leave the other elements untouched.
     *
     * @param elements
     *            the elements
     */
    public void checkElements(Object[] elements) {
        for (Object o : elements) {
            fObjects.put(o, true);
        }
    }

    /**
     * Check the given element and all its visible children. Leave the other
     * elements untouched.
     *
     * @param element
     *            the element
     */
    public void checkSubtree(Object element) {
        if (!fPatternFilter.isElementVisible(fViewer, element)) {
            return;
        }
        fObjects.put(element, true);
        for (Object o : fContentProvider.getChildren(element)) {
            checkSubtree(o);
        }
    }

    /**
     * Uncheck all the visible elements of the viewer input. Note that if a
     * visible root node is unchecked, all the children (either visible or not)
     * are unchecked. Leave the other elements untouched.
     */
    public void uncheckAll() {
        for (Object root : fContentProvider.getElements(fViewer.getInput())) {
            if (fPatternFilter.isElementVisible(fViewer, root)) {
                recursiveUncheck(root);
            }
        }
    }

    /**
     * Recursively uncheck the given element and all its children.
     *
     * @param obj
     *            the element to uncheck
     */
    private void recursiveUncheck(Object obj) {
        fObjects.put(obj, false);
        for (Object o : fContentProvider.getChildren(obj)) {
            recursiveUncheck(o);
        }
    }

    /**
     * Get all the checked elements, either visible or not.
     *
     * @return an array containing all the checked elements
     */
    public Object[] getCheckedElements() {
        List<Object> checked = new LinkedList<>();
        for (Entry<Object, Boolean> e : fObjects.entrySet()) {
            if (e.getValue()) {
                checked.add(e.getKey());
            }
        }
        return checked.toArray();
    }

    /**
     * Check if a tree element is checked or not.
     *
     * @param elem
     *            the tree element
     * @return true if the element is checked, false otherwise
     */
    public boolean isChecked(Object elem) {
        if (fObjects.containsKey(elem)) {
            return fObjects.get(elem);
        }
        return false;
    }

}
