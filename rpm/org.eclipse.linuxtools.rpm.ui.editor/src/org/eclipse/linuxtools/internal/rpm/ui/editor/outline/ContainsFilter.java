/*******************************************************************************
 * Copyright (c) 2008 Alexander Kurtakov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.outline;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Simple filter that only check whether the label contains the given string to
 * look for.
 */
public class ContainsFilter extends ViewerFilter {

    private String lookFor;

    public void setLookFor(String lookFor) {
        this.lookFor = lookFor;
    }

    /**
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (!(viewer instanceof TreeViewer)) {
            return true;
        }
        TreeViewer treeViewer = (TreeViewer) viewer;
        String currentLabel = ((ILabelProvider) treeViewer.getLabelProvider())
                .getText(element);
        if (lookFor == null) {
            return true;
        }
        if (currentLabel != null && currentLabel.contains(lookFor)) {
            return true;
        }
        return hasUnfilteredChild(treeViewer, element);
    }

    private boolean hasUnfilteredChild(TreeViewer viewer, Object element) {
        Object[] children = ((ITreeContentProvider) viewer.getContentProvider())
                .getChildren(element);
        for (Object child :children) {
            if (select(viewer, element, child)) {
                return true;
            }
        }
        return false;
    }
}
