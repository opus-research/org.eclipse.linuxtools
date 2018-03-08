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

package org.eclipse.linuxtools.tmf.ui.viewers;

import org.eclipse.linuxtools.tmf.core.component.ITmfComponent;
import org.eclipse.swt.widgets.Control;

/**
 * Interface to viewers.
 *
 * @author Mathieu Denis
 * @since 2.0
 */
public interface ITmfViewer extends ITmfComponent {
    /**
     * Returns the primary control associated with this viewer.
     *
     * @return the SWT control which displays this viewer's content
     */
    public Control getControl();
}
