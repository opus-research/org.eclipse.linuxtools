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

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;

class FileAndName implements Comparable<FileAndName> {


    final private File file;
    private IConfigurationElement fConfiguration;
    public String name;
    private boolean fConflict;


    /**
     * @param f
     * @param n
     *
     */
    public FileAndName(File f, String n) {
        file = f;
        name = n;
        fConfiguration = null;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public ITmfTrace getTraceType(){
        if( fConfiguration != null ){
            try {
                return (ITmfTrace) fConfiguration.createExecutableExtension(TmfTraceType.TRACE_TYPE_ATTR);
            } catch (CoreException e) {
                // do nothing
            }
        }
        return null;
    }

    public void setConfigurationElement( IConfigurationElement elem){
        fConfiguration = elem;
    }

    public IConfigurationElement getConfigurationElement(){
        return fConfiguration;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    @Override
    public int compareTo(FileAndName o) {
        final int fileCompare = getFile().compareTo(o.getFile());
        if (fileCompare != 0) {
            return fileCompare;
        }
        return getName().compareTo(o.getFile().getName());
    }

    public void setConflictingName(boolean conflict ){
        fConflict = conflict;
    }
    public boolean isConflictingName() {
        return fConflict;
    }
}