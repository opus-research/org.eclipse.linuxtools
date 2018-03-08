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
 * Interface for a trace host provider. Classes implementing this will provide a
 * host name for a trace, ie an identification of where the trace comes from
 *
 * @since 3.0
 */
public interface ITmfHostProvider {

    /**
     * Returns a host identifying string, representing the machine on which the
     * trace was taken
     *
     * @return The host name
     */
    String getHost();

}
