/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Intial API and Implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics.model;

/**
 * Primitive container for Statistics data
 *
 * Contains information about statistics that can be retrieved with any type of
 * traces
 *
 * @version 2.0
 * @since 2.0
 * @author Mathieu Denis
 */
public class TmfStatistics {
    /**
     * Number of events.
     */
    public long nbEvents = 0;
}
