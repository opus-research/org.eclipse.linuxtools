/*******************************************************************************
 * Copyright (c) 2013 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.osgi.util.NLS;

/**
 * @author Matthew Khouzam
 * @since 2.0
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.core.ctfadaptor.messages"; //$NON-NLS-1$
    /**
     * And error message
     */
    public static String CtfTmfTrace_MajorNotSet;
    /**
     * Read error
     */
    public static String CtfTmfTrace_ReadingError;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
