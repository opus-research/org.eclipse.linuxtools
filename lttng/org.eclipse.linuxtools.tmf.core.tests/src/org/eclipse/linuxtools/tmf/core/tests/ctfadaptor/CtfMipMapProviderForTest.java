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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.MipMapProvider;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * this is the mip map provider for CTF
 *
 * @author Jean-Christian Kouamé
 * @since 3.0
 */
public class CtfMipMapProviderForTest extends MipMapProvider {
    private final int DEFAULT_RESOLUTION = 16;
    private int resolution[];
    private final static String TEST_EVENT_NAME = "event"; //$NON-NLS-1$
    private final static String UST_TEST_EVENT_NAME = "ust_tests_hello:tptest"; //$NON-NLS-1$
    private final static String TEST_ATTRIBUTE_NAME = "test_attribute"; //$NON-NLS-1$
    private final static String TEST_FIELD_NAME = "value"; //$NON-NLS-1$
    private final static String CTF_MIPMAP_ID = "CTF_MIPMAP_ID"; //$NON-NLS-1$
    Map<String, Integer> knownEventNames = new HashMap<String, Integer>();

    private final String ERROR_ATTRIBUTE_NOT_FOUND = "Error : Impossible to find the attribute"; //$NON-NLS-1$
    private final String ERROR_INVALID_STATE_VALUE = "Error : Invalid state value"; //$NON-NLS-1$
    private final String ERROR_INVALID_TIMESTAMP = "Error : Invalid timestamp"; //$NON-NLS-1$

    /**
     * constructor
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     */
    public CtfMipMapProviderForTest(CtfTmfTrace trace) {
        super(trace, CtfTmfEvent.class, CTF_MIPMAP_ID, false);
        this.knownEventNames = fillEventNames();
        this.resolution = new int[] {DEFAULT_RESOLUTION, DEFAULT_RESOLUTION, DEFAULT_RESOLUTION};
    }

    /**
     * constructor
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     * @param resolution
     *            the way to consider the state changes
     */
    public CtfMipMapProviderForTest(CtfTmfTrace trace, int resolution[]) {
        super(trace, CtfTmfEvent.class, CTF_MIPMAP_ID, false);
        this.knownEventNames = fillEventNames();
        this.resolution = Arrays.copyOf(resolution, resolution.length);
    }

    @Override
    protected void eventHandle(ITmfEvent ev) {
        final long ts = ev.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        try {
        ITmfStateValue value;
        if (ev instanceof CtfTmfEvent)
        {
            CtfTmfEvent event = (CtfTmfEvent) ev;
            switch (getEventIndex(event.getEventName())) {
            case 1:
            case 2:
                final Integer currentParentNode = ss.getQuarkAbsoluteAndAdd(MIPMAP);
                int quark = ss.getQuarkRelativeAndAdd(currentParentNode, TEST_ATTRIBUTE_NAME);
                value = TmfStateValue.newValueLong((Long) ev.getContent().getField(TEST_FIELD_NAME).getValue());
                computeMipmapAttribute(ts, value, quark, MipMapProvider.MAX_STRING, resolution[0]);
                computeMipmapAttribute(ts, value, quark, MipMapProvider.MIN_STRING, resolution[1]);
                computeMipmapAttribute(ts, value, quark, MipMapProvider.AVG_STRING, resolution[2]);
                ss.modifyAttribute(ts, value, quark);
                break;
            default:
                break;
            }
        }
        } catch (TimeRangeException e) {
            Activator.logError(ERROR_INVALID_TIMESTAMP, e);
        } catch (AttributeNotFoundException e) {
            Activator.logError(ERROR_ATTRIBUTE_NOT_FOUND, e);
        } catch (StateValueTypeException e) {
            Activator.logError(ERROR_INVALID_STATE_VALUE, e);
        }
    }

    @Override
    public void dispose() {
        closeMipMap();
        super.dispose();
    }
    @Override
    public CtfMipMapProviderForTest getNewInstance() {
        return new CtfMipMapProviderForTest((CtfTmfTrace) this.getTrace());
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
