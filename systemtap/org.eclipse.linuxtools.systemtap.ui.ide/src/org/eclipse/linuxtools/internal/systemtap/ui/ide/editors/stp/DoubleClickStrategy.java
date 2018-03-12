/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

public class DoubleClickStrategy implements ITextDoubleClickStrategy {
    private ITextViewer fText;

    @Override
    public void doubleClicked(ITextViewer part) {
        int pos = part.getSelectedRange().x;

        if (pos < 0) {
            return;
        }

        fText = part;

        if (!selectComment(pos)) {
            selectWord(pos);
        }
    }

    /**
     * Method handles the selection of comments in the editor.
     *
     * @param caretPos postition of the caret
     */
    private boolean selectComment(int caretPos) {
        IDocument doc = fText.getDocument();
        int startPos, endPos;

        try {
            int pos = caretPos;
            char c = ' ';

            while (pos >= 0) {
                c = doc.getChar(pos);
                if (c == '\\') {
                    pos -= 2;
                    continue;
                }
                if (c == Character.LINE_SEPARATOR || c == '\"') {
                    break;
                }
                --pos;
            }

            if (c != '\"') {
                return false;
            }

            startPos = pos;

            pos = caretPos;
            int length = doc.getLength();
            c = ' ';

            while (pos < length) {
                c = doc.getChar(pos);
                if (c == Character.LINE_SEPARATOR || c == '\"') {
                    break;
                }
                ++pos;
            }
            if (c != '\"') {
                return false;
            }

            endPos = pos;

            int offset = startPos + 1;
            int len = endPos - offset;
            fText.setSelectedRange(offset, len);

            return true;
        } catch (BadLocationException x) {
            // Pass
        }

        return false;
    }

    /**
     * Method handles the selection of words in the editor.
     *
     * @param caretPos postition of the caret
     */
    private boolean selectWord(int caretPos) {
        IDocument doc = fText.getDocument();
        int startPos, endPos;

        try {
            int pos = caretPos;
            char c;

            while (pos >= 0) {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                --pos;
            }

            startPos = pos;

            pos = caretPos;
            int length = doc.getLength();

            while (pos < length) {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                ++pos;
            }

            endPos = pos;
            selectRange(startPos, endPos);

            return true;
        } catch (BadLocationException x) {
            // Pass
        }

        return false;
    }

    private void selectRange(int startPos, int stopPos) {
        int offset = startPos + 1;
        int length = stopPos - offset;
        fText.setSelectedRange(offset, length);
    }
}