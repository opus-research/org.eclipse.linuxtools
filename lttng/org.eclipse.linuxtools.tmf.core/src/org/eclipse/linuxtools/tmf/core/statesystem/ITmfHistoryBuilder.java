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
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.statesystem;

/**
 * Interface that a history builder class has to implement.
 *
 * @author Bernd Hufmann
 */
public interface ITmfHistoryBuilder {
    /**
     * Gets the state system instance created by this builder.
     * @return the state system instance.
     */
    ITmfStateSystem getStateSystem();
    /**
    * Disposes state provider and the resources created
    * @param deleteFiles
    *          true to delete the history file else false
    */
    void dispose(boolean deleteFiles);
    /**
     * Builds the state system history.
     */
    void build();
    /**
     * Cancels the building of the state system history.
     */
    void cancel();
}
