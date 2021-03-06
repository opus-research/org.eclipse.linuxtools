/*******************************************************************************
 * Copyright (c) 2008, 2011 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.linuxtools.cdt.libhover.LibhoverPlugin;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHoverMessages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class LibHoverPreferencePage
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

    private final static String CACHE_EXT = "Libhover.CachePreference.msg"; //$NON-NLS-1$
    private final static String LOAD_ON_STARTUP = "Libhover.LoadOnStartup.msg"; //$NON-NLS-1$

    public LibHoverPreferencePage() {
        super(GRID);
        setPreferenceStore(LibhoverPlugin.getDefault().getPreferenceStore());
    }

    /**
     * Creates the field editors. Field editors are abstractions of
     * the common GUI blocks needed to manipulate various types
     * of preferences. Each field editor knows how to save and
     * restore itself.
     */
    @Override
    public void createFieldEditors() {
        addField(
                new BooleanFieldEditor(
                        PreferenceConstants.CACHE_EXT_LIBHOVER,
                        LibHoverMessages.getString(CACHE_EXT),
                        getFieldEditorParent()));
        addField(
                new BooleanFieldEditor(
                        PreferenceConstants.LAZY_LOAD,
                        LibHoverMessages.getString(LOAD_ON_STARTUP),
                        getFieldEditorParent()));

    }

    @Override
    public void init(IWorkbench workbench) {
    }

}