/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

/**
 * Base implementation of the ITmfHostProvider interface. The default host only
 * returns the name of the trace itself, so each trace is its own host
 *
 * @since 3.0
 */
public class TmfHostProvider implements ITmfHostProvider {

    private final ITmfTrace fTrace;

    /**
     * Constructor
     *
     * @param trace
     *            The trace
     */
    public TmfHostProvider(ITmfTrace trace) {
        fTrace = trace;
    }

    /**
     * Returns the trace
     *
     * @return The trace
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public String getHost() {
        return fTrace.getName();
    }

}
