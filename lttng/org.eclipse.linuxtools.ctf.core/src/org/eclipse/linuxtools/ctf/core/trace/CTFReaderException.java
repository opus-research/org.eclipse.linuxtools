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

    /**
     * Re-thrrow the exception but read it's data
     *
     * @param e
     *            the previous recognition exception
     * @throws IllegalAccessException
     * @since 2.0
     */
    public CTFReaderException(RecognitionException e) throws IllegalAccessException {

        super(e);
        this.fErrorLine = e.line;
        this.fToken = e.token;
        this.fFile = "metadata"; //$NON-NLS-1$ // we're in CTF, the only thing using antlr is metadata
        // UGLY UGLY UGLY
        if (e instanceof MismatchedTokenException) {
            MismatchedTokenException m = (MismatchedTokenException) e;
            for (Field f : CTFLexer.class.getDeclaredFields()) {
                f.setAccessible(true);

                String name;
                int value;
                try {
                    name = f.getName();

                    final boolean isInt = (f.getType().isPrimitive());
                    if (isInt) {
                        value = ((Integer) f.get(null)).intValue();
                        if (value == m.expecting) {
                            this.fExpectingName = name;
                            this.fExpectedValue = value;
                        }
                        if (value == m.c) {
                            this.fActualName = name;
                            this.fActualValue = value;
                        }
                    }
                } catch (NullPointerException e1) {
                    // Pokemon, gotta catch em all!
                } catch (IllegalArgumentException e1) {
                    throw e1;
                } catch (IllegalAccessException e1) {
                    throw e1;
                }
            }
        }
    }

    @Override
    public String getMessage() {
        final String message = super.getMessage();
        if (fErrorLine == -1) {
            return message;
        }
        String expected = "" + this.fExpectedValue; //$NON-NLS-1$
        String actual = "" + this.fActualValue; //$NON-NLS-1$
        String newMessage = message.replaceAll(expected, this.fExpectingName);
        newMessage = newMessage.replaceAll(actual, this.fActualName);
        return newMessage + " at " + fFile + ":" + fErrorLine; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
