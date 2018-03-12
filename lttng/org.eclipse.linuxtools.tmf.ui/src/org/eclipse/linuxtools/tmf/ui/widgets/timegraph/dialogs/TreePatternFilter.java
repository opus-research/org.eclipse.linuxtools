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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * A filter used in conjunction with a <code>CheckboxTreeViewer</code>. In order
 * to determine if a node should be filtered it uses the content and label
 * provider of the tree to do pattern matching on its children. This causes the
 * entire tree structure to be realized. It works with all the label providers
 * supported by a <code>CheckboxTreeViewer</code>, namely
 * <code>ILabelProvider</code>, <code>ITableLabelProvider</code> and
 * <code>CellLabelProvider</code>. This code is mostly based on the
 * <code>org.eclipse.ui.dialogs.PatternFilter<code> by IBM. We had to create
 * this class since the original <code>PatternFilter</code> works only if the
 * label provider implements <code>ILabelProvider</code>.
 *
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * @since 3.1
 */
public class TreePatternFilter extends ViewerFilter {

    private static Object[] EMPTY = new Object[0];

    private String fPatternString = null;

    /*
     * Cache of filtered elements in the tree
     */
    private Map<Object, Object[]> fCache = new HashMap<>();

    /*
     * Maps parent elements to TRUE or FALSE
     */
    private Map<Object, Boolean> fFoundAnyCache = new HashMap<>();

    @Override
    public Object[] filter(Viewer viewer, Object parent, Object[] elements) {

        Object[] filtered = fCache.get(parent);
        if (filtered == null) {
            Boolean foundAny = fFoundAnyCache.get(parent);
            if (foundAny != null && !foundAny.booleanValue()) {
                filtered = EMPTY;
            } else {
                filtered = super.filter(viewer, parent, elements);
            }
            fCache.put(parent, filtered);
        }
        return filtered;
    }

    @Override
    public final boolean select(Viewer viewer, Object parentElement,
            Object element) {
        return isElementVisible(viewer, element);
    }

