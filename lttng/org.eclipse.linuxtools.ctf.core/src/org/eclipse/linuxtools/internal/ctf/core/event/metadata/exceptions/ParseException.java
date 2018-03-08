/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions;

import java.lang.reflect.Field;

import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.eclipse.linuxtools.ctf.parser.CTFLexer;

/**
 * <b><u>ParseException</u></b>
 */
public class ParseException extends Exception {

    private static final long serialVersionUID = 7901917601459652080L;
    private int fErrorLine;
    private Token fToken;
    private String fFile;
    private String fExpectingName;
    private int fExpectedValue;
    private String fActualName;
    private int fActualValue;

    /**
     * Enpty constructor
     */
    public ParseException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message (to be sent to logs
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Copy constructor
     * @param e the exception to throw
     */
    public ParseException(Exception e) {
        super(e);
    }
    /**
     * Re-throw the exception but read its data
     *
     * @param e
     *            the previous recognition exception (Antlr specific)
     * @throws IllegalAccessException
     * @since 2.0
     */
    public ParseException(RecognitionException e) throws IllegalAccessException {

        super(e);
        this.fErrorLine = e.line;
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
