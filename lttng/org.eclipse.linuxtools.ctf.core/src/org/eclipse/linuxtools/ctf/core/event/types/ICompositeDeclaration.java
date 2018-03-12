/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.scope.LexicalScope;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * Composite declaration, it is a base for struct and struct-like accelerator
 * declarations. It has a
 * {@link ICompositeDeclaration#createDefinition(IDefinitionScope, LexicalScope, BitBuffer)}
 * method that should be faster than the standard
 * {@link IDeclaration#createDefinition(IDefinitionScope, String, BitBuffer)} by
 * taking a scope and returns an {@link ICompositeDefinition}.
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
@NonNullByDefault
public interface ICompositeDeclaration extends IDeclaration {

    /**
     * Create a definition from this declaration. This is a faster constructor
     * as it has a lexical scope and this does not need to look it up.
     *
     * @param definitionScope
     *            the definition scope, the parent where the definition will be
     *            placed
     * @param fieldScope
     *            the scope of the definition
     * @param input
     *            a bitbuffer to read from
     * @return a reference to the definition
     * @throws CTFReaderException
     *             error in reading
     */
    public ICompositeDefinition createDefinition(@Nullable IDefinitionScope definitionScope, LexicalScope fieldScope, BitBuffer input) throws CTFReaderException;

    /**
     * Get the field declaration corresponding to a field name.
     *
     * @param fieldName
     *            The field name
     * @return The declaration of the field, or null if there is no such field.
     */
    @Nullable
    public IDeclaration getField(String fieldName);

    /**
     * Gets the field list. Very important since the map of fields does not
     * retain the order of the fields.
     *
     * @return the field list.
     */
    public Iterable<String> getFieldsList();
}
