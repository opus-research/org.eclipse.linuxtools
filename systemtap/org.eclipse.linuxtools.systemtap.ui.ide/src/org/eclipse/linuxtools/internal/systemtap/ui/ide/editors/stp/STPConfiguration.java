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
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.linuxtools.systemtap.ui.editor.ColorManager;
import org.eclipse.linuxtools.systemtap.ui.editor.DoubleClickStrategy;

public class STPConfiguration extends SourceViewerConfiguration {

	private STPElementScanner scanner;
	private ColorManager colorManager;
	private STPEditor editor;
	private DoubleClickStrategy doubleClickStrategy;
	
	public STPConfiguration(ColorManager colorManager, STPEditor editor) {
		this.colorManager = colorManager;
		this.editor = editor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
				IDocument.DEFAULT_CONTENT_TYPE,
				STPPartitionScanner.STP_COMMENT,
				STPPartitionScanner.STP_STRING,
				STPPartitionScanner.STP_PROBE};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();

		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant
				.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		IContentAssistProcessor processor = new STPCompletionProcessor();

		assistant.setContentAssistProcessor(processor,IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(processor,STPPartitionScanner.STP_PROBE);
		
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		return assistant;
	}

	/**
	 * Return the default Element scanner.
	 * 
	 * @return default element scanner.
	 */
	protected STPElementScanner getSTPScanner() {
		if (scanner == null) {
			scanner = new STPElementScanner(colorManager);
			scanner.setDefaultReturnToken(new Token(new TextAttribute(
					colorManager.getColor(STPColorConstants.DEFAULT))));
		}
		return scanner;
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
     * 
     * Return the reconciler built on the custom Systemtap reconciling strategy that enables code folding for this editor.
     */
	@Override
    public IReconciler getReconciler(ISourceViewer sourceViewer)
    {
        STPReconcilingStrategy strategy = new STPReconcilingStrategy();
        strategy.setEditor(editor);        
        MonoReconciler reconciler = new MonoReconciler(strategy,false);        
        return reconciler;
    }
	
	/**
	 * Instantiates and returns a double click strategy object if one does not exist, and returns the 
	 * current one if it does.
	 */
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer,String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new DoubleClickStrategy();
		return doubleClickStrategy;
	}

    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {

		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getSTPScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(getSTPScanner());
		reconciler.setDamager(dr, STPPartitionScanner.STP_COMMENT);
		reconciler.setRepairer(dr, STPPartitionScanner.STP_COMMENT);

		dr = new DefaultDamagerRepairer(getSTPScanner());
		reconciler.setDamager(dr, STPPartitionScanner.STP_STRING);
		reconciler.setRepairer(dr, STPPartitionScanner.STP_STRING);

		dr = new DefaultDamagerRepairer(getSTPScanner());
		reconciler.setDamager(dr, STPPartitionScanner.STP_KEYWORD);
		reconciler.setRepairer(dr, STPPartitionScanner.STP_KEYWORD);

		dr = new DefaultDamagerRepairer(getSTPScanner());
		reconciler.setDamager(dr, STPPartitionScanner.STP_CONDITIONAL);
		reconciler.setRepairer(dr, STPPartitionScanner.STP_CONDITIONAL);

		dr = new DefaultDamagerRepairer(getSTPScanner());
		reconciler.setDamager(dr, STPPartitionScanner.STP_PROBE);
		reconciler.setRepairer(dr, STPPartitionScanner.STP_PROBE);

		return reconciler;
	}

}