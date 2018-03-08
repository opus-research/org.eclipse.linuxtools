/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * This is the interface used to define the "state change input", which is the
 * main type of input that goes in the state system.
 *
 * Usually a state change input, also called "state provider" is the piece of
 * the pipeline which converts trace events to state changes.
 *
 * @version 2.0
 * @author Alexandre Montplaisir
 */
public interface IStateChangeInput {

    /**
     * Special state provider version number that will tell the backend to
     * ignore the version check and open an existing file even if the versions
     * don't match.
     * @since 2.0
     */
    public final static int IGNORE_PROVIDER_VERSION = -42;

    /**
     * Event handler plugins should provide a version number. This is used to
     * determine if a potential existing file can be re-opened later (if the
     * versions in the file and in the viewer match), or if the file should be
     * rebuilt from scratch (if the versions don't match).
     *
     * @return The version number of the input plugin
     * @since 2.0
     */
    public int getVersion();

    /**
     * Get the trace with which this state input plugin is associated.
     *
     * @return The associated trace
     */
    public ITmfTrace getTrace();

    /**
     * Return the start time of this "state change input", which is normally the
     * start time of the originating trace (or it can be the time of the first
     * state-changing event).
     *
     * @return The start time
     */
    public long getStartTime();

    /**
     * Method for the input plugin to specify which type of events it expects.
     * This will guarantee that all events it receives via processEvent() are
     * indeed of the given type, so it should be safe to cast to that type.
     *
     * @return The expected Class of the event. Only events of this class (and
     *         valid subclasses) will be handled.
     * @since 2.0
     */
    public Class<? extends ITmfEvent> getExpectedEventType();

    /**
     * Assign the target state system where this SCI will insert its state
     * changes. Because of dependencies issues, this can normally not be done at
     * the constructor.
     *
     * This needs to be called before .run()!
     *
     * @param ssb
     *            Target state system for the state changes generated by this
     *            input plugin
     * @since 2.0
     */
    public void assignTargetStateSystem(ITmfStateSystemBuilder ssb);

    /**
     * Return the currently assigned target state system.
     *
     * @return Reference to the currently assigned state system, or null if no
     *         SS is assigned yet
     * @since 2.0
     */
    public ITmfStateSystem getAssignedStateSystem();

    /**
     * Send an event to this input plugin for processing. The implementation
     * should check the contents, and call the state-modifying methods of its
     * IStateSystemBuilder object accordingly.
     *
     * @param event
     *            The event (which should be safe to cast to the
     *            expectedEventType) that has to be processed.
     */
    public void processEvent(ITmfEvent event);

    /**
     * Indicate to the state history building process that we are done (for now),
     * and that it should close its current history.
     */
    public void dispose();
}
