/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Ericsson.
 *
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.ISDPreferences;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.SDViewPref;

/**
 * Class to add a hot spot marker.
 *
 * @version 1.0
 * @author sveyrier
 */
public class HotSpot extends GraphNode {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The grahNode ID constant
     */
    public static final String GLYPH = "Glyph"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The execution occurrence the hot spot marker is for.
     */
    protected BasicExecutionOccurrence fExecOcc = null;
    /**
     * The occurrence number.
     */
    protected int fOccurrence = 0;
    /**
     * The marker image to display.
     */
    protected IImage fImage = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public HotSpot() {
        fPrefId = ISDPreferences.PREF_EXEC;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Set the marker image.
     *
     * @param img A image to set
     */
    public void setImage(IImage img) {
        fImage = img;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getX()
     */
    @Override
    public int getX() {
        if (fExecOcc != null) {
            return fExecOcc.getX() - 3;
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getY()
     */
    @Override
    public int getY() {
        if (fExecOcc != null){
            return fExecOcc.getY();
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getWidth()
     */
    @Override
    public int getWidth() {
        if (fExecOcc != null) {
            return fExecOcc.getWidth() + 7;
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getHeight()
     */
    @Override
    public int getHeight() {
        if (fExecOcc != null) {
            return fExecOcc.getWidth() + 10;
        }
        return 0;
    }

    /**
     * Set the lifeline on which the execution occurrence appears.
     *
     * @param occ the parent lifeline
     */
    public void setExecution(BasicExecutionOccurrence occ) {
        fExecOcc = occ;
        fExecOcc.addNode(this);
    }

    /**
     * Get the lifeline on which the execution occurrence appears.
     *
     * @return - the parent lifeline
     */
    public BasicExecutionOccurrence getExecOcc() {
        return fExecOcc;
    }

    /**
     * Returns the occurrence number.
     *
     * @return the occurrence number.
     */
    public int getOccurrence() {
        return fOccurrence;
    }

    /**
     * Set the occurrence number.
     *
     * @param occ A number to set.
     */
    public void setOccurrence(int occ) {
        fOccurrence = occ;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#draw(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC)
     */
    @Override
    public void draw(IGC context) {

        ISDPreferences pref = SDViewPref.getInstance();

        // The execution occurrence is selected
        // if the owning lifeline is selected
        if (isSelected() || (fExecOcc != null && fExecOcc.isSelected()) || (fExecOcc != null && fExecOcc.getLifeline() != null && fExecOcc.getLifeline().isSelected())) {
            context.setBackground(pref.getBackGroundColorSelection());
            context.setForeground(pref.getForeGroundColorSelection());
        } else {
            context.setBackground(pref.getBackGroundColor(ISDPreferences.PREF_EXEC));
            context.setForeground(pref.getForeGroundColor(ISDPreferences.PREF_EXEC));
        }
        context.drawImage(fImage, getX(), getY(), getWidth(), getHeight());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getArrayId()
     */
    @Override
    public String getArrayId() {
        return GLYPH;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#isVisible(int, int, int, int)
     */
    @Override
    public boolean isVisible(int x, int y, int width, int height) {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#contains(int, int)
     */
    @Override
    public boolean contains(int xValue, int yValue) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        if (GraphNode.contains(x, y, width, height, xValue, yValue)) {
            return true;
        }
        return false;
    }
}
