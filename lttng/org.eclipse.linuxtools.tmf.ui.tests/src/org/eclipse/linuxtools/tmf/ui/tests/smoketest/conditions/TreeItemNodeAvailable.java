/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.smoketest.conditions;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

class TreeItemNodeAvailable implements ICondition {

    private final SWTBotTreeItem fTreeItem;
    private final String fName;

    public TreeItemNodeAvailable(String name, SWTBotTreeItem treeItem) {
        fName = name;
        fTreeItem = treeItem;
    }

    @Override
    public boolean test() throws Exception {
        try {
            return fTreeItem.getNode(fName) != null;
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public void init(SWTBot bot) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getFailureMessage() {
        // TODO Auto-generated method stub
        return null;
    }

}
