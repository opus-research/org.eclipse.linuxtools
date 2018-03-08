/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis (mathieu.denis@polymtl.ca)  - Initial design and implementation
 *   Bernd Hufmann - Fixed warnings
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.statistics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.util.TmfFixedArray;
import org.eclipse.linuxtools.tmf.ui.views.statistics.ITmfExtraEventInfo;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.Messages;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfBaseStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfTreeContentProvider;

/**
 * TmfTreeContentProvider Test Cases.
 */
@SuppressWarnings("nls")
public class TmfTreeContentProviderTest extends TestCase {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private String fTestName = null;

    private final String fContext = "UnitTest";
    private final String fTypeId1 = "Some type1";
    private final String fTypeId2 = "Some type2";

    private final String fLabel0 = "label1";
    private final String fLabel1 = "label2";
    private final String[] fLabels = new String[] { fLabel0, fLabel1 };

    private final TmfTimestamp fTimestamp1 = new TmfTimestamp(12345, (byte) 2, 5);
    private final TmfTimestamp fTimestamp2 = new TmfTimestamp(12350, (byte) 2, 5);

    private final String fSource = "Source";

    private final TmfEventType fType1 = new TmfEventType(fContext, fTypeId1, TmfEventField.makeRoot(fLabels));
    private final TmfEventType fType2 = new TmfEventType(fContext, fTypeId2, TmfEventField.makeRoot(fLabels));

    private final String fReference = "Some reference";

    private final TmfEvent fEvent1;
    private final TmfEvent fEvent2;

    private final TmfEventField fContent1;
    private final TmfEventField fContent2;

    private final TmfBaseStatisticsTree fStatsData;

    private final ITmfExtraEventInfo fExtraInfo;

    private final TmfTreeContentProvider treeProvider;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * @param name
     *            of the test
     */
    public TmfTreeContentProviderTest(final String name) {
        super(name);

        fTestName = name;

        fContent1 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some content");
        fEvent1 = new TmfEvent(null, fTimestamp1, fSource, fType1, fContent1, fReference);

        fContent2 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, "Some other content");
        fEvent2 = new TmfEvent(null, fTimestamp2, fSource, fType2, fContent2, fReference);

        fStatsData = new TmfBaseStatisticsTree();
        fExtraInfo = new ITmfExtraEventInfo() {
            @Override
            public String getTraceName() {
                return name;
            }
        };
        fStatsData.registerEvent(fEvent1, fExtraInfo);
        fStatsData.registerEvent(fEvent2, fExtraInfo);

        treeProvider = new TmfTreeContentProvider();
    }

    // ------------------------------------------------------------------------
    // GetChildren
    // ------------------------------------------------------------------------

    /**
     * Test getting of children.
     */
    public void testGetChildren() {
        Object[] objectArray = treeProvider.getChildren(fStatsData.getOrCreate(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes)));
        TmfStatisticsTreeNode[] childrenNode = Arrays.asList(objectArray).toArray(new TmfStatisticsTreeNode[0]);

        Collection<TmfFixedArray<String>> childrenExpected = new Vector<TmfFixedArray<String>>();
        childrenExpected.add(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().toString()));
        childrenExpected.add(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent2.getType().toString()));

        assertEquals("getChildren", childrenExpected.size(), childrenNode.length);
        // assertTrue("getChildren", childrenPath.equals(childrenExpected));
        for (TmfStatisticsTreeNode childNode : childrenNode) {
            if (childrenExpected.contains(childNode.getPath())) {
                childrenExpected.remove(childNode.getPath());
            } else {
                fail();
            }
        }
    }

    // ------------------------------------------------------------------------
    // GetParent
    // ------------------------------------------------------------------------

    /**
     * Test getting of parent.
     */
    public void testGetParent() {
        TmfStatisticsTreeNode parent = (TmfStatisticsTreeNode) treeProvider.getParent(fStatsData.get(new TmfFixedArray<String>(fTestName)));

        assertNotNull("getParent", parent);
        assertTrue("getParent", parent.getPath().equals(AbsTmfStatisticsTree.ROOT));
    }

    // ------------------------------------------------------------------------
    // HasChildren
    // ------------------------------------------------------------------------
    /**
     * Test checking for children.
     */
    public void testHasChildren() {
        Boolean hasChildren = treeProvider.hasChildren(fStatsData.getOrCreate(AbsTmfStatisticsTree.ROOT));
        assertTrue("hasChildren", hasChildren);

        hasChildren = treeProvider.hasChildren(fStatsData.getOrCreate(new TmfFixedArray<String>(fTestName)));
        assertTrue("hasChildren", hasChildren);

        hasChildren = treeProvider.hasChildren(fStatsData.getOrCreate(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes)));
        assertTrue("hasChildren", hasChildren);

        hasChildren = treeProvider.hasChildren(fStatsData.getOrCreate(new TmfFixedArray<String>(fTestName, Messages.TmfStatisticsData_EventTypes, fEvent1.getType().toString())));
        assertFalse("hasChildren", hasChildren);
    }

    // ------------------------------------------------------------------------
    // GetElements
    // ------------------------------------------------------------------------

    /**
     * Test getting of elements.
     */
    public void testGetElements() {
        Object[] objectElements = treeProvider.getElements(fStatsData.get(AbsTmfStatisticsTree.ROOT));
        TmfStatisticsTreeNode[] nodeElements = Arrays.asList(objectElements).toArray(new TmfStatisticsTreeNode[0]);
        assertEquals("getElements", 1, nodeElements.length);
        assertTrue("getElements", nodeElements[0].getPath().equals(new TmfFixedArray<String>(fTestName)));
    }
}
