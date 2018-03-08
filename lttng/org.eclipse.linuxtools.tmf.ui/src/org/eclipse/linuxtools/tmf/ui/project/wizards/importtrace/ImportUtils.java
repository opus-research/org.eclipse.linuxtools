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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.ui.dialogs.FileSystemElement;

/**
 * Utilities for trace import
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
final public class ImportUtils {

    /**
     * Custom text label
     */
    public static final String CUSTOM_TXT_CATEGORY = "Custom Text"; //$NON-NLS-1$
    /**
     * Custom XML label
     */
    public static final String CUSTOM_XML_CATEGORY = "Custom XML"; //$NON-NLS-1$

    // The mapping of available trace type IDs to their corresponding
    // configuration element
    private static final Map<String, IConfigurationElement> fTraceTypeAttributes = new HashMap<String, IConfigurationElement>();
    private static final Map<String, IConfigurationElement> fTraceCategories = new HashMap<String, IConfigurationElement>();
    private static final Map<String, IConfigurationElement> fTraceAttributes = new HashMap<String, IConfigurationElement>();
    private static final Map<String, TraceType> fTraceTypes = new LinkedHashMap<String, TraceType>();

    // private static final HashMap<String, List<String>> fTraceTypes = new
    // HashMap<String, List<String>>();
    // private static final List<ITmfTrace> fTmfTraceTypes = new
    // ArrayList<ITmfTrace>();
    // //private static final Map<String, ITmfTrace> fTmfTraceTypeByName = new
    // HashMap<String, ITmfTrace>();
    // private static final List<String> fTraceCategoryNames = new
    // ArrayList<String>();
    // private static final List<String> fTraceHumanNames = new
    // ArrayList<String>();
    // private static final Map<String, String> fHumanToIDTraceNames = new
    // HashMap<String, String>();

    private ImportUtils() {

    }

    /**
     * @param bitw
     *            the wizard to scan
     */
    public static void buildFilesToScan(BatchImportTraceWizard bitw) {
        bitw.clearFilesToScan();
        for (String s : bitw.getFileNames()) {
            recurseFiles(new File(s), bitw);
        }
    }

    // ------------------------------------------------------------------
    // Get trace types
    // ------------------------------------------------------------------

    /**
     * @return returns a list of "category : tracetype , ..."
     */
    public static String[] getAvailableTraceTypes() {

        populateCategoriesAndTraceTypes();

        // Generate the list of Category:TraceType to populate the ComboBox
        List<String> traceTypes = new ArrayList<String>();

        List<String> customTypes = getCustomTraceTypes();
        traceTypes.addAll(customTypes);

        // Format result
        return traceTypes.toArray(new String[traceTypes.size()]);
    }

    /**
     * @param type
     *            the type to get (Text, xml or other...)
     * @return the list of custom trace types
     */
    public static List<String> getCustomTraceTypes(String type) {
        List<String> traceTypes = new ArrayList<String>();
        if (type.equals(CUSTOM_TXT_CATEGORY)) {
            for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                String traceTypeName = def.definitionName;
                traceTypes.add(traceTypeName);
            }
        }
        if (type.equals(CUSTOM_XML_CATEGORY)) {
            for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                String traceTypeName = def.definitionName;
                traceTypes.add(traceTypeName);
            }
        }
        return traceTypes;
    }

    /**
     * @return the list of custom trace types
     */
    public static List<String> getCustomTraceTypes() {
        List<String> traceTypes = new ArrayList<String>();
        // remove the customTraceTypes
        final String[] keySet = fTraceTypes.keySet().toArray(new String[0]);
        for (String key : keySet) {
            if (fTraceTypes.get(key).getCategoryName().equals(CUSTOM_TXT_CATEGORY) || fTraceTypes.get(key).getCategoryName().equals(CUSTOM_XML_CATEGORY)) {
                fTraceTypes.remove(key);
            }
        }

        // add the custom trace types
        for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
            String traceTypeName = CUSTOM_TXT_CATEGORY + " : " + def.definitionName; //$NON-NLS-1$
            TraceType tt = new TraceType(traceTypeName, CUSTOM_TXT_CATEGORY, def.definitionName, null);
            fTraceTypes.put(traceTypeName, tt);
            traceTypes.add(traceTypeName);
        }
        for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
            String traceTypeName = CUSTOM_XML_CATEGORY + " : " + def.definitionName; //$NON-NLS-1$
            TraceType tt = new TraceType(traceTypeName, CUSTOM_TXT_CATEGORY, def.definitionName, null);
            fTraceTypes.put(traceTypeName, tt);
            traceTypes.add(traceTypeName);
        }
        return traceTypes;
    }

    public static TraceType getTraceType(String id){
        return fTraceTypes.get(id);
    }

    /**
     *
     */
    private static void populateCategoriesAndTraceTypes() {
        if (fTraceTypes.isEmpty()) {
            // Populate the Categories and Trace Types
            IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(TmfTraceType.TMF_TRACE_TYPE_ID);
            for (IConfigurationElement ce : config) {
                String elementName = ce.getName();
                if (elementName.equals(TmfTraceType.TYPE_ELEM)) {
                    String traceTypeId = ce.getAttribute(TmfTraceType.ID_ATTR);
                    fTraceTypeAttributes.put(traceTypeId, ce);
                } else if (elementName.equals(TmfTraceType.CATEGORY_ELEM)) {
                    String categoryId = ce.getAttribute(TmfTraceType.ID_ATTR);
                    fTraceCategories.put(categoryId, ce);
                }
            }
            // create the trace types
            for (String typeId : fTraceTypeAttributes.keySet()) {
                IConfigurationElement ce = fTraceTypeAttributes.get(typeId);
                final String category = getCategory(ce);
                final String attribute = ce.getAttribute(TmfTraceType.NAME_ATTR);
                ITmfTrace trace = null;
                try {
                    trace = (ITmfTrace) ce.createExecutableExtension(TmfTraceType.TRACE_TYPE_ATTR);
                } catch (CoreException e) {
                }
                TraceType tt = new TraceType(typeId, category, attribute, trace);
                fTraceTypes.put(typeId, tt);
            }
        }
    }

    private static String getCategory(IConfigurationElement ce) {
        final String categoryId = ce.getAttribute(TmfTraceType.CATEGORY_ATTR);
        if (categoryId != null) {
            IConfigurationElement category = fTraceCategories.get(categoryId);
            if (category != null && !category.getName().equals("")) { //$NON-NLS-1$
                return category.getAttribute(TmfTraceType.NAME_ATTR);
            }
        }
        return "[no category]"; //$NON-NLS-1$
    }

    /**
     * Walks into directories and provides a file
     *
     * @param bitw
     *            the wizard to check
     * @return the amount of files to scan
     */
    public static int getFileCount(BatchImportTraceWizard bitw) {
        if (bitw.getFilesToScan().isEmpty()) {
            buildFilesToScan(bitw);
        }
        return bitw.getFilesToScan().size();
    }

    /**
     * @return the list of trace categories
     */
    public static List<String> getTraceCategories() {
        init();
        List<String> categoryNames = new ArrayList<String>();
        for( String key : fTraceTypes.keySet()){
            final String categoryName = fTraceTypes.get(key).getCategoryName();
            if( !categoryNames.contains(categoryName)){
                categoryNames.add(categoryName);
            }
        }
        return categoryNames;
    }

    /**
     * @param category
     *            the category to lookup
     * @return the trace types
     */

    public static List<TraceType> getTraceTypes(String category) {
        init();
        List<TraceType> traceNames = new ArrayList<TraceType>();
        for( String key : fTraceTypes.keySet()){
            final String categoryName = fTraceTypes.get(key).getCategoryName();
            if( categoryName.equals(category)){
                traceNames.add(fTraceTypes.get(key));
            }
        }
        return traceNames;
    }

    private static void init() {
        populateCategoriesAndTraceTypes();
        getCustomTraceTypes();

    }

    private static List<File> isolateTraces(List<FileSystemElement> selectedResources) {

        List<File> traces = new ArrayList<File>();

        // Get the selection
        Iterator<FileSystemElement> resources = selectedResources.iterator();

        // Get the sorted list of unique entries
        Map<String, File> fileSystemObjects = new HashMap<String, File>();
        while (resources.hasNext()) {
            File resource = (File) resources.next().getFileSystemObject();
            String key = resource.getAbsolutePath();
            fileSystemObjects.put(key, resource);
        }
        List<String> files = new ArrayList<String>(fileSystemObjects.keySet());
        Collections.sort(files);

        // After sorting, traces correspond to the unique prefixes
        String prefix = null;
        for (int i = 0; i < files.size(); i++) {
            File file = fileSystemObjects.get(files.get(i));
            String name = file.getAbsolutePath();
            if (prefix == null || !name.startsWith(prefix)) {
                prefix = name; // new prefix
                traces.add(file);
            }
        }

        return traces;
    }

    private static void recurseFiles(final File f, final BatchImportTraceWizard bitw) {
        // arbitrary value
        if (!bitw.hasFileToScan(f.getAbsolutePath()) && (bitw.getFilesToScan().size() < 65536)) {
            bitw.addFileToScan(f.getAbsolutePath());
            final File[] listFiles = f.listFiles();
            if (listFiles != null) {
                for (File child : listFiles) {
                    recurseFiles(child, bitw);
                }
            }
        }
    }

    /**
     * @param traceCat
     *            the trace category
     * @param fileName
     * @return
     */
    public static boolean validate(String traceCat, String fileName) {
        File f = new File(fileName);
        List<File> fileList = new ArrayList<File>();
        fileList.add(new File(fileName));
        return validateTraceFiles(traceCat, fileList);
    }

    /**
     * @param traceToValidate
     *            the trace category
     * @return
     */
    public static boolean validate(TraceToValidate traceToValidate) {
        return validate(traceToValidate.getTraceType(), traceToValidate.getTraceToScan());
    }

    /**
     * @param traceTypeName
     * @param selectedResources
     * @return true if the traces are valid
     */
    public static boolean validateTrace(String traceTypeName, List<FileSystemElement> selectedResources) {
        List<File> traces = isolateTraces(selectedResources);
        return validateTraceFiles(traceTypeName, traces);
    }

    /**
     * @param traceTypeName
     * @param traces
     * @return true if the traces are valid
     */
    public static boolean validateTraceFiles(String traceTypeName, List<File> traces) {
        if (traceTypeName != null && !"".equals(traceTypeName) && //$NON-NLS-1$
                !traceTypeName.startsWith(ImportUtils.CUSTOM_TXT_CATEGORY) && !traceTypeName.startsWith(ImportUtils.CUSTOM_XML_CATEGORY)) {
            for (File trace : traces) {
                if (!fTraceTypes.get(traceTypeName).validate(trace.getAbsolutePath())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param traceType
     *            tt
     * @return ce
     */
    public static IConfigurationElement getTraceAttributes(String traceType) {
        return fTraceAttributes.get(traceType);
    }
}
