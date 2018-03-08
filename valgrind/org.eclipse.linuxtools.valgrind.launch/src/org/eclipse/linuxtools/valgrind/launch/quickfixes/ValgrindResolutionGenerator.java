/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Medeiros Teixeira <rafaelmt@linux.vnet.ibm.com> - initial API and implementation
*******************************************************************************/

package org.eclipse.linuxtools.valgrind.launch.quickfixes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.linuxtools.internal.valgrind.launch.Messages;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class ValgrindResolutionGenerator implements IMarkerResolutionGenerator {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$


	public IMarkerResolution[] getResolutions(IMarker marker) {
		List<IMarkerResolution> resolutionsList = new ArrayList<IMarkerResolution>();
		String message = marker.getAttribute(IMarker.MESSAGE, EMPTY_STRING );
		if(message.contains(Messages.getString("ValgrindMemcheckQuickFixes.Wrong_dealloc_message"))){ //$NON-NLS-1$
			resolutionsList.add(new ValgrindWrongDeallocResolution(marker));
		}

		IMarkerResolution[] resolutions = new IMarkerResolution[resolutionsList.size()];
		resolutionsList.toArray(resolutions);

		return resolutions;
	}
}
