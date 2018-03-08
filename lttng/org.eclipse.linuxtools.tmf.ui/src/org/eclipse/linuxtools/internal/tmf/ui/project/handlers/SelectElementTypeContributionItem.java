/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Create this abstract class to allow selection of
 *                experiment type as well
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * ContributionItem for the element type selection.
 *
 * @author Patrick Tassé
 */
public abstract class SelectElementTypeContributionItem extends CompoundContributionItem {

    //private static final ImageDescriptor SELECTED_ICON = ImageDescriptor.createFromImage(TmfUiPlugin.getDefault().getImageFromPath("icons/elcl16/bullet.gif")); //$NON-NLS-1$
    private static final ImageDescriptor SELECTED_ICON = Activator.getDefault().getImageDescripterFromPath("icons/elcl16/bullet.gif"); //$NON-NLS-1$
    private static final String CUSTOM_TXT_CATEGORY = "Custom Text"; //$NON-NLS-1$
    private static final String CUSTOM_XML_CATEGORY = "Custom XML"; //$NON-NLS-1$

    @Override
    protected abstract IContributionItem[] getContributionItems();

    /**
     * Get the contribution items that are meant to be trace type or experiment
     * type
     *
     * @param forExperiment
     *            Whether to get the contribution items for a trace or an
     *            experiment
     * @return The list of contribution items
     */
    protected IContributionItem[] getContributionItems(boolean forExperiment) {

        Set<String> selectedTraceTypes = new HashSet<String>();
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        ISelection selection = page.getSelection();
        if (selection instanceof StructuredSelection) {
            for (Object element : ((StructuredSelection) selection).toList()) {
                if (!forExperiment && element instanceof TmfTraceElement) {
                    TmfTraceElement trace = (TmfTraceElement) element;
                    selectedTraceTypes.add(trace.getTraceType());
                } else if (forExperiment && element instanceof TmfExperimentElement) {
                    TmfExperimentElement trace = (TmfExperimentElement) element;
                    selectedTraceTypes.add(trace.getTraceType());
                }
            }
        }

        List<IContributionItem> list = new LinkedList<IContributionItem>();

        Map<String, MenuManager> categoriesMap = new HashMap<String, MenuManager>();
        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
                TmfTraceType.TMF_TRACE_TYPE_ID);
        for (IConfigurationElement ce : config) {
            if (ce.getName().equals(TmfTraceType.CATEGORY_ELEM)) {
                boolean isExperiment = Boolean.valueOf(ce.getAttribute(TmfTraceType.IS_EXPERIMENT_ATTR)).booleanValue();
                // Add the category if both isExperiment and forExperiment are equals
                if ( (isExperiment && forExperiment) || (!isExperiment && !forExperiment)) {
                    String categoryId = ce.getAttribute(TmfTraceType.ID_ATTR);
                    ImageDescriptor icon = isSelectedCategory(categoryId, config, selectedTraceTypes) ? SELECTED_ICON : null;
                    MenuManager subMenu = new MenuManager(ce.getAttribute(TmfTraceType.NAME_ATTR), icon, null);
                    categoriesMap.put(categoryId, subMenu);
                    list.add(subMenu);
                }
            }
        }

        CustomTxtTraceDefinition[] customTxtTraceDefinitions = CustomTxtTraceDefinition.loadAll();
        if (customTxtTraceDefinitions.length > 0) {
            ImageDescriptor icon = isSelectedCategory(customTxtTraceDefinitions, selectedTraceTypes) ? SELECTED_ICON : null;
            MenuManager subMenu = new MenuManager(CUSTOM_TXT_CATEGORY, icon, null);
            categoriesMap.put(CUSTOM_TXT_CATEGORY, subMenu);
            list.add(subMenu);
        }
        CustomXmlTraceDefinition[] customXmlTraceDefinitions = CustomXmlTraceDefinition.loadAll();
        if (customXmlTraceDefinitions.length > 0) {
            ImageDescriptor icon = isSelectedCategory(customXmlTraceDefinitions, selectedTraceTypes) ? SELECTED_ICON : null;
            MenuManager subMenu = new MenuManager(CUSTOM_XML_CATEGORY, icon, null);
            categoriesMap.put(CUSTOM_XML_CATEGORY, subMenu);
            list.add(subMenu);
        }

