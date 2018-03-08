/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alexandre Montplaisir - Initial API and implementation
 * Contributors: Matthew Khouzam - Addition to have more descriptive errors
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.trace;

import java.lang.reflect.Field;

import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.eclipse.linuxtools.ctf.parser.CTFLexer;

/**
 * General exception that is thrown when there is a problem somewhere with the
 * CTF trace reader.
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
public class CTFReaderException extends Exception {

    private static final long serialVersionUID = 2065258365219777672L;
    private int fErrorLine = -1;
    private Token fToken = null;
    private String fFile = ""; //$NON-NLS-1$
    private String fExpectingName;
    private String fActualName;
    private int fExpectedValue;
    private int fActualValue;


    /**
     * Default constructor with no message.
     */
    public CTFReaderException() {
        super();
    }

    /**
     * Constructor with an attached message.
     *
     * @param message
     *            The message attached to this exception
     */
    public CTFReaderException(String message) {
        super(message);
    }

    /**
     * Re-throw an exception into this type.
     *
     * @param e
     *            The previous Exception we caught
     */
    public CTFReaderException(Exception e) {
        super(e);
    }


}
