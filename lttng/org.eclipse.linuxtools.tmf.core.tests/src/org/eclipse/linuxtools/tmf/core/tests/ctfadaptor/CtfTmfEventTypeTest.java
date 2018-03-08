/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEventType;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.junit.Test;

/**
 * The class <code>CtfTmfEventTypeTest</code> contains tests for the class
 * <code>{@link CtfTmfEventType}</code>.
 *
 * @author ematkho
 * @version 1.0
 */
@SuppressWarnings("nls")
public class CtfTmfEventTypeTest {

    /**
     * Run the CtfTmfEventType(String,String,ITmfEventField) constructor test.
     */
    @Test
    public void testCtfTmfEventType() {
        String eventName = "";
        ITmfEventField content = new TmfEventField("", new ITmfEventField[] {});
        CtfTmfEventType result = new CtfTmfEventType( eventName, content);

        assertNotNull(result);
        assertEquals("", result.toString());
        assertEquals("", result.getName());
        assertEquals("Ctf Event", result.getContext());
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        ITmfEventField emptyField = new TmfEventField("", new ITmfEventField[] {});
        CtfTmfEventType fixture = new CtfTmfEventType("", emptyField);

        String result = fixture.toString();

        assertEquals("", result);
    }
}