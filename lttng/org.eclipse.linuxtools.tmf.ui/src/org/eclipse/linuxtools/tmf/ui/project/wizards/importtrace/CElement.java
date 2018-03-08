/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.InvalidRegistryObjectException;

/**
 * Configuration wrapper for custom trace types
 * @author Matthew Khouzam
 * @since 2.0
 */
class CElement implements IConfigurationElement{

    final private String fName;

    public CElement(String customCategory, String traceType) {
        fName = customCategory + " : " + traceType; //$NON-NLS-1$
    }
    @Override
    public Object createExecutableExtension(String propertyName) throws CoreException {
        return null;
    }

    @Override
    public String getAttribute(String name) throws InvalidRegistryObjectException {
        return null;
    }

    @Override
    public String getAttribute(String attrName, String locale) throws InvalidRegistryObjectException {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public String getAttributeAsIs(String name) throws InvalidRegistryObjectException {
        return null;
    }

    @Override
    public String[] getAttributeNames() throws InvalidRegistryObjectException {
        return null;
    }

    @Override
    public IConfigurationElement[] getChildren() throws InvalidRegistryObjectException {
        return null;
    }

    @Override
    public IConfigurationElement[] getChildren(String name) throws InvalidRegistryObjectException {
        return null;
    }

    @Override
    public IExtension getDeclaringExtension() throws InvalidRegistryObjectException {
        return null;
    }

    @Override
    public String getName() throws InvalidRegistryObjectException {
        return fName;
    }

    @Override
    public Object getParent() throws InvalidRegistryObjectException {
        return null;
    }

    @Override
    public String getValue() throws InvalidRegistryObjectException {
        return null;
    }

    @Override
    public String getValue(String locale) throws InvalidRegistryObjectException {
        return null;
    }

    @Deprecated
    @Override
    public String getValueAsIs() throws InvalidRegistryObjectException {
        return null;
    }

    @Deprecated
    @Override
    public String getNamespace() throws InvalidRegistryObjectException {
        return null;
    }

    @Override
    public String getNamespaceIdentifier() throws InvalidRegistryObjectException {
        return null;
    }

    @Override
    public IContributor getContributor() throws InvalidRegistryObjectException {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

}
