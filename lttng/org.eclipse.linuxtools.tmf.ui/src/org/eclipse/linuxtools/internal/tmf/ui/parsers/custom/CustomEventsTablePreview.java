/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Alexandre Montplaisir - Update for TmfEventTableColumn
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.swt.widgets.Composite;

/**
 * Events table for custom text parsers.
 *
 * This is only used for preview purposes in the wizard. The "real" event table
 * will be created by the events editor, using {@link CustomEventTableColumns}.
 *
 * @author Patrick Tassé
 */
public class CustomEventsTablePreview extends TmfEventsTable {

    /**
     * Constructor.
     *
     * @param definition
     *            Trace definition object
     * @param parent
     *            Parent composite of the view
     * @param cacheSize
     *            How many events to keep in cache
     */
    public CustomEventsTablePreview(CustomTraceDefinition definition, Composite parent, int cacheSize) {
        super(parent, cacheSize, CustomEventTableColumns.generateColumns(definition));
    }
}
