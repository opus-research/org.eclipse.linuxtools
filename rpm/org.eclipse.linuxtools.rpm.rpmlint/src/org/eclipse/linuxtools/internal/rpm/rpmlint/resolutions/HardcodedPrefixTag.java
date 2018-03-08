/*******************************************************************************
 * Copyright (c) 2008, 2013 Alexander Kurtakov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions;


/**
 * Quick fix for the hardcoded-prefix-tag warning.
 *
 */
public class HardcodedPrefixTag extends ARemoveLineResolution{
    /**
     * Rpmlint warning id.
     */
    public static final String ID = "hardcoded-prefix-tag"; //$NON-NLS-1$

    @Override
    public String getDescription() {
        return Messages.HardcodedPrefixTag_0;
    }

    @Override
    public String getLabel() {
        return ID;
    }
}
