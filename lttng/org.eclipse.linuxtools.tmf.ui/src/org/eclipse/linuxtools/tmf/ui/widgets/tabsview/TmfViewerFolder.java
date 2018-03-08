/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.tabsview;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.linuxtools.tmf.ui.viewers.ITmfViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Allows the user to create multiple tabs which makes it look like folders. It
 * simplifies the management of the viewer contained in each tabs.
 *
 * The indexing of the viewers is based on their name.
 *
 * @author Mathieu Denis
 *
 * @param <V>
 *            Type of viewers that will be held in the tabs view
 * @since 2.0
 */
public class TmfViewerFolder<V extends ITmfViewer> extends Composite {

    /**
     * The list of viewers in the folder
     */
    private final HashMap<String, V> fViewers;

    /**
     * The parent folder that contains all viewers
     */
    private CTabFolder fFolder;

    /**
     * Standard constructor
     *
     * @param parent
     *            The parent composite
     */
    public TmfViewerFolder(Composite parent) {
        this(parent, SWT.NONE);
    }

    /**
     * Standard constructor
     *
     * @param parent
     *            The parent composite
     * @param style
     *            The style of the view that will be created
     */
    public TmfViewerFolder(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());

        fViewers = new HashMap<String, V>();
        fFolder = new CTabFolder(this, SWT.LEFT | SWT.BORDER);
        fFolder.setSimple(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        if (fFolder != null) {
            for (V viewer : fViewers.values()) {
                viewer.dispose();
            }
            fFolder.dispose();
        }
    }

    /**
     * Create a new tab that will hold the viewer content. The viewers name must
     * be unique.
     *
     * The parent of the viewer control must be the folder returned by
     * {@link #getParentFolder()}
     *
     * @param viewer
     *            the viewer to put in the new tab
     * @param style
     *            the style of the widget to build
     */
    public void addTab(V viewer, int style) {
        if (fFolder == null || viewer.getControl().getParent() != fFolder) {
            return;
        }
        CTabItem item = new CTabItem(fFolder, style);
        item.setText(viewer.getName());
        item.setControl(viewer.getControl());
        // Register the viewer in the map to dispose it at closing time
        fViewers.put(viewer.getName(), viewer);
    }

    /**
     * Gets the folder that will be use as the parent of tabs that will hold the
     * viewer.
     *
     * In order to be able to add new tabs in this view, the parent of the
     * viewer control has to be this composite.
     *
     * @return the folder composite to use as the parent for the viewer control
     *         to create.
     */
    public Composite getParentFolder() {
        return fFolder;
    }

    /**
     * Gets a viewer based on his name.
     *
     * @param viewerName
     *            The name of the viewer to find in the folder
     * @return The viewer which name is viewerName, or null if there is no such
     *         viewer
     */
    public V getViewer(String viewerName) {
        return fViewers.get(viewerName);
    }

    /**
     * Gets the viewers list contained in the folder view. The list can return
     * the viewers in any order. It is not to be assumed that the viewers are
     * returned in the same order as they were inserted.
     *
     * @return a collection of viewers contained in this view.
     */
    public Collection<V> getViewers() {
        return fViewers.values();
    }

    /**
     * Selects the tab at the specified index
     *
     * @param index
     *            The index of the tab to be selected
     * @throws SWTException
     *             <ul>
     *             <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed
     *             </li>
     *             <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *             thread that created the receiver</li>
     *             </ul>
     */
    public void setSelection(int index) throws SWTException {
        fFolder.setSelection(index);
    }
}
