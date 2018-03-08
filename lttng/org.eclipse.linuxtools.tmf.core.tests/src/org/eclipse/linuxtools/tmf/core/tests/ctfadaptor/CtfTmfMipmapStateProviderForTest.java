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

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.AbstractTmfMipmapStateProvider;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * this is the mip map provider for CTF
 *
 * @author Jean-Christian Kouamé
 * @since 3.0
 */
public class CtfTmfMipmapStateProviderForTest extends AbstractTmfMipmapStateProvider {
    /** test attribute name */
    public final static String TEST_ATTRIBUTE_NAME = "test_attribute"; //$NON-NLS-1$

    private final int DEFAULT_RESOLUTION = 16;
    private int resolution;
    private Type type;
    private final static String TEST_EVENT_NAME = "event"; //$NON-NLS-1$
    private final static String UST_TEST_EVENT_NAME = "ust_tests_hello:tptest"; //$NON-NLS-1$
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
    public CtfTmfMipmapStateProviderForTest(CtfTmfTrace trace) {
        super(trace, CtfTmfEvent.class, CTF_MIPMAP_ID);
        this.knownEventNames = fillEventNames();
        this.resolution = DEFAULT_RESOLUTION;
        this.type = Type.LONG;
    }

    /**
     * constructor
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     * @param resolution
     *            the mipmap resolution array (max, min, avg)
     * @param type
     *            the type of value to use
     */
    public CtfTmfMipmapStateProviderForTest(CtfTmfTrace trace, int resolution, Type type) {
        super(trace, CtfTmfEvent.class, CTF_MIPMAP_ID);
        this.knownEventNames = fillEventNames();
        this.resolution = resolution;
        this.type = type;
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
                int quark = ss.getQuarkAbsoluteAndAdd(TEST_ATTRIBUTE_NAME);
                Long longVal = (Long) ev.getContent().getField(TEST_FIELD_NAME).getValue();
                if (longVal == -1) {
                    value = TmfStateValue.nullValue();
                } else if (type == Type.LONG) {
                    value = TmfStateValue.newValueLong(longVal);
                } else if (type == Type.INTEGER) {
                    value = TmfStateValue.newValueInt(longVal.intValue());
                } else if (type == Type.DOUBLE) {
                    value = TmfStateValue.newValueDouble(longVal.doubleValue());
                } else {
                    value = TmfStateValue.nullValue();
                }

                modifyMipmapAttribute(ts, value, quark, MIN | MAX | AVG, resolution);

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
    public int getVersion() {
        return 0;
    }

    @Override
    public CtfTmfMipmapStateProviderForTest getNewInstance() {
        return new CtfTmfMipmapStateProviderForTest((CtfTmfTrace) this.getTrace());
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
