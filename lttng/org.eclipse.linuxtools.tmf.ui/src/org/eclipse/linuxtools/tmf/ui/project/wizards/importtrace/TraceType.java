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

import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * @author Matthew Khouzam
 * @since 2.0
 */
class TraceType {


    final private String fName;
    final private String fCategoryName;
    final private String fCanonicalName;
    final private ITmfTrace fTrace;

    /**
     * @param canonicalName
     *            The "path" of the tracetype
     * @param categoryName
     *            the category of the trace type
     * @param name
     *            the name of the trace
     * @param trace
     *            an object of the trace type
     *
     */
    public TraceType(String canonicalName, String categoryName, String name, ITmfTrace trace) {
        fName = name;
        fCategoryName = categoryName;
        fCanonicalName = canonicalName;
        fTrace = trace;
    }

    /**
     * @return the Name
     */
    public String getName() {
        return fName;
    }

    /**
     * @return the Category Name
     */
    public String getCategoryName() {
        return fCategoryName;
    }

    /**
     * @return the Canonical Name
     */
    public String getCanonicalName() {
        return fCanonicalName;
    }

    /**
     * @param path
     *            the trace to validate
     * @return whether it passes the validation
     */
    public boolean validate(String path) {
        boolean valid = false;
        if (fTrace != null) {
            valid = standardValidate(path);
        }
        if (fCategoryName.equals(ImportUtils.CUSTOM_TXT_CATEGORY) || fCategoryName.equals(ImportUtils.CUSTOM_XML_CATEGORY)) {
            valid = customValidate(path);
        }
        return valid;
    }

    private boolean standardValidate(String path) {
        final boolean valid = fTrace.validate(null, path).equals(Status.OK_STATUS);
        return valid;
    }

    private static boolean customValidate(String path) {
        File f = new File(path);
        return f.exists() && f.isFile();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return fName;
    }

}