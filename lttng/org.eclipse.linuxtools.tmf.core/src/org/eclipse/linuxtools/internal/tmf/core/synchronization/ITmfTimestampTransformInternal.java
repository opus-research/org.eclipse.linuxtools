/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.synchronization;

import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;

/**
 * Internal interface providing some additional functionnality to timestamp
 * transforms not meant to be exposed as public API.
 *
 * @author Geneviève Bastien
 */
public interface ITmfTimestampTransformInternal extends ITmfTimestampTransform {

    /**
     * Returns the inverse of this transform. The transform composed with its
     * inverse yields the identity.
     *
     * @return The inverse transform
     */
    ITmfTimestampTransform inverse();

}
