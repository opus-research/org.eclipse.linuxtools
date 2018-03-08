/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.annotatedsourceeditor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.internal.text.revisions.RevisionPainter;
import org.eclipse.jface.internal.text.source.DiffPainter;
import org.eclipse.jface.text.revisions.IRevisionListener;
import org.eclipse.jface.text.revisions.IRevisionRulerColumn;
import org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension;
import org.eclipse.jface.text.revisions.RevisionInformation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A vertical ruler column displaying line numbers and serving as a UI for quick diff. Clients usually instantiate and
 * configure object of this class.
 * 
 * @since 3.0
 */
public final class STChangeRulerColumn extends STRulerColumn implements IChangeRulerColumn, IRevisionRulerColumn,
        IRevisionRulerColumnExtension {
    /** The ruler's annotation model. */
    private IAnnotationModel fAnnotationModel;
    /** <code>true</code> if changes should be displayed using character indications instead of background colors. */
    private boolean fCharacterDisplay;
    /**
     * The revision painter strategy.
     * 
     * @since 3.2
     */
    private final RevisionPainter fRevisionPainter;
    /**
     * The diff information painter strategy.
     * 
     * @since 3.2
     */
    private final DiffPainter fDiffPainter;
    /**
     * Whether to show number or to behave like a change ruler column.
     * 
     * @since 3.3
     */
    private boolean fShowSTRuler = true;

    /**
     * Creates a new instance.
     * 
     * @param sharedColors
     *            the shared colors provider to use
     */
    public STChangeRulerColumn(ISharedTextColors sharedColors, ISTAnnotationColumn ac) {
        super(ac);
        Assert.isNotNull(sharedColors);
        fRevisionPainter = new RevisionPainter(this, sharedColors);
        fDiffPainter = new DiffPainter(this, sharedColors);
    }

    /*
     * @see
     * org.eclipse.jface.text.source.LineNumberRulerColumn#createControl(org.eclipse.jface.text.source.CompositeRuler,
     * org.eclipse.swt.widgets.Composite)
     */
    @Override
    public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
        Control control = super.createControl(parentRuler, parentControl);
        fRevisionPainter.setParentRuler(parentRuler);
        fDiffPainter.setParentRuler(parentRuler);
        return control;
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
     */
    @Override
    public int getLineOfLastMouseButtonActivity() {
        return getParentRuler().getLineOfLastMouseButtonActivity();
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerInfo#toDocumentLineNumber(int)
     */
    @Override
    public int toDocumentLineNumber(int y_coordinate) {
        return getParentRuler().toDocumentLineNumber(y_coordinate);
    }

    /*
     * @see IVerticalRulerColumn#setModel(IAnnotationModel)
     */
    @Override
    public void setModel(IAnnotationModel model) {
        setAnnotationModel(model);
        fRevisionPainter.setModel(model);
        fDiffPainter.setModel(model);
        updateNumberOfDigits();
        computeIndentations();
        layout(true);
        postRedraw();
    }

    private void setAnnotationModel(IAnnotationModel model) {
        if (fAnnotationModel != model)
            fAnnotationModel = model;
    }

    /**
     * Sets the display mode of the ruler. If character mode is set to <code>true</code>, diff information will be
     * displayed textually on the line number ruler.
     * 
     * @param characterMode
     *            <code>true</code> if diff information is to be displayed textually.
     */
    public void setDisplayMode(boolean characterMode) {
        if (characterMode != fCharacterDisplay) {
            fCharacterDisplay = characterMode;
            updateNumberOfDigits();
            computeIndentations();
            layout(true);
        }
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getModel()
     */
    @Override
    public IAnnotationModel getModel() {
        return fAnnotationModel;
    }

    /*
     * @see org.eclipse.jface.text.source.LineNumberRulerColumn#createDisplayString(int)
     */
    @Override
    protected String createDisplayString(int line) {
        StringBuffer buffer = new StringBuffer();
        if (fShowSTRuler)
            buffer.append(super.createDisplayString(line));
        if (fCharacterDisplay && getModel() != null)
            buffer.append(fDiffPainter.getDisplayCharacter(line));
        return buffer.toString();
    }

    /*
     * @see org.eclipse.jface.text.source.LineNumberRulerColumn#computeNumberOfDigits()
     */
    @Override
    protected int computeNumberOfDigits() {
        int digits;
        if (fCharacterDisplay && getModel() != null) {
            if (fShowSTRuler)
                digits = super.computeNumberOfDigits() + 1;
            else
                digits = 1;
        } else {
            if (fShowSTRuler)
                digits = super.computeNumberOfDigits();
            else
                digits = 0;
        }
        if (fRevisionPainter.hasInformation())
            digits += fRevisionPainter.getRequiredWidth();
        return digits;
    }

    /*
     * @see
     * org.eclipse.jface.text.source.IVerticalRulerInfoExtension#addVerticalRulerListener(org.eclipse.jface.text.source
     * .IVerticalRulerListener)
     */
    @Override
    public void addVerticalRulerListener(IVerticalRulerListener listener) {
        throw new UnsupportedOperationException();
    }

    /*
     * @see
     * org.eclipse.jface.text.source.IVerticalRulerInfoExtension#removeVerticalRulerListener(org.eclipse.jface.text.
     * source.IVerticalRulerListener)
     */
    @Override
    public void removeVerticalRulerListener(IVerticalRulerListener listener) {
        throw new UnsupportedOperationException();
    }

    /*
     * @see org.eclipse.jface.text.source.LineNumberRulerColumn#doPaint(org.eclipse.swt.graphics.GC)
     */
    @Override
    void doPaint(GC gc, ILineRange visibleLines) {
        Color foreground = gc.getForeground();
        if (visibleLines != null) {
            if (fRevisionPainter.hasInformation())
                fRevisionPainter.paint(gc, visibleLines);
            else if (fDiffPainter.hasInformation()) // don't paint quick diff colors if revisions are painted
                fDiffPainter.paint(gc, visibleLines);
        }
        gc.setForeground(foreground);
        if (fShowSTRuler || fCharacterDisplay)
            super.doPaint(gc, visibleLines);
    }

    /*
     * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getHover()
     */
    @Override
    public IAnnotationHover getHover() {
        int activeLine = getParentRuler().getLineOfLastMouseButtonActivity();
        if (fRevisionPainter.hasHover(activeLine))
            return fRevisionPainter.getHover();
        if (fDiffPainter.hasHover(activeLine))
            return fDiffPainter.getHover();
        return null;
    }

    /*
     * @see org.eclipse.jface.text.source.IChangeRulerColumn#setHover(org.eclipse.jface.text.source.IAnnotationHover)
     */
    @Override
    public void setHover(IAnnotationHover hover) {
        fRevisionPainter.setHover(hover);
        fDiffPainter.setHover(hover);
    }

    /*
     * @see org.eclipse.jface.text.source.IChangeRulerColumn#setBackground(org.eclipse.swt.graphics.Color)
     */
    @Override
    public void setBackground(Color background) {
        super.setBackground(background);
        fRevisionPainter.setBackground(background);
        fDiffPainter.setBackground(background);
    }

    /*
     * @see org.eclipse.jface.text.source.IChangeRulerColumn#setAddedColor(org.eclipse.swt.graphics.Color)
     */
    @Override
    public void setAddedColor(Color addedColor) {
        fDiffPainter.setAddedColor(addedColor);
    }

    /*
     * @see org.eclipse.jface.text.source.IChangeRulerColumn#setChangedColor(org.eclipse.swt.graphics.Color)
     */
    @Override
    public void setChangedColor(Color changedColor) {
        fDiffPainter.setChangedColor(changedColor);
    }

    /*
     * @see org.eclipse.jface.text.source.IChangeRulerColumn#setDeletedColor(org.eclipse.swt.graphics.Color)
     */
    @Override
    public void setDeletedColor(Color deletedColor) {
        fDiffPainter.setDeletedColor(deletedColor);
    }

    /*
     * @see
     * org.eclipse.jface.text.revisions.IRevisionRulerColumn#setRevisionInformation(org.eclipse.jface.text.revisions
     * .RevisionInformation)
     */
    @Override
    public void setRevisionInformation(RevisionInformation info) {
        fRevisionPainter.setRevisionInformation(info);
        updateNumberOfDigits();
        computeIndentations();
        layout(true);
        postRedraw();
    }

    /*
     * @see org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension#getRevisionSelectionProvider()
     * 
     * @since 3.2
     */
    @Override
    public ISelectionProvider getRevisionSelectionProvider() {
        return fRevisionPainter.getRevisionSelectionProvider();
    }

    /*
     * @see
     * org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension#setRenderingMode(org.eclipse.jface.text.revisions
     * .IRevisionRulerColumnExtension.RenderingMode)
     * 
     * @since 3.3
     */
    @Override
    public void setRevisionRenderingMode(RenderingMode renderingMode) {
        fRevisionPainter.setRenderingMode(renderingMode);
    }

    /**
     * Sets the line number display mode.
     * 
     * @param showNumbers
     *            <code>true</code> to show numbers, <code>false</code> to only show diff / revision info.
     * @since 3.3
     */
    public void showLineNumbers(boolean showNumbers) {
        if (fShowSTRuler != showNumbers) {
            fShowSTRuler = showNumbers;
            updateNumberOfDigits();
            computeIndentations();
            layout(true);
        }
    }

    /*
     * @see org.eclipse.jface.text.source.LineNumberRulerColumn#getWidth()
     * 
     * @since 3.3
     */
    @Override
    public int getWidth() {
        int width = super.getWidth();
        return width > 0 ? width : 8; // minimal width to display quick diff / revisions if no textual info is shown
    }

    /**
     * Returns <code>true</code> if the ruler is showing line numbers, <code>false</code> otherwise
     * 
     * @return <code>true</code> if line numbers are shown, <code>false</code> otherwise
     * @since 3.3
     */
    public boolean isShowingSTRuler() {
        return fShowSTRuler;
    }

    /**
     * Returns <code>true</code> if the ruler is showing revision information, <code>false</code> otherwise
     * 
     * @return <code>true</code> if revision information is shown, <code>false</code> otherwise
     * @since 3.3
     */
    public boolean isShowingRevisionInformation() {
        return fRevisionPainter.hasInformation();
    }

    /**
     * Returns <code>true</code> if the ruler is showing change information, <code>false</code> otherwise
     * 
     * @return <code>true</code> if change information is shown, <code>false</code> otherwise
     * @since 3.3
     */
    public boolean isShowingChangeInformation() {
        return fDiffPainter.hasInformation();
    }

    /*
     * @see org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension#showRevisionAuthor(boolean)
     * 
     * @since 3.3
     */
    @Override
    public void showRevisionAuthor(boolean show) {
        fRevisionPainter.showRevisionAuthor(show);
        updateNumberOfDigits();
        computeIndentations();
        layout(true);
        postRedraw();
    }

    /*
     * @see org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension#showRevisionId(boolean)
     * 
     * @since 3.3
     */
    @Override
    public void showRevisionId(boolean show) {
        fRevisionPainter.showRevisionId(show);
        updateNumberOfDigits();
        computeIndentations();
        layout(true);
        postRedraw();
    }

    /*
     * @see
     * org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension#addRevisionListener(org.eclipse.jface.text.revisions
     * .IRevisionListener)
     * 
     * @since 3.3
     */
    @Override
    public void addRevisionListener(IRevisionListener listener) {
        fRevisionPainter.addRevisionListener(listener);
    }

    /*
     * @see
     * org.eclipse.jface.text.revisions.IRevisionRulerColumnExtension#removeRevisionListener(org.eclipse.jface.text.
     * revisions.IRevisionListener)
     * 
     * @since 3.3
     */
    @Override
    public void removeRevisionListener(IRevisionListener listener) {
        fRevisionPainter.removeRevisionListener(listener);
    }

    /*
     * @see org.eclipse.jface.text.source.LineNumberRulerColumn#handleDispose()
     * 
     * @since 3.3
     */
    @Override
    protected void handleDispose() {
        fRevisionPainter.setParentRuler(null);
        fRevisionPainter.setModel(null);
        fDiffPainter.setParentRuler(null);
        fDiffPainter.setModel(null);
        super.handleDispose();
    }
}
