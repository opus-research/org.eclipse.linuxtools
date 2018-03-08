/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.event;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimePreferences;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Test suite for the TmfTimestampFormat class.
 */
@SuppressWarnings("javadoc")
public class TmfTimestampFormatTest {

    private static final String TEST_PATTERN = "HH:mm:ss.SSS";
    private static final TimeZone TEST_TIME_ZONE = TimeZone.getTimeZone(TimeZone.getAvailableIDs(0)[0]);

    private final TmfTimestampFormat tsf1 = new TmfTimestampFormat(TEST_PATTERN);
    private final TmfTimestampFormat tsf2 = new TmfTimestampFormat(TEST_PATTERN, TEST_TIME_ZONE);

    @Test
    public void testDefaultConstructor() {
        // The default should be loaded
        TmfTimestampFormat ts0 = new TmfTimestampFormat();
        assertEquals("HH:mm:ss.SSS CCC NNN", ts0.toPattern());
    }

    @Test
    public void testValueConstructor() {
        assertEquals(TEST_PATTERN, tsf1.toPattern());
    }

    @Test
    public void testValueTimeZoneConstructor() {
        assertEquals(TEST_PATTERN, tsf2.toPattern());
        assertEquals(TEST_TIME_ZONE, tsf2.getTimeZone());
    }

    @Test
    public void testUpdateDefaultFormats() {
        IEclipsePreferences node = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);

        String dateTimeTestValue = TmfTimePreferences.TIME_HOUR_FMT + ":";
        node.put(TmfTimePreferences.DATIME, dateTimeTestValue);

        String subSecTestValue = TmfTimePreferences.SUBSEC_NANO_FMT + ":";
        node.put(TmfTimePreferences.SUBSEC, subSecTestValue);
        try {
            node.flush();
        } catch (BackingStoreException e) {
        }
        TmfTimestampFormat.updateDefaultFormats();
        String expected = dateTimeTestValue + "." + subSecTestValue;
        String expected2 = "TTT." + subSecTestValue;
        assertEquals(expected, TmfTimestampFormat.getDefaulTimeFormat().toPattern());
        assertEquals(expected2, TmfTimestampFormat.getDefaulIntervalFormat().toPattern());
        // Revert prefs
        node.put(TmfTimePreferences.DATIME, TmfTimePreferences.TIME_HOUR_FMT);
        node.put(TmfTimePreferences.SUBSEC, TmfTimePreferences.SUBSEC_NANO_FMT);
        try {
            node.flush();
        } catch (BackingStoreException e) {
        }
        TmfTimestampFormat.updateDefaultFormats();
    }

    @Test
    public void testGetDefaulTimeFormat() {
        assertEquals(TmfTimestampFormat.getDefaulTimeFormat().toPattern(), TmfTimePreferences.getInstance().getTimePattern());
    }

    @Test
    public void testGetDefaulIntervalFormat() {
        assertEquals(TmfTimestampFormat.getDefaulIntervalFormat().toPattern(), TmfTimePreferences.getInstance().getIntervalPattern());
    }
}
