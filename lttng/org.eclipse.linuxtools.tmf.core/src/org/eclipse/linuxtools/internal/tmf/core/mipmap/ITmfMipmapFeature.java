/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Christian Kouamé - Initial API and implementation
 *     Patrick Tasse - Updates to mipmap feature
 *******************************************************************************/
package org.eclipse.linuxtools.internal.tmf.core.mipmap;

import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;

/**
 * @author Jean-Christian Kouamé
 *
 */
public interface ITmfMipmapFeature {

    /**
     * Update the mipmap with a new state value.
     *
     * @param value
     *            The new state value
     * @param ts
     *            The timestamp of the event
     */
    public void updateMipmap(ITmfStateValue value, long ts);

    /**
     * Update the mipmap values at all levels before closing.
     */
    public void updateAndCloseMipmap();
}