        for (IConfigurationElement ce : config) {
            if (ce.getName().equals(TmfTraceType.TYPE_ELEM)) {
                boolean isExperiment = Boolean.valueOf(ce.getAttribute(TmfTraceType.IS_EXPERIMENT_ATTR)).booleanValue();
                // Add the contribution item if both isExperiment and forExperiment are equals
                if ( (isExperiment && forExperiment) || (!isExperiment && !forExperiment)) {
                    String traceBundle = ce.getContributor().getName();
                    String traceTypeId = ce.getAttribute(TmfTraceType.ID_ATTR);
                    String traceIcon = ce.getAttribute(TmfTraceType.ICON_ATTR);
                    String label = ce.getAttribute(TmfTraceType.NAME_ATTR).replaceAll("&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$
                    boolean selected = selectedTraceTypes.contains(traceTypeId);
                    MenuManager subMenu = categoriesMap.get(ce.getAttribute(TmfTraceType.CATEGORY_ATTR));

                    addContributionItem(list, traceBundle, traceTypeId, traceIcon, label, selected, subMenu);
                }
            }
        }

        // add the custom trace types
        for (CustomTxtTraceDefinition def : customTxtTraceDefinitions) {
            String traceBundle = Activator.getDefault().getBundle().getSymbolicName();
            String traceTypeId = CustomTxtTrace.class.getCanonicalName() + ":" + def.definitionName; //$NON-NLS-1$
            String traceIcon = getDefaultIconPath();
            String label = def.definitionName;
            boolean selected = selectedTraceTypes.contains(traceTypeId);
            MenuManager subMenu = categoriesMap.get(CUSTOM_TXT_CATEGORY);

            addContributionItem(list, traceBundle, traceTypeId, traceIcon, label, selected, subMenu);
        }
        for (CustomXmlTraceDefinition def : customXmlTraceDefinitions) {
            String traceBundle = Activator.getDefault().getBundle().getSymbolicName();
            String traceTypeId = CustomXmlTrace.class.getCanonicalName() + ":" + def.definitionName; //$NON-NLS-1$
            String traceIcon = getDefaultIconPath();
            String label = def.definitionName;
            boolean selected = selectedTraceTypes.contains(traceTypeId);
            MenuManager subMenu = categoriesMap.get(CUSTOM_XML_CATEGORY);

            addContributionItem(list, traceBundle, traceTypeId, traceIcon, label, selected, subMenu);
        }

        return list.toArray(new IContributionItem[list.size()]);
    }

    /**
     * Get the bundle parameter name
     *
     * @return The Bundle parameter name
     */
    protected abstract String getBundleParameter();

    /**
     * Get the icon parameter name
     *
     * @return The icon parameter name
     */
    protected abstract String getIconParameter();

    /**
     * Get the type parameter name
     *
     * @return The type parameter name
     */
    protected abstract String getTypeParameter();

    /**
     * Get the command id
     *
     * @return The command id
     */
    protected abstract String getCommandId();

    /**
     * Get the default icon path
     *
     * @return The default icon path
     */
    protected abstract String getDefaultIconPath();

    private void addContributionItem(List<IContributionItem> list,
            String traceBundle, String traceTypeId, String traceIcon,
            String label, boolean selected,
            MenuManager subMenu) {
        Map<String, String> params;

        params = new HashMap<String, String>();
        params.put(getBundleParameter(), traceBundle);
        params.put(getTypeParameter(), traceTypeId);
        params.put(getIconParameter(), traceIcon);

        ImageDescriptor icon = null;
        if (selected) {
            icon = SELECTED_ICON;
        }

        CommandContributionItemParameter param = new CommandContributionItemParameter(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow(), // serviceLocator
                "my.parameterid", // id //$NON-NLS-1$
                getCommandId(), // commandId
                CommandContributionItem.STYLE_PUSH // style
        );
        param.parameters = params;
        param.icon = icon;
        param.disabledIcon = icon;
        param.hoverIcon = icon;
        param.label = label;
        param.visibleEnabled = true;

        if (subMenu != null) {
            subMenu.add(new CommandContributionItem(param));
        } else {
            list.add(new CommandContributionItem(param));
        }
    }

    private static boolean isSelectedCategory(String categoryId, IConfigurationElement[] config, Set<String> selectedTraceTypes) {
        for (IConfigurationElement ce : config) {
            if (ce.getName().equals(TmfTraceType.TYPE_ELEM)) {
                String traceTypeId = ce.getAttribute(TmfTraceType.ID_ATTR);
                if (selectedTraceTypes.contains(traceTypeId)) {
                    if (categoryId.equals(ce.getAttribute(TmfTraceType.CATEGORY_ATTR))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isSelectedCategory(CustomTxtTraceDefinition[] customTxtTraceDefinitions, Set<String> selectedTraceTypes) {
        for (CustomTxtTraceDefinition def : customTxtTraceDefinitions) {
            String traceTypeId = CustomTxtTrace.class.getCanonicalName() + ":" + def.definitionName; //$NON-NLS-1$
            if (selectedTraceTypes.contains(traceTypeId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSelectedCategory(CustomXmlTraceDefinition[] customXmlTraceDefinitions, Set<String> selectedTraceTypes) {
        for (CustomXmlTraceDefinition def : customXmlTraceDefinitions) {
            String traceTypeId = CustomXmlTrace.class.getCanonicalName() + ":" + def.definitionName; //$NON-NLS-1$
            if (selectedTraceTypes.contains(traceTypeId)) {
                return true;
            }
        }
        return false;
    }
}
