/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.synchronization;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventDependency;
import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.SynchronizationAlgorithm;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableMap.Builder;

/**
 * Synchronization algorithm offset
 *
 * @author Matthew Khouzam
 */
public class SyncAlgorithmOffset extends SynchronizationAlgorithm {

    /**
     *
     */
    private static final long serialVersionUID = 2298870935633276437L;
    private final Map<String, TmfConstantTransform> fTransformMap;

    /**
     * Constructor
     *
     * @param offsets
     *            a map of traces getPath() return to time offsets in nanoseconds
     */
    public SyncAlgorithmOffset(Map<String, Long> offsets) {
        Builder<String, TmfConstantTransform> builder = new Builder<>();
        for (Entry<String, Long> e : offsets.entrySet()) {
            builder.put(e.getKey(), new TmfConstantTransform(e.getValue()));
        }
        fTransformMap = builder.build();

    }

    @Override
    protected void processMatch(TmfEventDependency match) {
        // no matches
    }

    @Override
    public Map<String, Map<String, Object>> getStats() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public ITmfTimestampTransform getTimestampTransform(ITmfTrace trace) {
        return fTransformMap.get(trace.getPath());
    }

    @Override
    public ITmfTimestampTransform getTimestampTransform(String hostId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SyncQuality getSynchronizationQuality(ITmfTrace trace1, ITmfTrace trace2) {
        return SyncQuality.ACCURATE;
    }

    @Override
    public boolean isTraceSynced(String hostId) {
        return true;
    }

}
