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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.linuxtools.valgrind.core.tests.TestSourceReader;
import org.junit.Test;
import org.mockito.Mockito;

public class ValgrindCoreParserTest {
    protected String getAboveComment() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String name = stackTraceElements[2].getMethodName();
        return getContents(1, name)[0].toString();
    }

    protected StringBuilder[] getContents(int sections, String name) {
        try {
            return TestSourceReader.getContentsForTest(getPlugin().getBundle(), getSourcePrefix(), getClass(), name, sections);
        } catch (IOException e) {
            fail(e.getMessage());
            return null;
        }
    }

    protected String getSourcePrefix() {
        return "src";
    }

    protected Plugin getPlugin() {
        ValgrindCoreActivator plugin = ValgrindCoreActivator.getDefault();
        return plugin;
    }

    @Test(expected = IOException.class)
    public void test() throws IOException {
        ValgrindCoreParser valgrindCoreParser = new ValgrindCoreParser(new File("/tmp/valgrind_aaa01.txt"),
                (ILaunch) null);
        valgrindCoreParser.getMessages();
    }

    // la la
    @Test
    public void testLaunch() throws IOException {
        String str = getAboveComment();
        System.err.println(str);
        ILaunch l = Mockito.mock(ILaunch.class);
        ValgrindCoreParser valgrindCoreParser = new ValgrindCoreParser(new File("/tmp/valgrind_aaa01.txt"), l);
        valgrindCoreParser.getMessages();
    }
}
