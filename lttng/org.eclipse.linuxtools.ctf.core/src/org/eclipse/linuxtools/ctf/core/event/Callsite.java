/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Matthew Khouzam - Initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event;

/**
 * Callsite information to help with cdt integration
 *
 * @author Matthew Khouzam
 *
 * @since 1.2
 */
public class Callsite implements Comparable<Callsite>{
    /**
     * The callsite constructor
     * @param en The event name
     * @param func the function name
     * @param ip the instruction pointer of the callsite
     * @param fn the file name of the callsite
     * @param line the line number of the callsite
     */
    public Callsite(String en, String func, Long ip, String fn, Long line){
        EventName = en;
        FileName = fn;
        FunctionName = func;
        this.ip = ip;
        this.LineNumber = line;
    }

    /**
     * The event name
     */
    final public String EventName;
    /**
     * the file name of the callsite
     */
    final public String FileName;
    /**
     *
     */
    final public Long ip;
    /**
     * the function name
     */
    final public String FunctionName;
    /**
     * the line number of the callsite
     */
    final public Long LineNumber;
    /**
     * it needs to compare addresses
     */
    @Override
    public int compareTo(Callsite o) {
        final long left = ip;
        final long right = o.ip;
        if (left >= 0 && right < 0) {
            return -1;
        } else if (left < 0 && right >= 0) {
            return 1;
        } else {
            return (int) (right - left);
        }
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return FileName + "/" + FunctionName + ":" + LineNumber; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