    /**
     * The pattern string for which this filter should select elements in the
     * viewer.
     *
     * @param patternString
     *            pattern to match
     */
    public void setPattern(String patternString) {
        clearCaches();
        if (patternString == null || patternString.equals("")) { //$NON-NLS-1$
            fPatternString = null;
        } else {
            fPatternString = ".*" + patternString + ".*";//$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Answers whether the given element in the given viewer matches the filter
     * pattern. This is a default implementation that will show a leaf element
     * in the tree based on whether the provided filter text matches the text of
     * the given element's text, or that of its children (if the element has
     * any), or that of one of its parents.
     *
     * Subclasses may override this method.
     *
     * @param viewer
     *            the tree viewer in which the element resides
     * @param element
     *            the element in the tree to check for a match
     *
     * @return true if the element matches the filter pattern
     */
    public boolean isElementVisible(Viewer viewer, Object element) {
        return isParentMatch(viewer, element) || isLeafMatch(viewer, element)
                || isChildMatch(viewer, element);
    }

    /**
     * Check if at least one of the children of this element is a match with the
     * filter text.
     *
     * Subclasses may override this method.
     *
     * @param viewer
     *            the viewer that contains the element
     * @param element
     *            the tree element to check
     * @return true if the given element has children that matches the filter
     *         text
     */
    private boolean isParentMatch(Viewer viewer, Object element) {
        Object[] children = ((ITreeContentProvider) ((AbstractTreeViewer) viewer)
                .getContentProvider()).getChildren(element);

        if ((children != null) && (children.length > 0)) {
            return isAnyVisible(viewer, element, children);
        }
        return false;
    }

    /**
     * Check if the current (leaf) element is a match with the filter text.
     *
     * @param viewer
     *            the viewer that contains the element
     * @param element
     *            the tree element to check
     * @return true if the given element's label matches the filter text
     */
    private boolean isLeafMatch(Viewer viewer, Object element) {
        String labelText = getLabel(((StructuredViewer) viewer).getLabelProvider(), element);
        if (labelText == null) {
            return false;
        }
        return wordMatches(labelText);
    }

    /**
     * Check if at least one of the parents of this element is a match with the
     * filter text.
     *
     * @param viewer
     *            the viewer that contains the element
     * @param element
     *            the tree element to check
     * @return true if the given element has a parent that matches the filter
     *         text
     */
    private boolean isChildMatch(Viewer viewer, Object element) {
        Object parent = ((ITreeContentProvider) ((AbstractTreeViewer) viewer)
                .getContentProvider()).getParent(element);
        while (parent != null) {
            if (isLeafMatch(viewer, parent)) {
                return true;
            }
            parent = ((ITreeContentProvider) ((AbstractTreeViewer) viewer)
                    .getContentProvider()).getParent(parent);
        }
        return false;
    }

    /**
     * Return whether or not any of the words in text satisfy the match
     * criteria.
     *
     * @param text
     *            the text to match
     * @return boolean <code>true</code> if one of the words in text satisfies
     *         the match criteria.
     */
    private boolean wordMatches(String text) {
        if (text == null) {
            return false;
        }

        try {
            // If the whole text matches we are all set
            if (match(text)) {
                return true;
            }
        } catch (PatternSyntaxException e) {
            return false;
        }

        // Otherwise check if any of the words of the text matches
        String[] words = getWords(text);
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (match(word)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if any of the elements makes it through the filter. This
     * method uses caching; the computation is done in computeAnyVisible.
     *
     * @param viewer
     *            the viewer
     * @param parent
     *            the parent element
     * @param elements
     *            the elements (must not be an empty array)
     * @return true if any of the elements makes it through the filter.
     */
    private boolean isAnyVisible(Viewer viewer, Object parent, Object[] elements) {
        Object[] filtered = fCache.get(parent);
        if (filtered != null) {
            return filtered.length > 0;
        }
        Boolean foundAny = fFoundAnyCache.get(parent);
        if (foundAny == null) {
            foundAny = computeAnyVisible(viewer, elements) ? Boolean.TRUE : Boolean.FALSE;
            fFoundAnyCache.put(parent, foundAny);
        }
        return foundAny.booleanValue();
    }

    /**
     * Returns true if any of the elements makes it through the filter.
     *
     * @param viewer
     *            the viewer
     * @param elements
     *            the elements to test
     * @return <code>true</code> if any of the elements makes it through the
     *         filter
     */
    private boolean computeAnyVisible(Viewer viewer, Object[] elements) {
        boolean elementFound = false;
        for (int i = 0; i < elements.length && !elementFound; i++) {
            Object element = elements[i];
            elementFound = isElementVisible(viewer, element);
        }
        return elementFound;
    }

    /**
     * Clears the caches used for optimizing this filter. Needs to be called
     * whenever the tree content changes.
     */
    private void clearCaches() {
        fCache.clear();
        fFoundAnyCache.clear();
    }

    /**
     * Answers whether the given String matches the pattern.
     *
     * @param string
     *            the String to check against the pattern
     * @return whether the string matches the pattern
     */
    private boolean match(String string) {
        if (fPatternString == null) {
            return true;
        }
        return string.matches(fPatternString);
    }

    /**
     * Get the element label using the given label provider. The label provider
     * must be one of the following: <code>ILabelProvider</code>,
     * <code>ITableLabelProvider</code>, <code>CellLabelProvider</code>.
     *
     * @param labelProvider
     *            the label provider
     * @param element
     *            the element
     * @return the element label
     */
    private static String getLabel(IBaseLabelProvider labelProvider, Object element) {
        if (labelProvider instanceof ITableLabelProvider) {
            return ((ITableLabelProvider) labelProvider).getColumnText(element, 0);
        } else if (labelProvider instanceof ILabelProvider) {
            return ((ILabelProvider) labelProvider).getText(element);
        } else if (labelProvider instanceof CellLabelProvider) {
            return ((CellLabelProvider) labelProvider).getToolTipText(element);
        }
        return null;
    }

    /**
     * Take the given text and break it down into words using a BreakIterator.
     *
     * @param text
     *            text to break into words
     * @return an array of words
     */
    private static String[] getWords(String text) {
        List<String> words = new ArrayList<>();
        BreakIterator iter = BreakIterator.getWordInstance();
        iter.setText(text);
        int i = iter.first();
        while (i != java.text.BreakIterator.DONE && i < text.length()) {
            int j = iter.following(i);
            if (j == java.text.BreakIterator.DONE) {
                j = text.length();
            }
            // match the word
            if (Character.isLetterOrDigit(text.charAt(i))) {
                String word = text.substring(i, j);
                words.add(word);
            }
            i = j;
        }
        return words.toArray(new String[words.size()]);
    }

}
