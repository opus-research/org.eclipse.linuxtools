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
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.IStateHistoryBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree.ThreadedHistoryTreeBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.partial.PartialHistoryBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.partial.PartialStateSystem;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;

/**
 * Instantiate a builder class for a state system with partial history.
 *
 * @author Bernd Hufmann
 */
public class PartialStateSystemBuilder extends AbstractHistoryBuilder {

    /**
     * Instantiate a new partial history state system builder class.
     *
     * @param realStateProvider
     *            The state provider plug- in to use
     * @param htPartialFile
     *            The partial state history file
     * @throws TmfTraceException
     *            If there is a problem writing to disk
     */
    public PartialStateSystemBuilder(ITmfStateProvider realStateProvider, File htPartialFile) throws TmfTraceException {
        super();

        if (realStateProvider == null) {
            throw new IllegalArgumentException();
        }

        /*
         * The order of initializations is very tricky (but very important!)
         * here. We need to follow this pattern:
         * (1 is done before the call to this method)
         *
         * 1- Instantiate realStateProvider
         * 2- Instantiate realBackend
         * 3- Instantiate partialBackend, whith prereqs:
         *  3a- Instantiate partialProvider, via realProvider.getNew()
         *  3b- Instantiate nullBackend (partialSS's backend)
         *  3c- Instantiate partialSS
         *  3d- partialProvider.assignSS(partialSS)
         * 4- Instantiate realSS
         * 5- partialSS.assignUpstream(realSS)
         * 6- realProvider.assignSS(realSS)
         * 7- Call HistoryBuilder(realProvider, realSS, partialBackend) to build the thing.
         */

        final long granularity = 50000;

        /* 2 */
        IStateHistoryBackend realBackend = null;
        try {
            realBackend = new ThreadedHistoryTreeBackend(htPartialFile,
                    realStateProvider.getStartTime(), realStateProvider.getVersion(), QUEUE_SIZE);
        } catch (IOException e) {
            throw new TmfTraceException(e.toString(), e);
        }

        /* 3a */
        ITmfStateProvider partialProvider = realStateProvider.getNewInstance();

        /* 3b-3c, constructor automatically uses a NullBackend */
        PartialStateSystem pss = new PartialStateSystem();

        /* 3d */
        partialProvider.assignTargetStateSystem(pss);

        /* 3 */
        IStateHistoryBackend partialBackend =
                new PartialHistoryBackend(partialProvider, pss, realBackend, granularity);

        /* 4 */
        StateSystem realSS = new StateSystem(partialBackend);

        /* 5 */
        pss.assignUpstream(realSS);

        /* 6 */
        realStateProvider.assignTargetStateSystem(realSS);

        /* 7 */
        fHtBackend = partialBackend;
        fStateSystem = realSS;
        fStateProvider = realStateProvider;
    }
}
