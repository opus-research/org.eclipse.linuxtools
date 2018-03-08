/*******************************************************************************
 * Copyright (c) 2008 Alexander Kurtakov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions;

import java.util.List;

import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSection;

/**
 * Resolution for the no-%build-section rpmlint warning.
 * Fix is to put an empty %build section.
 *
 */
public class NoBuildSection extends AInsertLineResolution {
	/**
	 * The string ID of the rpmlint warning.
	 */
	public static final String ID = "no-%build-section"; //$NON-NLS-1$

	/**
	 * @see org.eclipse.ui.IMarkerResolution2#getDescription()
	 */
	public String getDescription() {
		return Messages.NoBuildSection_0;
	}

	/**
	 * @see org.eclipse.ui.IMarkerResolution#getLabel()
	 */
	public String getLabel() {
		return ID;
	}

	/**
	 * @see org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions.AInsertLineResolution#getLineToInsert()
	 */
	@Override
	public String getLineToInsert() {
		return "%build\n\n"; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.linuxtools.internal.rpm.rpmlint.resolutions.AInsertLineResolution#getLineNumberForInsert(org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor)
	 */
	@Override
	public int getLineNumberForInsert(SpecfileEditor editor) {
		List<SpecfileSection> sections = editor.getSpecfile().getSections();
		for (SpecfileSection section : sections) {
			if (section.getName().equals("install")) { //$NON-NLS-1$
				return section.getLineNumber();
			}
		}
		return 0;
	}
}
