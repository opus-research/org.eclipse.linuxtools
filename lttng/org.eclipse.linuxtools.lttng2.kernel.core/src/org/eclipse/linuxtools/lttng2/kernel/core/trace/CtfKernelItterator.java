/*******************************************************************************
 * Copyright (c) 2011-2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng2.kernel.core.trace;

import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;

/**
 * @author ematkho
 * @since 2.0
 */
public class CtfKernelItterator extends CtfIterator {

    /**
     * Constructor
     * @param trace the parent trace
     */
    public CtfKernelItterator(CtfTmfTrace trace) {
        super(trace);
    }

    @Override
    public CtfTmfEvent getCurrentEvent() {
        final StreamInputReader top = super.prio.peek();
        if (top != null) {
            return new CtfKernelEvent(top.getCurrentEvent(), top.getFilename(),
                    (CtfKernelTrace) this.getCtfTmfTrace());
        }
        return null;
    }

}
