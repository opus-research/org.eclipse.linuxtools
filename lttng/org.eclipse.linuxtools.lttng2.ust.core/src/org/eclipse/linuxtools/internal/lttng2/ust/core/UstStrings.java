/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ust.core;

/**
 * UST Strings
 *
 * @author Matthew Khouzam
 */
@SuppressWarnings({ "nls", "javadoc" })
public interface UstStrings {
    /* UST_libc stuff */
    public static final String MALLOC = "ust_libc:malloc";
    public static final String CALLOC = "ust_libc:calloc";
    public static final String REALLOC = "ust_libc:realloc";
    public static final String FREE = "ust_libc:free";
    public static final String MEMALIGN = "ust_libc:memalign";
    public static final String POSIX_MEMALIGN = "ust_libc:posix_memalign";
}
