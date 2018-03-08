/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * @since 3.0
 */
public class TmfAnalysisView extends TmfProjectModelElement {

    private String fViewId;

    /**
     * Constructor
     *
     * @param name
     *            Name of the view
     * @param resource
     *            Resource for the view
     * @param parent
     *            Parent analysis of the view
     * @param id
     *            The view id
     */
    protected TmfAnalysisView(String name, IResource resource, ITmfProjectModelElement parent, String id) {
        super(name, resource, parent);
        fViewId = id;
    }

    @Override
    public TmfProjectElement getProject() {
        return (TmfProjectElement) getParent();
    }

    /**
     * Gets the view id
     *
     * @return The view id
     */
    public String getViewId() {
        return fViewId;
    }

    /**
     * Gets the icon of the view
     *
     * @return The view icon
     */
    public Image getIcon() {
        IViewDescriptor descr = PlatformUI.getWorkbench().getViewRegistry().find(
                fViewId);
        if (descr != null) {
            return descr.getImageDescriptor().createImage();
        }
        return null;
    }

}
