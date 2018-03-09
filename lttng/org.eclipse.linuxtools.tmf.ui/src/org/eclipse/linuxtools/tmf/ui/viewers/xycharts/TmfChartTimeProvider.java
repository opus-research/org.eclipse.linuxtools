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

/**
 * A default implementation of a chart provider, returning 0 for all values
 *
 * @since 3.0
 */
public class TmfChartTimeProvider implements ITmfChartTimeProvider {

    @Override
    public long getStartTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getEndTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getWindowStartTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getWindowEndTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getWindowDuration() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getSelectedTime() {
        // TODO Auto-generated method stub
        return 0;
    }

}
