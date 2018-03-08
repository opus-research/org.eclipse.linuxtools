/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.viewers.xycharts;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;

/**
 * SimpleDateFormat class that can be used for SWT chart time
 * display together with the TmfTimestampFormat implementation.
 *
 * @author Bernd Hufmann
 * @since 2.1
 */
public class TmfChartTimeStampFormat extends SimpleDateFormat {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final long serialVersionUID = 3719743469686142387L;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private long fOffset;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Standard Constructor
     *
     * @param offset
     *        offset to apply before formating time (time = time + offset)
     */
    public TmfChartTimeStampFormat(long offset) {
        fOffset = offset;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {

        long time = date.getTime() + fOffset;
        toAppendTo.append(TmfTimestampFormat.getDefaulTimeFormat().format(time));
        return toAppendTo;
    }

}
