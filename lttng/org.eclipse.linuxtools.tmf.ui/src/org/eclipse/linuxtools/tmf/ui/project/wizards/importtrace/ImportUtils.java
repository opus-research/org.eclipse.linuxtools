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
public abstract class ImportUtils {

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
    private static final HashMap<String, List<String>> fTraceTypes = new HashMap<String, List<String>>();
    private static final List<ITmfTrace> fTmfTraceTypes = new ArrayList<ITmfTrace>();
    private static final Map<String, ITmfTrace> fTmfTraceTypeByName = new HashMap<String, ITmfTrace>();
    private static final List<String> fTraceCategoryNames = new ArrayList<String>();
    private static final Map<String, String> fHumanToIDTraceNames = new HashMap<String, String>();

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
     * @param type the type to get (Text, xml or other...)
     * @return the list of custom trace types
     */
    public static List<String> getCustomTraceTypes(String type){
        List<String> traceTypes = new ArrayList<String>();
        if( type.equals(CUSTOM_TXT_CATEGORY)) {
            for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                String traceTypeName = def.definitionName;
                traceTypes.add(traceTypeName);
            }
        }
        if( type.equals(CUSTOM_XML_CATEGORY)) {
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
        if (!fTraceTypes.containsKey(CUSTOM_TXT_CATEGORY)) {
            fTraceTypes.put(CUSTOM_TXT_CATEGORY, new ArrayList<String>());
        }
        if (!fTraceTypes.containsKey(CUSTOM_XML_CATEGORY)) {
            fTraceTypes.put(CUSTOM_XML_CATEGORY, new ArrayList<String>());
        }
        fTraceTypes.get(CUSTOM_TXT_CATEGORY).clear();
        fTraceTypes.get(CUSTOM_XML_CATEGORY).clear();

        // add the custom trace types
        for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
            String traceTypeName = CUSTOM_TXT_CATEGORY + " : " + def.definitionName; //$NON-NLS-1$

            traceTypes.add(traceTypeName);
            if (!fTraceTypes.get(CUSTOM_TXT_CATEGORY).contains(traceTypeName)) {
                fTraceTypes.get(CUSTOM_TXT_CATEGORY).add(def.definitionName);
            }
        }
        for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
            String traceTypeName = CUSTOM_XML_CATEGORY + " : " + def.definitionName; //$NON-NLS-1$

            traceTypes.add(traceTypeName);
            if (!fTraceTypes.get(CUSTOM_XML_CATEGORY).contains(traceTypeName)) {
                fTraceTypes.get(CUSTOM_XML_CATEGORY).add(def.definitionName);
            }
        }
        return traceTypes;
    }

    /**
     *
     */
    private static void populateCategoriesAndTraceTypes() {
        if (fTraceCategoryNames.isEmpty()) {
            // reset everything
            fTraceCategoryNames.clear();
            fTraceTypeAttributes.clear();
            fTraceCategories.clear();
            fTraceAttributes.clear();
            fTraceTypes.clear();
            fTmfTraceTypes.clear();
            fHumanToIDTraceNames.clear();

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
                    fTraceCategoryNames.add(categoryId);
                }
            }

            for (String typeId : fTraceTypeAttributes.keySet()) {
                IConfigurationElement ce = fTraceTypeAttributes.get(typeId);
                final String category = getCategory(ce);
                final String attribute = ce.getAttribute(TmfTraceType.NAME_ATTR);
                String traceTypeName = category + " : " + attribute; //$NON-NLS-1$
                fTraceAttributes.put(traceTypeName, ce);
                fHumanToIDTraceNames.put(ce.getAttribute(TmfTraceType.NAME_ATTR), traceTypeName);
                List<String> categoryList = fTraceTypes.get(category);
                if (categoryList == null) {
                    fTraceTypes.put(category, new ArrayList<String>());
                    categoryList = fTraceTypes.get(category);
                }
                int posToAdd = -1;
                for (int i = 0; i < categoryList.size(); i++) {
                    if (traceTypeName.compareTo(categoryList.get(i)) < 0) {
                        posToAdd = i;
                    } else {
                        break;
                    }
                }
                if (posToAdd > 0) {
                    categoryList.add(posToAdd + 1, attribute);
                } else {
                    categoryList.add(attribute);
                }
            }
//            fTraceCategoryNames.add(CUSTOM_TXT_CATEGORY);
//            fTraceCategoryNames.add(CUSTOM_XML_CATEGORY);
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
        return fTraceCategoryNames;
    }

    /**
     * @param category
     *            the category to lookup
     * @return the trace types
     */

    public static List<String> getTraceType(String category) {
        init();
        return fTraceTypes.get(category);
    }

    /**
     * @return the set of trace types
     */
    public static Set<String> getTraceTypeNames() {
        init();
        return fTraceTypes.keySet();
    }

    private static void init() {
        populateCategoriesAndTraceTypes();

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
        if (!bitw.HasFileToScan(f.getAbsolutePath()) && (bitw.getFilesToScan().size() < 65536)) {
            bitw.AddFileToScan(f.getAbsolutePath());
            final File[] listFiles = f.listFiles();
            if (listFiles != null) {
                for (File child : listFiles) {
                    recurseFiles(child, bitw);
                }
            }
        }
    }

    /**
     * @param traceCat the trace category
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
     * @param traceCat the trace category
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
                ITmfTrace tmfTrace = null;
                try {
                    final String lookupName = fHumanToIDTraceNames.get(traceTypeName);
                    if( lookupName == null ){
                         // custom parser
                        final File traceFile = new File(trace.getAbsolutePath());
                        return traceFile.exists() && traceFile.isFile();
                    }

                    IConfigurationElement ce = fTraceAttributes.get(lookupName);
                    tmfTrace = (ITmfTrace) ce.createExecutableExtension(TmfTraceType.TRACE_TYPE_ATTR);
                    if (tmfTrace != null && !tmfTrace.validate(null, trace.getAbsolutePath())) {
                        tmfTrace.dispose();
                        return false;
                    }
                } catch (CoreException e) {
                } finally {
                    if (tmfTrace != null) {
                        tmfTrace.dispose();
                    }
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
        return fTraceAttributes.get(fHumanToIDTraceNames.get(traceType));
    }


}
