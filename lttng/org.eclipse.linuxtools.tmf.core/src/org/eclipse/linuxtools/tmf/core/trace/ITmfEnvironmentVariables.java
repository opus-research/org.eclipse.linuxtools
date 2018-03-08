/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import java.util.Map;

/**
 * Interface for trace types to implement when they can provide trace-wide
 * environment variables.
 *
 * This information will be displayed in the trace's Properties View, among
 * other things.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public interface ITmfEnvironmentVariables {

    /**
     * Get the environment variables related to this trace.
     *
     * @return The map of environment variables, <name, value>
     */
    public Map<String, String> getEnvironment();
}
