/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphing.core;

import org.eclipse.swt.graphics.RGB;

public interface IGraphColorConstants {
    RGB[] COLORS = new RGB[] {


        new RGB(0,102,204),
        new RGB(51,153,255),
        new RGB(102,204,255),

        new RGB(0,51,153),
        new RGB(153,153,255),
        new RGB(102,102,255),
        new RGB(51,51,255),
        new RGB(102,153,255),
        new RGB(153,204,255),
        new RGB(204,255,255),
        new RGB(102,204,204),
        new RGB(0,153,153),
        new RGB(0,102,102),
        new RGB(0,51,102),
        new RGB(0,153,255),



        new RGB(202,204,255),

        /*new RGB(153,102,102),
        new RGB(153,153,153),
        new RGB(204,102,103),
        new RGB(204,153,102),
        new RGB(204,153,51),*/

        new RGB(204,153,153),
        new RGB(204,204,204),
        new RGB(255,153,153),
        new RGB(255,204,153),
        new RGB(255,204,102),
        new RGB(204,204,153),
        new RGB(255,255,153),
        new RGB(255,255,51),
        new RGB(255,204,153),
        new RGB(153,204,153),
        new RGB(102,204,204),
        new RGB(153,204,204),
        new RGB(153,153,204),
        new RGB(153,102,153),
        new RGB(204,153,204),
        new RGB(153,153,104),

        new RGB(0, 0, 255),
        new RGB(255, 0, 0),
        new RGB(0, 255, 0),
        new RGB(255, 0, 255),
        new RGB(0, 255, 255),
        new RGB(255, 255, 0),
        new RGB(127, 127, 255),
        new RGB(255, 127, 127),
        new RGB(127, 255, 127),

        new RGB(255, 255, 255),
        new RGB(255, 255, 204),
        new RGB(255, 255, 153),
        new RGB(255, 255, 102),
        new RGB(255, 255, 51),
        new RGB(255, 255, 0),
        new RGB(255, 204, 255),
        new RGB(255, 204, 204),
        new RGB(255, 204, 153),
        new RGB(255, 204, 102),
        new RGB(255, 204, 51),
        new RGB(255, 204, 0),
        new RGB(255, 153, 255),
        new RGB(255, 153, 204),
        new RGB(255, 153, 153),
        new RGB(255, 153, 102),
        new RGB(255, 153, 51),
        new RGB(255, 153, 0),
        new RGB(255, 102, 255),
        new RGB(255, 102, 204),
        new RGB(255, 102, 153),
        new RGB(255, 102, 102),
        new RGB(255, 102, 51),
        new RGB(255, 102, 0),
        new RGB(255, 51, 255),
        new RGB(255, 51, 204),
        new RGB(255, 51, 153),
        new RGB(255, 51, 102),
        new RGB(255, 51, 51),
        new RGB(255, 51, 0),
        new RGB(255, 0, 255),
        new RGB(255, 0, 204),
        new RGB(255, 0, 153),
        new RGB(255, 0, 102),
        new RGB(255, 0, 51),
        new RGB(255, 0, 0),
        new RGB(204, 255, 255),
        new RGB(204, 255, 204),
        new RGB(204, 255, 153),
        new RGB(204, 255, 102),
        new RGB(204, 255, 51),
        new RGB(204, 255, 0),
        new RGB(204, 204, 255),
        new RGB(204, 204, 204),
        new RGB(204, 204, 153),
        new RGB(204, 204, 102),
        new RGB(204, 204, 51),
        new RGB(204, 204, 0),
        new RGB(204, 153, 255),
        new RGB(204, 153, 204),
        new RGB(204, 153, 153),
        new RGB(204, 153, 102),
        new RGB(204, 153, 51),
        new RGB(204, 153, 0),
        new RGB(204, 102, 255),
        new RGB(204, 102, 204),
        new RGB(204, 102, 153),
        new RGB(204, 102, 102),
        new RGB(204, 102, 51),
        new RGB(204, 102, 0),
        new RGB(204, 51, 255),
        new RGB(204, 51, 204),
        new RGB(204, 51, 153),
        new RGB(204, 51, 102),
        new RGB(204, 51, 51),
        new RGB(204, 51, 0),
        new RGB(204, 0, 255),
        new RGB(204, 0, 204),
        new RGB(204, 0, 153),
        new RGB(204, 0, 102),
        new RGB(204, 0, 51),
        new RGB(204, 0, 0),
        new RGB(153, 255, 255),
        new RGB(153, 255, 204),
        new RGB(153, 255, 153),
        new RGB(153, 255, 102),
        new RGB(153, 255, 51),
        new RGB(153, 255, 0),
        new RGB(153, 204, 255),
        new RGB(153, 204, 204),
        new RGB(153, 204, 153),
        new RGB(153, 204, 102),
        new RGB(153, 204, 51),
        new RGB(153, 204, 0),
        new RGB(153, 153, 255),
        new RGB(153, 153, 204),
        new RGB(153, 153, 153),
        new RGB(153, 153, 102),
        new RGB(153, 153, 51),
        new RGB(153, 153, 0),
        new RGB(153, 102, 255),
        new RGB(153, 102, 204),
        new RGB(153, 102, 153),
        new RGB(153, 102, 102),
        new RGB(153, 102, 51),
        new RGB(153, 102, 0),
        new RGB(153, 51, 255),
        new RGB(153, 51, 204),
        new RGB(153, 51, 153),
        new RGB(153, 51, 102),
        new RGB(153, 51, 51),
        new RGB(153, 51, 0),
        new RGB(153, 0, 255),
        new RGB(153, 0, 204),
        new RGB(153, 0, 153),
        new RGB(153, 0, 102),
        new RGB(153, 0, 51),
        new RGB(153, 0, 0),
        new RGB(102, 255, 255),
        new RGB(102, 255, 204),
        new RGB(102, 255, 153),
        new RGB(102, 255, 102),
        new RGB(102, 255, 51),
        new RGB(102, 255, 0),
        new RGB(102, 204, 255),
        new RGB(102, 204, 204),
        new RGB(102, 204, 153),
        new RGB(102, 204, 102),
        new RGB(102, 204, 51),
        new RGB(102, 204, 0),
        new RGB(102, 153, 255),
        new RGB(102, 153, 204),
        new RGB(102, 153, 153),
        new RGB(102, 153, 102),
        new RGB(102, 153, 51),
        new RGB(102, 153, 0),
        new RGB(102, 102, 255),
        new RGB(102, 102, 204),
        new RGB(102, 102, 153),
        new RGB(102, 102, 102),
        new RGB(102, 102, 51),
        new RGB(102, 102, 0),
        new RGB(102, 51, 255),
        new RGB(102, 51, 204),
        new RGB(102, 51, 153),
        new RGB(102, 51, 102),
        new RGB(102, 51, 51),
        new RGB(102, 51, 0),
        new RGB(102, 0, 255),
        new RGB(102, 0, 204),
        new RGB(102, 0, 153),
        new RGB(102, 0, 102),
        new RGB(102, 0, 51),
        new RGB(102, 0, 0),
        new RGB(51, 255, 255),
        new RGB(51, 255, 204),
        new RGB(51, 255, 153),
        new RGB(51, 255, 102),
        new RGB(51, 255, 51),
        new RGB(51, 255, 0),
        new RGB(51, 204, 255),
        new RGB(51, 204, 204),
        new RGB(51, 204, 153),
        new RGB(51, 204, 102),
        new RGB(51, 204, 51),
        new RGB(51, 204, 0),
        new RGB(51, 153, 255),
        new RGB(51, 153, 204),
        new RGB(51, 153, 153),
        new RGB(51, 153, 102),
        new RGB(51, 153, 51),
        new RGB(51, 153, 0),
        new RGB(51, 102, 255),
        new RGB(51, 102, 204),
        new RGB(51, 102, 153),
        new RGB(51, 102, 102),
        new RGB(51, 102, 51),
        new RGB(51, 102, 0),
        new RGB(51, 51, 255),
        new RGB(51, 51, 204),
        new RGB(51, 51, 153),
        new RGB(51, 51, 102),
        new RGB(51, 51, 51),
        new RGB(51, 51, 0),
        new RGB(51, 0, 255),
        new RGB(51, 0, 204),
        new RGB(51, 0, 153),
        new RGB(51, 0, 102),
        new RGB(51, 0, 51),
        new RGB(51, 0, 0),
        new RGB(0, 255, 255),
        new RGB(0, 255, 204),
        new RGB(0, 255, 153),
        new RGB(0, 255, 102),
        new RGB(0, 255, 51),
        new RGB(0, 255, 0),
        new RGB(0, 204, 255),
        new RGB(0, 204, 204),
        new RGB(0, 204, 153),
        new RGB(0, 204, 102),
        new RGB(0, 204, 51),
        new RGB(0, 204, 0),
        new RGB(0, 153, 255),
        new RGB(0, 153, 204),
        new RGB(0, 153, 153),
        new RGB(0, 153, 102),
        new RGB(0, 153, 51),
        new RGB(0, 153, 0),
        new RGB(0, 102, 255),
        new RGB(0, 102, 204),
        new RGB(0, 102, 153),
        new RGB(0, 102, 102),
        new RGB(0, 102, 51),
        new RGB(0, 102, 0),
        new RGB(0, 51, 255),
        new RGB(0, 51, 204),
        new RGB(0, 51, 153),
        new RGB(0, 51, 102),
        new RGB(0, 51, 51),
        new RGB(0, 51, 0),
        new RGB(0, 0, 255),
        new RGB(0, 0, 204),
        new RGB(0, 0, 153),
        new RGB(0, 0, 102),
        new RGB(0, 0, 51),
        new RGB(0, 0, 0)
    };


}
