/*******************************************************************************
 * Copyright (c) 2010, 2011, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider implementation for trace folders for tree viewers that display
 * the content of a trace folder.
 * <p>
 * 
 * @version 1.0
 * @author Francois Chouinard 
 */
public class TraceFolderContentProvider implements IStructuredContentProvider {

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof TmfTraceFolder) {
            TmfTraceFolder folder = (TmfTraceFolder) inputElement;
            List<ITmfProjectModelElement> elements = new ArrayList<ITmfProjectModelElement>();
            for (ITmfProjectModelElement element : folder.getChildren()) {
                if (element instanceof TmfTraceElement) {
                    TmfTraceElement trace = (TmfTraceElement) element;
                    if (trace.getTraceType() != null) {
                        elements.add(trace);
                    }
                }
            }
            return elements.toArray();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

}
