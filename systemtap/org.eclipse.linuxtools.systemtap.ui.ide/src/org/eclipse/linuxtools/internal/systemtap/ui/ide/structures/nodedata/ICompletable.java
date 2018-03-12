/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.nodedata;

/**
 * An interface for providing completion text for any object
 * that can be typed in a SystemTap script.
 */
public interface ICompletable {
    /**
     * @return The text associated with this object that can be auto-completed
     * and inserted into a script.
     */
    String getCompletionText();
}
