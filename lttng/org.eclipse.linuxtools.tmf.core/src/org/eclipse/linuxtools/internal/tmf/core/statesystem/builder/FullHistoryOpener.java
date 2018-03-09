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
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree.HistoryTreeBackend;

/**
 * Instantiate a builder class for an existing history file. It just opens
 * the existing history file.
 *
 * @author Bernd Hufmann
 *
 */
public class FullHistoryOpener extends AbstractHistoryBuilder {

    /**
     * Instantiate a builder class for an existing history file.
     *
     * @param htFile
     *            The target history file to open.
     * @param version
     *            The state system version of the history file.
     * @throws IOException
     *            If there was an error opening the existing file. Perhaps it was
     *             corrupted, perhaps it's an old version?
     */
    public FullHistoryOpener(File htFile, int version) throws IOException {
        if (htFile == null || !htFile.exists()) {
            throw new IllegalArgumentException();
        }

        fHtBackend = new HistoryTreeBackend(htFile, version);
        fStateSystem = new StateSystem(fHtBackend, false);
    }

    @Override
    public void dispose(boolean deleteFiles) {
    }

    @Override
    public void build() {
    }

    @Override
    public void cancel() {
    }
}
