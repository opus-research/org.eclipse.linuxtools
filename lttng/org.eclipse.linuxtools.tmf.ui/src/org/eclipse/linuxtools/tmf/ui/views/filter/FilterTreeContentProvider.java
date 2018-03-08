/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Yuriy Vashchuk - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.filter;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;

/**
 * This is the Content Provider of our tree
 *
 * @version 1.0
 * @author Yuriy Vashchuk
 */
public class FilterTreeContentProvider implements ITreeContentProvider {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
    public void dispose() {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	@Override
    public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ITmfFilterTreeNode) {
			ArrayList<ITmfFilterTreeNode> result = new ArrayList<ITmfFilterTreeNode>();
			for(int i = 0; i < ((ITmfFilterTreeNode)inputElement).getChildrenCount(); i++) {
				result.add(((ITmfFilterTreeNode)inputElement).getChild(i));
			}

			return result.toArray();
		}
        return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
    public Object[] getChildren(Object parentElement) {
		ArrayList<ITmfFilterTreeNode> result = new ArrayList<ITmfFilterTreeNode>();
		for(int i = 0; i < ((ITmfFilterTreeNode)parentElement).getChildrenCount(); i++) {
			result.add(((ITmfFilterTreeNode)parentElement).getChild(i));
		}
		return result.toArray();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
    public Object getParent(Object element) {
		return ((ITmfFilterTreeNode) element).getParent();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
    public boolean hasChildren(Object element) {
		return ((ITmfFilterTreeNode) element).hasChildren();
	}

}
