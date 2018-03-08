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

/**
 * File and name internal helper class <br>
 * it has the file, a name to display, whether the name is conflicting and a
 * reference to the configuration element defining its trace type.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
class FileAndName implements Comparable<FileAndName> {

    final private File file;
    private IConfigurationElement fConfiguration;
    public String name;
    private boolean fConflict;

    // ///////////////
    // Constructor
    // ///////////////

    /**
     * A file and name
     *
     * @param f
     *            the file, can only be set here
     * @param n
     *            the name, can be renamed
     *
     */
    public FileAndName(File f, String n) {
        file = f;
        name = n;
        fConfiguration = null;
    }

    // /////////////////
    // Getter / Setter
    // /////////////////

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

    /**
     * Gets the trace type.
     *
     * @return an ITmfTrace of the type of the trace or null if the trace type
     *         is unknown
     */
    public ITmfTrace getTraceType() {
        if (fConfiguration != null) {
            try {
                return (ITmfTrace) fConfiguration.createExecutableExtension(TmfTraceType.TRACE_TYPE_ATTR);
            } catch (CoreException e) {
                // do nothing
            }
        }
        return null;
    }

    /**
     * Sets the configuration element of the
     *
     * @param elem
     *            the element
     */
    public void setConfigurationElement(IConfigurationElement elem) {
        fConfiguration = elem;
    }

    /**
     * @return gets the configuration element
     */
    public IConfigurationElement getConfigurationElement() {
        return fConfiguration;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Set that the name is conflicting or not
     *
     * @param conflict
     *            if the name is conflicting or not
     */
    public void setConflictingName(boolean conflict) {
        fConflict = conflict;
    }

    /**
     * @return is the name conflicting?
     */
    public boolean isConflictingName() {
        return fConflict;
    }

    // ///////////////
    // helper
    // ///////////////

    @Override
    public int compareTo(FileAndName o) {
        return getFile().compareTo(o.getFile());
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fConfiguration == null) ? 0 : fConfiguration.hashCode());
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FileAndName)) {
            return false;
        }
        FileAndName other = (FileAndName) obj;
        if (fConfiguration == null) {
            if (other.fConfiguration != null) {
                return false;
            }
        } else if (!fConfiguration.equals(other.fConfiguration)) {
            return false;
        }
        if (file == null) {
            if (other.file != null) {
                return false;
            }
        } else if (!file.equals(other.file)) {
            return false;
        }
        return true;
    }
}