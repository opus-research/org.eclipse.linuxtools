/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc Dumais - Initial implementation
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Updated for TMF 2.0
 *******************************************************************************/

package org.eclipse.linuxtools.gdbtrace.trace;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.linuxtools.gdbtrace.GdbTraceCorePlugin;
import org.eclipse.linuxtools.gdbtrace.event.GdbTraceEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLongLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

/**
 * <b><u>GdbTrace</u></b>
 * <p>
 * GDB Tracepoint extension of TmfTrace.  This class implements the necessary
 * methods and functionalities so that a GDB tracepoint file can be used by
 * the TMF framework as a "tracer".
 * <p>
 * @author Marc Dumais
 * @author Francois Chouinard
 */
public class GdbTrace extends TmfTrace implements ITmfEventParser {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final int CACHE_SIZE = 20;
    private static final String GDB_EXECUTABLE = "gdb"; //$NON-NLS-1$

    /** The qualified name for the 'executable' persistent property */
    public static final QualifiedName EXEC_KEY = new QualifiedName(GdbTraceCorePlugin.PLUGIN_ID, "executable"); //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Interface to access GDB Tracepoints
    private DsfGdbAdaptor fGdbTpRef;
    private long fNbFrames = 0;

    // The trace location
    long fLocation;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public GdbTrace() {
        setCacheSize(CACHE_SIZE);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#validate(org.eclipse.core.resources.IProject, java.lang.String)
     */
    @Override
    public boolean validate(IProject project, String path) {
        return fileExists(path);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfTrace#initTrace(org.eclipse.core.resources.IResource, java.lang.String, java.lang.Class)
     */
    @Override
    public void initTrace(IResource resource, String path, Class<? extends ITmfEvent> type) throws TmfTraceException {
        try {
            String tracedExecutable = resource.getPersistentProperty(EXEC_KEY);
            if (tracedExecutable == null) {
                throw new TmfTraceException("Trace executable not set"); //$NON-NLS-1$
            }
            fGdbTpRef = new DsfGdbAdaptor(this, GDB_EXECUTABLE, path, tracedExecutable);
            fNbFrames = fGdbTpRef.getNumberOfFrames();
        } catch (CoreException e) {
            throw new TmfTraceException("Failed to initialize trace", e); //$NON-NLS-1$
        }

        super.initTrace(resource, path, type);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.component.TmfDataProvider#dispose()
     */
    @Override
    public synchronized void dispose() {
        if (fGdbTpRef != null) {
            fGdbTpRef.dispose();
        }
        super.dispose();
    }

    /**
     * @return GDB-DSF session id
     */
    public String getDsfSessionId () {
        return fGdbTpRef.getSessionId();
    }

    /**
     * @return the number of frames in current tp session
     */
    public long getNbFrames () {
        fNbFrames =  fGdbTpRef.getNumberOfFrames();
        return fNbFrames;
    }

    // ------------------------------------------------------------------------
    // TmfTrace
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#seekEvent(org.eclipse.linuxtools.tmf.core.trace.ITmfLocation)
     */
    @Override
    public synchronized TmfContext seekEvent(ITmfLocation location) {
        fLocation = (location != null) ? ((Long) location.getLocationInfo()) : 0;
        return new TmfContext(new TmfLongLocation(fLocation), fLocation);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#seekEvent(double)
     */
    @Override
    public synchronized ITmfContext seekEvent(double ratio) {
        TmfContext context = seekEvent((long) ratio * getNbEvents());
        return context;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.TmfTrace#getLocationRatio(org.eclipse.linuxtools.tmf.trace.ITmfLocation)
     */
    @Override
    public double getLocationRatio(ITmfLocation location) {
        if (getNbEvents() > 0 && location instanceof TmfLongLocation) {
            return (double) ((TmfLongLocation) location).getLocationInfo() / getNbEvents();
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.TmfTrace#getCurrentLocation()
     */
    @Override
    public ITmfLocation getCurrentLocation() {
        return new TmfLongLocation(fLocation);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser#parseEvent(org.eclipse.linuxtools.tmf.core.trace.ITmfContext)
     */
    @Override
    public GdbTraceEvent parseEvent(ITmfContext context) {
        if (context.getRank() >= fNbFrames) {
            return null;
        }
        // work-around to ensure that the select and parse of trace frame will be atomic
        GdbTraceEvent event = fGdbTpRef.selectAndReadFrame(context.getRank());
        fLocation++;
        return event;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.TmfTrace#seekEvent(org.eclipse.linuxtools.tmf.event.TmfTimestamp)
     */
    @Override
    public synchronized TmfContext seekEvent(ITmfTimestamp timestamp) {
        long rank = timestamp.getValue();
        return seekEvent(rank);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.TmfTrace#seekEvent(long)
     */
    @Override
    public synchronized TmfContext seekEvent(long rank) {
        fLocation = rank;
        TmfContext context = new TmfContext(new TmfLongLocation(fLocation), rank);
        return context;
    }

    /**
     * Select a frame and update the visualization
     * @param rank the rank
     */
    public void selectFrame(long rank) {
        fGdbTpRef.selectDataFrame(rank, true);
    }
}