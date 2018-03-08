/*****************************************************************************
 * Copyright (c) 2007, 2013 Intel Corporation, Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Added methods to save a time range selection
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;


/**
 * Extension of the ITimeDateProvider interface to avoid API breakage
 *
 * @version 1.0
 * @TODO: Move these to the ITimeDateProvider interface when API 3.0 is reached
 * @since 2.1
 */
public interface ITimeDataProvider2 extends ITimeDataProvider {

    void setSelectionStartFinishTime(long time0, long time1);

    /**
     * @return The start time of the current selection
     */
    long getSelectionStart();

    /**
     * @return The end time of the current selection
     */
    long getSelectionEnd();

}
