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

import java.util.Map;

import org.eclipse.core.resources.IProject;
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
    public boolean validate(final IProject project, final String path) {
        /* Make sure the domain is "kernel" in the trace's env vars */
        final Map<String, String> environmentSetup = getEnvironmentSetup(path);
        if (environmentSetup == null) {
            return false;
        }
        String dom = environmentSetup.get("domain"); //$NON-NLS-1$
        if (dom != null && dom.equals("\"ust\"")) { //$NON-NLS-1$
            return true;
        }
        return false;
    }
}
