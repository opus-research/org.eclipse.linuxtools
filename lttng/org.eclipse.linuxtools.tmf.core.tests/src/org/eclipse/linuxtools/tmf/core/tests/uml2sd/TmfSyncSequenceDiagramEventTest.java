/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.tests.uml2sd;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.uml2sd.TmfSyncSequenceDiagramEvent;

/**
 * <b><u>TmfSyncSequenceDiagramEventTest</u></b>
 * <p>
 * Implement me. Please.
 * <p>
 */
@SuppressWarnings({"nls","javadoc"})
public class TmfSyncSequenceDiagramEventTest extends TestCase {

    private final String fContext = ITmfEventType.DEFAULT_CONTEXT_ID;
    private final String fTypeId  = "Some type";
    private final String fLabel0  = "label1";
    private final String fLabel1  = "label2";
    private final String[] fLabels  = new String[] { fLabel0, fLabel1 };

    private final TmfTimestamp fTimestamp1 = new TmfTimestamp(12345, (byte) 2, 5);
    private final String       fSource     = "Source";
    private final TmfEventType fType       = new TmfEventType(fContext, fTypeId, TmfEventField.makeRoot(fLabels));
    private final String       fReference  = "Some reference";

    private final TmfEvent fEvent1;
    private final TmfEventField fContent1;

    public TmfSyncSequenceDiagramEventTest () {
        fContent1 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some content");
        fEvent1 = new TmfEvent(null, fTimestamp1, fSource, fType, fContent1, fReference);
    }

    @Override
    public void setUp() throws Exception {
    }

    @Override
    public void tearDown() throws Exception {
    }

    public void testTmfSyncSequenceDiagramEvent() {
        TmfSyncSequenceDiagramEvent event = null;
        try {
            event = new TmfSyncSequenceDiagramEvent(null, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("testTmfSyncSequenceDiagramEvent", e.getMessage().contains("startEvent=null"));
        }

        try {
            event = new TmfSyncSequenceDiagramEvent(fEvent1, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("testTmfSyncSequenceDiagramEvent", e.getMessage().contains("sender=null"));
        }

        try {
            event = new TmfSyncSequenceDiagramEvent(fEvent1, "sender", null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("testTmfSyncSequenceDiagramEvent", e.getMessage().contains("receiver=null"));
        }

        try {
            event = new TmfSyncSequenceDiagramEvent(fEvent1, "sender", "receiver", null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
            assertTrue("testTmfSyncSequenceDiagramEvent", e.getMessage().contains("name=null"));
        }

        try {
            event = new TmfSyncSequenceDiagramEvent(fEvent1, "sender", "receiver", "signal");
            // success
            assertEquals("testTmfSyncSequenceDiagramEvent", 0, event.getStartTime().compareTo(fTimestamp1, true));
            assertEquals("testTmfSyncSequenceDiagramEvent", "sender", event.getSender());
            assertEquals("testTmfSyncSequenceDiagramEvent", "receiver", event.getReceiver());
            assertEquals("testTmfSyncSequenceDiagramEvent", "signal", event.getName());

        } catch (IllegalArgumentException e) {
            fail();
        }
    }
}
