/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *                   (Extracted logic from HistoryBuilder and TmfStateSystemFactory
 *                    authored by Alexandre Montplaisir)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.tmf.core.statesystem.builder;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.StateSystem;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.NullBackend;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;

/**
 * Builder class for a state system with null history.
 *
 * @author Bernd Hufmann
 */
public class NullHistoryBuilder extends AbstractHistoryBuilder {

    /**
     * Instantiate a new builder class for a state system with null history.
     *
     * @param stateProvider
     *            The state provider plug-in to use
     */
    public NullHistoryBuilder(ITmfStateProvider stateProvider) {
        super();

        if (stateProvider == null) {
            throw new IllegalArgumentException();
        }

        fStateProvider = stateProvider;
        fHtBackend = new NullBackend();
        fStateSystem = new StateSystem(fHtBackend);
        fStateProvider.assignTargetStateSystem(fStateSystem);
    }
}
