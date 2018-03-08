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

/**
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public abstract class PlottingBinaryExpression extends PlottingExpression {

    protected final PlottingExpression leftExpression;
    protected final PlottingExpression rightExpression;

    public PlottingBinaryExpression(PlottingExpression leftExpression, PlottingExpression rightExpression) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

}
