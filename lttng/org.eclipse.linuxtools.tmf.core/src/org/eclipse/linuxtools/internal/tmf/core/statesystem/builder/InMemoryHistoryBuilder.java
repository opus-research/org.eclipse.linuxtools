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
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.InMemoryBackend;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;

/**
 * Builder class for a state system with in memory history.
 *
 * @author Bernd Hufmann
 */
public class InMemoryHistoryBuilder extends AbstractHistoryBuilder {

    /**
     * Instantiate a new state system builder class with in memory history.
     *
     * @param stateProvider
     *            The state provider plugin to use
     */
    public InMemoryHistoryBuilder(ITmfStateProvider stateProvider) {
        super();

        if (stateProvider == null) {
            throw new IllegalArgumentException();
        }

        fStateProvider = stateProvider;
        fHtBackend = new InMemoryBackend(stateProvider.getStartTime());
        fStateSystem = new StateSystem(fHtBackend);
        fStateProvider.assignTargetStateSystem(fStateSystem);
    }
}
