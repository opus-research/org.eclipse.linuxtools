/*******************************************************************************
 * Copyright (c) 2008 Phil Muldoon <pkmuldoon@picobot.org>.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API and implementation.
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.linuxtools.systemtap.ui.editor.SimpleDocumentProvider;

public class STPDocumentProvider extends SimpleDocumentProvider {

	@Override
	protected void setupDocument(IDocument document) {
		if (document != null) {
			IDocumentPartitioner partitioner = new FastPartitioner(
					new STPPartitionScanner(), new String[] {
							STPPartitionScanner.STP_COMMENT,
							STPPartitionScanner.STP_PROBE });
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
	}

	/**
	 * Instantiates and returns a new AnnotationModel object.
	 */
	@Override
	protected IAnnotationModel createAnnotationModel(Object element) {
		return new AnnotationModel();
	}

}
