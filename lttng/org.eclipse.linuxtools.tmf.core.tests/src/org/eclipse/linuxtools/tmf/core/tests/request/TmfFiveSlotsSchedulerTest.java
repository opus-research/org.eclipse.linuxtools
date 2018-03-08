/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Simon Delisle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.junit.Test;

/**
 * Test cases specific to the "five slots" scheduler
 */
public class TmfFiveSlotsSchedulerTest extends AbstractTmfRequestSchedulerTest {

    // ------------------------------------------------------------------------
    // Tests cases
    // ------------------------------------------------------------------------

    /**
     * Test if the scheduler is working as expected
     */
    @Test
    public void executionOrder() {
        List<String> expectedOrder = new LinkedList<String>();
        expectedOrder.add("FOREGROUND1");
        expectedOrder.add("FOREGROUND2");
        expectedOrder.add("FOREGROUND3");
        expectedOrder.add("FOREGROUND4");
        expectedOrder.add("BACKGROUND1");
        expectedOrder.add("FOREGROUND1");
        expectedOrder.add("FOREGROUND2");
        expectedOrder.add("FOREGROUND3");
        expectedOrder.add("FOREGROUND4");
        expectedOrder.add("BACKGROUND2");

        fOrderList.clear();
        fForegroundId = 0;
        fBackgroundId = 0;

        BackgroundRequest background1 = new BackgroundRequest(TmfTimeRange.ETERNITY);
        BackgroundRequest background2 = new BackgroundRequest(TmfTimeRange.ETERNITY);

        ForegroundRequest foreground1 = new ForegroundRequest(TmfTimeRange.ETERNITY);
        ForegroundRequest foreground2 = new ForegroundRequest(TmfTimeRange.ETERNITY);
        ForegroundRequest foreground3 = new ForegroundRequest(TmfTimeRange.ETERNITY);
        ForegroundRequest foreground4 = new ForegroundRequest(TmfTimeRange.ETERNITY);

        fixture.sendRequest(foreground1);
        fixture.sendRequest(foreground2);
        fixture.sendRequest(foreground3);
        fixture.sendRequest(foreground4);
        fixture.sendRequest(background1);
        fixture.sendRequest(background2);
        try {
            foreground1.waitForCompletion();
            foreground2.waitForCompletion();
            foreground3.waitForCompletion();
            foreground4.waitForCompletion();
            background1.waitForCompletion();
            background2.waitForCompletion();
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(expectedOrder, fOrderList.subList(0, expectedOrder.size()));
    }

}
