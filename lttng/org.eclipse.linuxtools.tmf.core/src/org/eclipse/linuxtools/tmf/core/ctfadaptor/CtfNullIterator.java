/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

/**
 * Null Iterator
 *
 * @author Matthew Khouzam
 *
 */
class CtfNullIterator extends CtfIterator {

    /**
     * Constructor
     */
    public CtfNullIterator() {
        super();
    }

    @Override
    public boolean seek(long timestamp) {
        return false;
    }

    @Override
    public CtfIterator clone() {
        return this;
    }

    @Override
    public long getRank() {
        return -2;
    }

    @Override
    public CtfTmfEvent getCurrentEvent() {
        return null;
    }

    @Override
    public CtfLocation getLocation() {
        return NULL_LOCATION;
    }

    @Override
    public synchronized boolean advance() {
        return false;
    }

}
