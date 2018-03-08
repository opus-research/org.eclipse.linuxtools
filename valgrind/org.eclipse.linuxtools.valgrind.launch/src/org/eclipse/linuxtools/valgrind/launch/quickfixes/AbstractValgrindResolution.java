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

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;


public abstract class AbstractValgrindResolution extends AbstractCodanCMarkerResolution {

	protected IMarker marker;
	protected IDocument document;
	protected IASTTranslationUnit ast;
	protected IASTNode node;

	public AbstractValgrindResolution(IMarker marker) {
		super();
		this.marker = marker;
		this.ast = getASTTranslationUnit(marker);
		this.document = openDocument(marker);
	}

	public abstract String getLabel();

	protected IASTNode getIASTNode(IMarker marker){
		int offset = this.getOffset(marker, document);
		int length = this.getLength(marker);

		IASTNode node = null;
		IASTTranslationUnit ast = getASTTranslationUnit(marker);

		IASTNodeSelector nodeSelector = ast.getNodeSelector(marker.getResource().getLocationURI().getPath());
		node = nodeSelector.findFirstContainedNode(offset, length);

		return node;
	}

	protected IASTTranslationUnit getASTTranslationUnit(IMarker marker){
		ITranslationUnit tu = getTranslationUnitViaEditor(marker);
		try {
			IASTTranslationUnit ast = tu.getAST();
			return ast;
		} catch (CoreException e) {
			return null;
		}
	}

	protected int getLength(IMarker mk) {
		int charStart = mk.getAttribute(IMarker.CHAR_START, -1);
		int charEnd = mk.getAttribute(IMarker.CHAR_END, -1);
		if (charEnd != -1 && charStart != -1)
			return charEnd- charStart;
		int line = mk.getAttribute(IMarker.LINE_NUMBER, -1) -1;
		try {
			return document.getLineLength(line);
		} catch (BadLocationException e) {
			return -1;
		}
    }
}
