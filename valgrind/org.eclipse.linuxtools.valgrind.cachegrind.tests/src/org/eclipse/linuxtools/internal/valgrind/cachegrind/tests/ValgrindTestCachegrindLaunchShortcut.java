/*******************************************************************************
 * Copyright (c) 2009, 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Red Hat Inc. - modified to use with Cachegrind testing
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.cachegrind.tests;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.cachegrind.CachegrindLaunchShortcut;

public class ValgrindTestCachegrindLaunchShortcut extends CachegrindLaunchShortcut {

    private ILaunchConfiguration config;

    @Override
    public void launch(IBinary bin, String mode) {
        config = findLaunchConfiguration(bin, mode);
    }

    public ILaunchConfiguration getConfig() {
        return config;
    }
}
