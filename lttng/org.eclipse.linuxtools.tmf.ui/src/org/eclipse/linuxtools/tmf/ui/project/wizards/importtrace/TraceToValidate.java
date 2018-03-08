/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

/**
 * Trace import helper class
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
class TraceToValidate implements Comparable<TraceToValidate> {

    private final String fTraceToScan;
    private final String fTraceType;

    public TraceToValidate(String traceToScan, String traceType) {
        this.fTraceToScan = traceToScan;
        this.fTraceType = traceType;
    }

    public String getTraceToScan() {
        return fTraceToScan;
    }

    public String getTraceType() {
        return fTraceType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fTraceToScan == null) ? 0 : fTraceToScan.hashCode());
        result = prime * result + ((fTraceType == null) ? 0 : fTraceType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TraceToValidate)) {
            return false;
        }
        TraceToValidate other = (TraceToValidate) obj;
        if (fTraceToScan == null) {
            if (other.fTraceToScan != null) {
                return false;
            }
        } else if (!fTraceToScan.equals(other.fTraceToScan)) {
            return false;
        }
        if (fTraceType == null) {
            if (other.fTraceType != null) {
                return false;
            }
        } else if (!fTraceType.equals(other.fTraceType)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(TraceToValidate o) {
        int retVal = fTraceToScan.compareTo(o.getTraceToScan());
        if( retVal == 0){
            retVal = fTraceType.compareTo(o.fTraceType);
        }
        return retVal;
    }
}