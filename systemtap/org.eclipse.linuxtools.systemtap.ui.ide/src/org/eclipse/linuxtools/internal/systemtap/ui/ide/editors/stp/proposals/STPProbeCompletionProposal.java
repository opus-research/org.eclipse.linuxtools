/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp.proposals;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetItemType;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;

public class STPProbeCompletionProposal extends STPCompletionProposal {

    public STPProbeCompletionProposal(TreeNode completionNode, int prefixLength,
            int replacementOffset) {
        super(completionNode, prefixLength, replacementOffset);
    }

    @Override
    protected TapsetItemType getType() {
        return TapsetItemType.PROBE;
    }

}
