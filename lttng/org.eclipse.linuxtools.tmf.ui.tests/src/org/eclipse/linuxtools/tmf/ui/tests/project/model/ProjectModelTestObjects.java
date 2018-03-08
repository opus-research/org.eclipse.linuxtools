/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.project.model;

import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.tmf.ui.project.model.TmfImportHelper;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;

/**
 * Creates objects used for this package's testing purposes
 */
public class ProjectModelTestObjects {

    /** Default test project name */
    public static final String PROJECT_NAME = "Test_Project";

    private static final int TRACE_INDEX = 0;
    private static final String PATH = CtfTmfTestTraces.getTestTracePath(TRACE_INDEX);

    /**
     * Gets a project element with traces all initialized
     *
     * @return A project stub element
     * @throws CoreException If something happened with the project creation
     */
    public static TmfProjectElement getFilledProject() throws CoreException {

        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
        if (!project.exists()) {
            project.create(null);
        }
        project.open(null);
        IFolder folder = project.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME);
        if (!folder.exists()) {
            folder.create(true, true, null);
        }

        /* Create a trace, if it exist, it will be replaced */
        File file = new File(PATH);
        String path = file.getAbsolutePath();
        final IPath pathString = Path.fromOSString(path);
        IResource linkedTrace = TmfImportHelper.createLink(folder, pathString, pathString.lastSegment());
        if (!(linkedTrace != null && linkedTrace.exists())) {
            return null;
        }
        linkedTrace.setPersistentProperty(TmfCommonConstants.TRACETYPE,
                "org.eclipse.linuxtools.tmf.ui.type.ctf");

        final TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
        TmfTraceElement traceElement = projectElement.getTracesFolder().getTraces().get(0);
        traceElement.refreshTraceType();

        return projectElement;
    }

    /**
     * Get the name of the test trace element
     *
     * @return The trace name
     */
    public static String getTraceName() {
        File file = new File(PATH);
        String path = file.getAbsolutePath();
        final IPath pathString = Path.fromOSString(path);
        return pathString.lastSegment();
    }

}
