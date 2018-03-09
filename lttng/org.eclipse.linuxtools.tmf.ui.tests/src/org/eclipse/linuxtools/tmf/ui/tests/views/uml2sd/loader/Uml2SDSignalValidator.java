/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Patrick Tasse - Support selection range
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.loader;

import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;

/**
 *  Class to implement that certain signals are sent as well as are sent with correct content.
 *
 *  @author Bernd Hufmann
 */
public class Uml2SDSignalValidator extends TmfComponent implements IUml2SdSignalValidator {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private boolean fIsSignalReceived = false;
    private boolean fIsSignalError = false;
    private boolean fIsSourceError = false;
    private boolean fIsCurrentTimeError = false;
    private boolean fIsRangeError = false;

    private Object fSource = null;
    private TmfTimestamp fCurrentTimestamp = null;
    private TmfTimeRange fCurrentTimeRange = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor
     */
    public Uml2SDSignalValidator() {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Signal handler for time synch signal.
     * @param signal the signal to handle.
     */
    @TmfSignalHandler
    public void synchToTime(TmfTimeSynchSignal signal) {
        // Set results so that it can be validated in the test case
        setSignalReceived(true);
        setSourceError(getSource() != signal.getSource());
        setCurrentTimeError(!getCurrentTime().equals(signal.getBeginTime()));
    }

    /**
     * Signal handler for time range synch signal.
     * @param signal the signal to handle.
     */
    @TmfSignalHandler
    public void synchToTimeRange(TmfRangeSynchSignal signal) {
        // Set results so that it can be validated in the test case
        setSignalReceived(true);
        if (getSource() != null) {
            setSourceError(getSource() != signal.getSource());
        }
        if (getCurrentRange() != null) {
            setRangeError(!getCurrentRange().equals(signal.getCurrentRange()));
        }
    }

    @Override
    public boolean isSignalReceived() {
        return fIsSignalReceived;
    }

    @Override
    public void setSignalReceived(boolean received) {
        fIsSignalReceived = received;
    }

    @Override
    public boolean isSourceError() {
        return fIsSourceError;
    }

    @Override
    public void setSourceError(boolean fIsSourceError) {
        this.fIsSourceError = fIsSourceError;
    }

    @Override
    public boolean isCurrentTimeError() {
        return fIsCurrentTimeError;
    }

    @Override
    public void setCurrentTimeError(boolean fIsCurrentTimeError) {
        this.fIsCurrentTimeError = fIsCurrentTimeError;
    }

    @Override
    public boolean isRangeError() {
        return fIsRangeError;
    }

    @Override
    public void setRangeError(boolean fIsRangeError) {
        this.fIsRangeError = fIsRangeError;
    }

    @Override
    public boolean isSignalError() {
        return fIsSignalError;
    }

    @Override
    public void setSignalError(boolean fIsSignalError) {
        this.fIsSignalError = fIsSignalError;
    }

    @Override
    public Object getSource() {
        return fSource;
    }

    @Override
    public void setSource(Object source) {
        fSource = source;
    }

    @Override
    public TmfTimestamp getCurrentTime() {
        return fCurrentTimestamp;
    }

    @Override
    public void setCurrentTime(TmfTimestamp currentTime) {
        fCurrentTimestamp = currentTime == null ? null : new TmfTimestamp(currentTime);
    }

    @Override
    public TmfTimeRange getCurrentRange() {
        return fCurrentTimeRange;
    }

    @Override
    public void setCurrentRange(TmfTimeRange currentRange) {
        fCurrentTimeRange = currentRange == null ? null : new TmfTimeRange(currentRange);
    }
}

