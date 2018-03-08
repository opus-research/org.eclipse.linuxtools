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

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author ematkho
 * @since 2.0
 *
 */
public class ImportTraceContentProvider implements ITreeContentProvider {

    final Set<String> fTraceTypes = new TreeSet<String>();
    final HashMap<String, Set<FileAndName>> fTraceFiles = new HashMap<String, Set<FileAndName>>();

    /**
     * @param category
     *            the category of the trace
     * @param traceToOpen
     *            the trace file.
     */
    public void addCandidate(String category, File traceToOpen) {
        fTraceTypes.add(category);
        if (!fTraceFiles.containsKey(category)) {
            fTraceFiles.put(category, new TreeSet<FileAndName>());
        }
        fTraceFiles.get(category).add(new FileAndName(traceToOpen, traceToOpen.getName()));
    }

    public void clearCandidates() {
        fTraceTypes.clear();
        fTraceFiles.clear();
    }

    @Override
    public void dispose() {
        fTraceFiles.clear();
        fTraceTypes.clear();

    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (oldInput != newInput) {
            if (newInput != null) {
                ImportTraceContentProvider input = (ImportTraceContentProvider) newInput;
                fTraceTypes.clear();
                fTraceTypes.addAll(input.fTraceTypes);
                fTraceFiles.clear();
                fTraceFiles.putAll(fTraceFiles);
            }
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return fTraceTypes.toArray(new String[fTraceTypes.size()]);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof String) {
            return fTraceFiles.get(parentElement).toArray(new FileAndName[fTraceFiles.get(parentElement).size()]);
        }
        return null;
    }

    /**
     * @param element
     * @return
     */
    public FileAndName[] getSiblings(FileAndName element) {
        String key = (String) getParent(element);
        return (FileAndName[]) getChildren(key);

    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof FileAndName) {
            for (String key : fTraceFiles.keySet()) {
                Set<FileAndName> fanSet = fTraceFiles.get(key);
                if (fanSet.contains(element)) {
                    return key;
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof String) {
            String key = (String) element;
            return fTraceFiles.containsKey(key);
        }
        return false;
    }
};