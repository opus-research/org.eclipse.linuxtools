/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Yuriy Vashchuk (yvashchuk@gmail.com) - Initial API and implementation
 *   Patrick Tasse - Refactoring
 *   Vincent Perot - Add subfield filtering
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.filter.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;

/**
 * The base class for the Filter tree nodes
 *
 * @version 1.0
 * @author Yuriy Vashchuk
 * @author Patrick Tasse
 */
public abstract class TmfFilterTreeNode implements ITmfFilterTreeNode, Cloneable {

    private static final char SLASH_CHAR = '/';
    private static final String SLASH_STRING = "/"; //$NON-NLS-1$

    private static final String[] VALID_CHILDREN = {
            TmfFilterEventTypeNode.NODE_NAME,
            TmfFilterAndNode.NODE_NAME,
            TmfFilterOrNode.NODE_NAME,
            TmfFilterContainsNode.NODE_NAME,
            TmfFilterEqualsNode.NODE_NAME,
            TmfFilterMatchesNode.NODE_NAME,
            TmfFilterCompareNode.NODE_NAME
    };

    private ITmfFilterTreeNode parent = null;
    private ArrayList<ITmfFilterTreeNode> children = new ArrayList<>();

    /**
     * @param parent
     *            the parent node
     */
    public TmfFilterTreeNode(final ITmfFilterTreeNode parent) {
        if (parent != null) {
            parent.addChild(this);
        }
    }

    @Override
    public ITmfFilterTreeNode getParent() {
        return parent;
    }

    @Override
    public abstract String getNodeName();

    @Override
    public boolean hasChildren() {
        return (children.size() > 0);
    }

    @Override
    public int getChildrenCount() {
        return children.size();
    }

    @Override
    public ITmfFilterTreeNode[] getChildren() {
        return children.toArray(new ITmfFilterTreeNode[0]);
    }

    @Override
    public ITmfFilterTreeNode getChild(final int index) throws IndexOutOfBoundsException {
        return children.get(index);
    }

    @Override
    public ITmfFilterTreeNode remove() {
        if (getParent() != null) {
            getParent().removeChild(this);
        }
        return this;
    }

    @Override
    public ITmfFilterTreeNode removeChild(ITmfFilterTreeNode node) {
        children.remove(node);
        node.setParent(null);
        return node;
    }

    @Override
    public int addChild(final ITmfFilterTreeNode node) {
        node.setParent(this);
        if (children.add(node)) {
            return (children.size() - 1);
        }
        return -1;
    }

    @Override
    public ITmfFilterTreeNode replaceChild(final int index, final ITmfFilterTreeNode node) throws IndexOutOfBoundsException {
        node.setParent(this);
        return children.set(index, node);
    }

    @Override
    public void setParent(final ITmfFilterTreeNode parent) {
        this.parent = parent;
    }

    @Override
    public abstract boolean matches(ITmfEvent event);

    /**
     * @param event the event
     * @param field the field id
     * @return the field value
     */
    protected Object getFieldValue(ITmfEvent event, String field) {
        Object value = null;
        if (ITmfEvent.EVENT_FIELD_CONTENT.equals(field)) {
            value = event.getContent().toString();
        }
        else if (ITmfEvent.EVENT_FIELD_TYPE.equals(field)) {
            value = event.getType().getName();
        }
        else if (ITmfEvent.EVENT_FIELD_TIMESTAMP.equals(field)) {
            value = event.getTimestamp().toString();
        }
        else if (ITmfEvent.EVENT_FIELD_SOURCE.equals(field)) {
            value = event.getSource();
        }
        else if (ITmfEvent.EVENT_FIELD_REFERENCE.equals(field)) {
            value = event.getReference();
        }
        else {
            ITmfEventField eventField;
            if (!(field.startsWith(SLASH_STRING))) {
                eventField = event.getContent().getField(field);
            } else {
                List<String> list = getPathList(field);
                ITmfEventField topField = event.getContent();
                eventField = getSubField(topField, list);
            }

            if (eventField != null) {
                value = eventField.getValue();
            }
        }
        return value;
    }

    private static ITmfEventField getSubField(ITmfEventField field, Collection<String> list) {
        ITmfEventField subfield = field;
        for (String name : list) {
            subfield = subfield.getField(name);
            if (subfield == null) {
                return null;
            }
        }
        return subfield;

    }

    private static List<String> getPathList(String field) {

        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();

        // We start at 1 since the first character is a slash that we want to
        // ignore.
        for (int i = 1; i < field.length(); i++) {
            char charAt = field.charAt(i);
            if (charAt == SLASH_CHAR) {

                // Count slashes
                int startIndex = i;
                while (charAt == SLASH_CHAR && i < field.length() - 1) {
                    i++;
                    charAt = field.charAt(i);
                }
                // Calculate slashes
                int nbSlashes = ((charAt == SLASH_CHAR) ? i - startIndex + 1 : i - startIndex);

                // Reposition index
                if (i != field.length() - 1) {
                    i--;
                }

                // Add half of the slashes
                for (int j = 0; j < nbSlashes / 2; j++) {
                    sb.append(SLASH_CHAR);
                }

                // Cut if uneven.
                if (nbSlashes % 2 != 0) {
                    list.add(sb.toString());
                    sb = new StringBuilder();
                    continue;
                }
            } else {
                sb.append(charAt);
            }
        }

        // Last block. add it to list.
        list.add(sb.toString());

        return list;
    }

    @Override
    public List<String> getValidChildren() {
        return Arrays.asList(VALID_CHILDREN);
    }

    @Override
    public ITmfFilterTreeNode clone() {
        try {
            TmfFilterTreeNode clone = (TmfFilterTreeNode) super.clone();
            clone.parent = null;
            clone.children = new ArrayList<>(children.size());
            for (ITmfFilterTreeNode child : getChildren()) {
                clone.addChild(child.clone());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
