/*******************************************************************************
 * Copyright (c) 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.ctf.core.event.types;

/**
 * Primitive declaration, has a getSize()
 *
 * @author Matthew Khouzam
 *
 */
public interface IPrimitiveDeclaration extends IDeclaration {
    /**
     * Gets the size of the field, in bytes
     * @return the size in bytes
     */
    long getSize();
}
