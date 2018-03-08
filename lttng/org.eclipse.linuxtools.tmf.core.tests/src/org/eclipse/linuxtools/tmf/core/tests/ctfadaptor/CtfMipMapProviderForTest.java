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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.statesystem.MipMapProvider;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;

/**
 * this is the mip map provider for CTF
 *
 * @author Jean-Christian Kouamé
 * @since 3.0
 */
public class CtfMipMapProviderForTest extends MipMapProvider {
    private final int DEFAULT_RANGE = 3;
    private int range;
    private final static String TEST_EVENT_NAME = "event"; //$NON-NLS-1$
    private final static String UST_TEST_EVENT_NAME = "ust_tests_hello:tptest"; //$NON-NLS-1$
    private final static String TEST_ATTRIBUTE_NAME = "test_attribute"; //$NON-NLS-1$
    private final static String TEST_FIELD_NAME = "value"; //$NON-NLS-1$
    private final static String CTF_MIPMAP_ID = "CTF_MIPMAP_ID"; //$NON-NLS-1$
    Map<String[], ITmfStateValue> eventMap = new HashMap<String[], ITmfStateValue>();
    Map<String, Integer> knownEventNames = new HashMap<String, Integer>();

    /**
     * constructor
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     */
    public CtfMipMapProviderForTest(CtfTmfTrace trace) {
        super(trace, CtfTmfEvent.class, CTF_MIPMAP_ID, false);
        knownEventNames = fillEventNames();
        range = DEFAULT_RANGE;
    }

    /**
     * constructor
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     * @param range
     *            the way to consider the state changes
     */
    public CtfMipMapProviderForTest(CtfTmfTrace trace, int range) {
        super(trace, CtfTmfEvent.class, CTF_MIPMAP_ID, false);
        knownEventNames = fillEventNames();
        this.range = range;

    }

    @Override
    public CtfMipMapProviderForTest getNewInstance() {
        return new CtfMipMapProviderForTest((CtfTmfTrace) this.getTrace());
    }

    @Override
    protected int getMipMapRange() {
        return range;
    }

    @Override
    protected Map<String[], ITmfStateValue> getEventMap() {
        return eventMap;
    }

    @Override
    protected void eventHandle(ITmfEvent ev) {
        fillEventAndValues(ev);
        super.eventHandle(ev);
    }

    /**
     * Determine which attributes we have to deal with.
     *
     * @param ev
     *            The event to process.
     */
    private void fillEventAndValues(ITmfEvent ev) {
        ITmfStateValue value;
        eventMap.clear();
        if (ev instanceof CtfTmfEvent)
        {
            CtfTmfEvent event = (CtfTmfEvent) ev;
            switch (getEventIndex(event.getEventName())) {
            case 1:
            case 2:
                value = TmfStateValue.newValueLong((Long) ev.getContent().getField(TEST_FIELD_NAME).getValue());
                eventMap.put(new String[] { TEST_ATTRIBUTE_NAME }, value);
                break;
            default:
                break;
            }
        }
    }

    private static HashMap<String, Integer> fillEventNames() {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put(TEST_EVENT_NAME, 1);
        map.put(UST_TEST_EVENT_NAME, 2);
        return map;
    }

    private int getEventIndex(String eventName) {
        Integer ret = knownEventNames.get(eventName);
        return (ret != null) ? ret : -1;
    }
}
