/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceChannelComponent;

/**
 * <p>
 * Command handler implementation to disable one or more events session, domain and channel.
 * </p>
 * 
 * @author Bernd Hufmann
 */
public class DisableEventHandler extends ChangeEventStateHandler {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.ChangeEventStateHandler#getNewState()
     */
    @Override
    protected TraceEnablement getNewState() {
        return TraceEnablement.DISABLED;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers.ChangeEventStateHandler#changeState(org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceChannelComponent, java.util.List, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void changeState(TraceChannelComponent channel, List<String> eventNames, IProgressMonitor monitor) throws ExecutionException{ 
        channel.disableEvent(eventNames, monitor);
    }
}
