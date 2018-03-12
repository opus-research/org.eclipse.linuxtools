/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.actions;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;


/**
 * Abstract implementation of <code>IStorageEditorInput</code>.
 */
public abstract class StorageEditorInput implements IStorageEditorInput {

    /**
     * Storage associated with this editor input
     */
    private IStorage fStorage;

    /**
     * Constructs an editor input on the given storage
     */
    public StorageEditorInput(IStorage storage) {
        fStorage = storage;
    }

    /**
     * @see IStorageEditorInput#getStorage()
     */
    @Override
    public IStorage getStorage() {
        return fStorage;
    }

    /**
     * @see IStorageEditorInput#getImageDescriptor()
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    /**
     * @see IStorageEditorInput#getName()
     */
    @Override
    public String getName() {
        return getStorage().getName();
    }

    /**
     * @see IStorageEditorInput#getPersistable()
     */
    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    /**
     * @see IStorageEditorInput#getToolTipText()
     */
    @Override
    public String getToolTipText() {
        return getStorage().getFullPath().toOSString();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        try {
            return object instanceof IStorageEditorInput
                && getStorage().equals(((IStorageEditorInput)object).getStorage());
        } catch (CoreException e) {
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getStorage().hashCode();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings({ "rawtypes" })
    public Object getAdapter(Class adapter) {
        return null;
    }

}
