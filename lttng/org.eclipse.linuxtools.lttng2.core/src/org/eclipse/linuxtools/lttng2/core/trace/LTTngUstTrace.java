/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.lttng2.core.trace;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.internal.lttng2.core.Activator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;

/**
 * Class to contain LTTng-UST traces
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @since 2.0
 */
public class LTTngUstTrace extends CtfTmfTrace {
    /**
     * ID of the state system we will build
     *
     * @since 2.0
     * */
    public static final String STATE_ID = "org.eclipse.linuxtools.lttng2.userpace"; //$NON-NLS-1$

    /**
     * Default constructor
     */
    public LTTngUstTrace() {
        super();
    }

    @Override
    public IStatus validate(final IProject project, final String path) {
        CTFTrace temp;
        IStatus validStatus;
        /*
         * Make sure the trace is openable as a CTF trace. We do this here
         * instead of calling super.validate() to keep the reference to "temp".
         */
        try {
            temp = new CTFTrace(path);
        } catch (CTFReaderException e) {
            validStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString(), e);
            return validStatus;
        } catch (NullPointerException e) {
            validStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString(), e);
            return validStatus;
        }

        /* Make sure the domain is "ust" in the trace's env vars */
        String dom = temp.getEnvironment().get("domain"); //$NON-NLS-1$
        temp.dispose();
        if (dom != null && dom.equals("\"ust\"")) { //$NON-NLS-1$
            return Status.OK_STATUS;
        }
        validStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LTTngUstTrace_USTDomainError + dom);
        return validStatus;
    }
}
