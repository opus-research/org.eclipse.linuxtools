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

package org.eclipse.linuxtools.ctf.core.event.types;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * A CTF string definition (similar to a C null-terminated byte array).
 *
 * Strings are an array of bytes of variable size and are terminated by a '\0'
 * "NULL" character. Their encoding is described in the TSDL meta-data. In
 * absence of encoding attribute information, the default encoding is UTF-8.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class StringDefinition extends Definition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private StringDeclaration declaration;

    private String string;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param declaration
     *            the parent declaration
     * @param definitionScope
     *            the parent scope
     * @param fieldName
     *            the field name
     */
    public StringDefinition(StringDeclaration declaration,
            IDefinitionScope definitionScope, String fieldName) {
        super(definitionScope, fieldName);

        this.declaration = declaration;

        string = ""; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public StringDeclaration getDeclaration() {
        return declaration;
    }

    /**
     * Sets the string declaration
     *
     * @param declaration
     *            the declaration
     */
    public void setDeclaration(StringDeclaration declaration) {
        this.declaration = declaration;
    }

    /**
     * Gets the string (value)
     *
     * @return the string
     */
    public String getValue() {
        return string;
    }

    /**
     * Sets the string (value)
     *
     * @param str the string
     */
    public void setValue(String str) {
        this.string = str;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void read(BitBuffer input) throws CTFReaderException {
        /* Offset the buffer position wrt the current alignment */
        alignRead(input, this.declaration);

        StringBuilder sb = new StringBuilder();
        char c = (char) input.get(8, false);
        while (c != 0) {
            sb.append(c);
            c = (char) input.get(8, false);
        }
        string = sb.toString();
    }

    @Override
    public String toString() {
        return '\"' + getValue() + '\"';
    }

}
