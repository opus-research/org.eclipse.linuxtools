/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Extracted from TmfEventsView
 *   Alexandre Montplaisir - Added a tooltip config element
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.virtualtable;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The configuration element for a {@link TmfVirtualTable} column.
 *
 * @author Matthew Khouzam
 */
public class ColumnData {

    /** The title of the column */
    public final String header;

    /** The width of the column in pixels */
    public final int width;

    /** The alignment of the column */
    public final int alignment;

    /** The header's tooltip. 'null' for no special tooltip
     * @since 3.1 */
    public final @Nullable String tooltip;

    /**
     * Constructor with no tooltip
     *
     * @param header
     *            Column header (title)
     * @param width
     *            Column width
     * @param alignment
     *            Text alignment for this column
     */
    public ColumnData(String header, int width, int alignment) {
        this(header, width, alignment, null);
    }

    /**
     * Constructor with a header tooltip
     *
     * @param header
     *            Column header (title)
     * @param width
     *            Column width
     * @param alignment
     *            Text alignment for this column
     * @param tooltip
     *            The tooltip for this column header, 'null' for no tooltip
     * @since 3.1
     */
    public ColumnData(String header, int width, int alignment, @Nullable String tooltip) {
        this.header = header;
        this.width = width;
        this.alignment = alignment;
        this.tooltip = tooltip;
    }

}
