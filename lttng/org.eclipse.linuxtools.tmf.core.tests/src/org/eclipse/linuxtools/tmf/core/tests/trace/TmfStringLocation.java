/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

/**
 * <b><u>TmfStringLocation</u></b>
 * <p>
 * Implement me. Please.
 * <p>
 */
@SuppressWarnings("javadoc")
public class TmfStringLocation extends TmfLocation {

    public TmfStringLocation(String location) {
        super(location);
    }

    public TmfStringLocation(TmfStringLocation other) {
        super(other.getLocationData());
    }

    @Override
    public TmfStringLocation clone() {
        return new TmfStringLocation((String) getLocationData());
    }

}
