/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.synchronization;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * This class contains a formula to transform the value of a timestamp, for
 * example after trace synchronization
 *
 * @author gbastien
 * @since 2.0
 */
public interface ITmfTimestampTransform {

    /**
     * Transforms a timestamp
     *
     * @param timestamp The timestamp to transform
     * @return the transformed timestamp
     */
    public ITmfTimestamp transform(ITmfTimestamp timestamp);

    /**
     * Transforms a timestamp value
     *
     * @param timestamp The timestamp to transform
     * @return the transformed value
     */
    public long transform(long timestamp);

}
