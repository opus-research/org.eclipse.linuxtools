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

import java.io.File;
import java.io.IOException;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.StateSystem;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree.ThreadedHistoryTreeBackend;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;

/**
 * Instantiate a builder class for a state system with full history.
 *
 * @author Bernd Hufmann
 */
public class FullHistoryBuilder extends AbstractHistoryBuilder {

    /**
     * Instantiate a new full history state system builder class.
     *
     * @param stateProvider
     *            The state provider plug-in to use
     * @param htFile
     *            The target history file we want to use.
     * @throws IOException
     *            In case of problem with writing to disk
     */
    public FullHistoryBuilder(ITmfStateProvider stateProvider, File htFile) throws IOException {
        if (stateProvider == null) {
            throw new IllegalArgumentException();
        }

        fHtBackend = new ThreadedHistoryTreeBackend(htFile,
                stateProvider.getStartTime(), stateProvider.getVersion(), QUEUE_SIZE);
        fStateSystem = new StateSystem(fHtBackend);
        stateProvider.assignTargetStateSystem(fStateSystem);
        fStateProvider = stateProvider;
    }
}
