/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author ematkho
 * @since 2.0
 *
 */
public class ImportTraceLabelProvider extends LabelProvider {



    @Override
    public String getText(Object element) {
        if (element instanceof String) {
            return (String) element;
        }
        if (element instanceof FileAndName) {
            return ((FileAndName) element).getFile().getName();
        }
        return null;
    }
}
