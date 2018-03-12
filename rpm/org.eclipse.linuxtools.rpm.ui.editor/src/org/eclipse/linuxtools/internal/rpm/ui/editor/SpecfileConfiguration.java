/*******************************************************************************
 * Copyright (c) 2007, 2017 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TabsToSpacesConverter;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.linuxtools.internal.rpm.ui.editor.hyperlink.URLHyperlinkWithMacroDetector;
import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfilePartitionScanner;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.HyperlinkDetectorDescriptor;
import org.eclipse.ui.texteditor.HyperlinkDetectorRegistry;
import org.osgi.framework.FrameworkUtil;

public class SpecfileConfiguration extends TextSourceViewerConfiguration {
	private SpecfileDoubleClickStrategy doubleClickStrategy;
	private SpecfileEditor editor;
	private IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE,
			FrameworkUtil.getBundle(SpecfileConfiguration.class).getSymbolicName());

	public SpecfileConfiguration(SpecfileEditor editor) {
		super();
		this.editor = editor;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return SpecfilePartitionScanner.SPEC_PARTITION_TYPES;
	}

	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		if (doubleClickStrategy == null) {
			doubleClickStrategy = new SpecfileDoubleClickStrategy();
		}
		return doubleClickStrategy;
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new SpecfileHover();
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		return new SpecfilePrecentationReconciler();
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (editor != null && editor.isEditable()) {
			SpecfileReconcilingStrategy strategy = new SpecfileReconcilingStrategy(editor);
			MonoReconciler reconciler = new MonoReconciler(strategy, false);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			reconciler.setDelay(500);
			return reconciler;
		}
		return null;
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		IContentAssistProcessor processor = new SpecfileCompletionProcessor();
		// add content assistance to all the supported contentType
		assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(processor, SpecfilePartitionScanner.SPEC_PREP);
		assistant.setContentAssistProcessor(processor, SpecfilePartitionScanner.SPEC_SCRIPT);
		assistant.setContentAssistProcessor(processor, SpecfilePartitionScanner.SPEC_FILES);
		assistant.setContentAssistProcessor(processor, SpecfilePartitionScanner.SPEC_CHANGELOG);
		assistant.setContentAssistProcessor(processor, SpecfilePartitionScanner.SPEC_PACKAGES);
		assistant.setContentAssistProcessor(processor, SpecfilePartitionScanner.SPEC_GROUP);
		// configure content assistance
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(parent -> new DefaultInformationControl(parent, false));
		assistant.enableAutoInsert(true);
		assistant.setStatusLineVisible(true);
		assistant.setStatusMessage(Messages.SpecfileConfiguration_0);
		return assistant;
	}

	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		if (sourceViewer == null) {
			return null;
		}
		Map<String, IAdaptable> targets = getHyperlinkDetectorTargets(sourceViewer);
		HyperlinkDetectorRegistry hlDetectorRegistry = EditorsUI.getHyperlinkDetectorRegistry();
		HyperlinkDetectorDescriptor[] hlDetectorDescriptor = hlDetectorRegistry.getHyperlinkDetectorDescriptors();
		List<IHyperlinkDetector> tempHDList = new ArrayList<>();

		for (Map.Entry<String, IAdaptable> entry : targets.entrySet()) {
			for (HyperlinkDetectorDescriptor hdd : hlDetectorDescriptor) {
				try {
					AbstractHyperlinkDetector ahld = (AbstractHyperlinkDetector) hdd
							.createHyperlinkDetectorImplementation();
					// filter using target id and not instance of
					// URLHyperlinkDetector
					// so that an option to open url with unresolved macros
					// won't show
					// however, allow URLHyperlinkWithMacroDetector
					if (hdd.getTargetId().equals(entry.getKey()) && (!(ahld instanceof URLHyperlinkDetector)
							|| ahld instanceof URLHyperlinkWithMacroDetector)) {
						ahld.setContext(entry.getValue());
						tempHDList.add(ahld);
					}
				} catch (CoreException e) {
					SpecfileLog.logError(e);
				}
			}
		}

		if (!tempHDList.isEmpty()) {
			return tempHDList.toArray(new IHyperlinkDetector[tempHDList.size()]);
		} else {
			return null;
		}
	}

	@Override
	protected Map<String, IAdaptable> getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
		Map<String, IAdaptable> targets = super.getHyperlinkDetectorTargets(sourceViewer);
		targets.put("org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditorTarget", editor); //$NON-NLS-1$
		targets.put("org.eclipse.ui.DefaultTextEditor", editor); //$NON-NLS-1$
		return targets;
	}

	private int getTabSize() {
		return store.getInt(PreferenceConstants.P_NBR_OF_SPACES_FOR_TAB);
	}

	private boolean isTabConversionEnabled() {
		return store.getBoolean(PreferenceConstants.P_SPACES_FOR_TABS);
	}

	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		if (isTabConversionEnabled()) {
			TabsToSpacesConverter tabsConverter = new TabsToSpacesConverter();
			tabsConverter.setLineTracker(new DefaultLineTracker());
			tabsConverter.setNumberOfSpacesPerTab(getTabSize());
			return new IAutoEditStrategy[] { tabsConverter };
		}
		return null;
	}

}