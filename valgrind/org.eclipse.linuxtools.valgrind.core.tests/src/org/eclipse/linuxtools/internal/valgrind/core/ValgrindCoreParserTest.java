/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.core;

import java.io.File;
import java.io.IOException;

import org.eclipse.debug.core.ILaunch;
import org.junit.Test;
import org.mockito.Mockito;

public class ValgrindCoreParserTest {
    @Test(expected = IOException.class)

    public void test() throws IOException {
        ValgrindCoreParser valgrindCoreParser = new ValgrindCoreParser(new File("/tmp/valgrind_aaa01.txt"),
                (ILaunch) null);
        valgrindCoreParser.getMessages();
    }

    public void testLaunch() throws IOException {
        ILaunch l = Mockito.mock(ILaunch.class);
        ValgrindCoreParser valgrindCoreParser = new ValgrindCoreParser(new File("/tmp/valgrind_aaa01.txt"),
                l);
        valgrindCoreParser.getMessages();
    }
}
