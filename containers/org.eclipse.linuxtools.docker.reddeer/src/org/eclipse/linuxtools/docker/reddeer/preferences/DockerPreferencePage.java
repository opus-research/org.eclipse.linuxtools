/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.docker.reddeer.preferences;

import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.preference.PreferencePage;
import org.eclipse.reddeer.swt.impl.text.LabeledText;

/**
 * 
 * 
 * @author jkopriva@redhat.com
 */

public class DockerPreferencePage extends PreferencePage {

	public DockerPreferencePage(ReferencedComposite referenced) {
		super(referenced, "Docker");
	}

	// Following constructor no longer works
	public DockerPreferencePage() {
		super(null, "Docker");
	}
	
	public void setContainerRefreshRate(int seconds) {
		new LabeledText("Container Refresh Rate (seconds)").setText(String.valueOf(seconds));
	}
}
