/*******************************************************************************
 * Copyright (c) 2013 Kalray
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xavier Raynaud - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.tmf.ui.graph.expression;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class PlottingTimestampExpression extends PlottingExpression {

    public PlottingTimestampExpression() {
    }

    @Override
    public double getValue(int rank, ITmfEvent event, ITmfEvent previous) {
        long eventTime = event.getTimestamp().normalize(0, -9).getValue();
        return eventTime;
    }

}
