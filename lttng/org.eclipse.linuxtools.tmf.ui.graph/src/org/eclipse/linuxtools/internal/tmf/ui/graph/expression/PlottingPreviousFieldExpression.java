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
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;

/**
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class PlottingPreviousFieldExpression extends PlottingExpression {

    private final String fieldName;

    public PlottingPreviousFieldExpression(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public double getValue(int rank, ITmfEvent event, ITmfEvent previous) {
        if (previous == null)
            return 0;
        ITmfEventField field = previous.getContent().getField(fieldName);
        if (field == null)
            return 0;
        Object o = field.getValue();
        if (o instanceof Number) {
            Number n = (Number) o;
            return n.doubleValue();
        }
        String s = String.valueOf(o);
        try {
            double d = Double.parseDouble(s);
            return d;
        } catch (NumberFormatException _) {
        }
        return 0;
    }

}
