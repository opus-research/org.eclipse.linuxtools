/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;

/**
 * The Attribute Tree is the /proc-like filesystem used to organize attributes.
 * Each node of this tree is both like a file and a directory in the
 * "file system".
 *
 * @author alexmont
 *
 */
public class AttributeTree {

    /* "Magic number" for attribute tree files or file sections */
    private final static int ATTRIB_TREE_MAGIC_NUMBER = 0x06EC3671;

    private final StateSystem ss;
    private final List<Attribute> attributeList;
    private final Attribute attributeTreeRoot;

    /**
     * Standard constructor, create a new empty Attribute Tree
     *
     * @param ss
     *            The StateSystem to which this AT is attached
     */
    AttributeTree(StateSystem ss) {
        this.ss = ss;
        this.attributeList = Collections.synchronizedList(new ArrayList<Attribute>());
        this.attributeTreeRoot = new AlphaNumAttribute(null, "root", -1); //$NON-NLS-1$
    }

    /**
     * "Existing file" constructor Builds a attribute tree from a "mapping file"
     * or mapping section previously saved somewhere.
     *
     * @param ss
     *            StateSystem to which this AT is attached
     * @param fis
     *            File stream where to read the AT information. Make sure it's
     *            seeked at the right place!
     * @throws IOException
     */
    AttributeTree(StateSystem ss, FileInputStream fis) throws IOException {
        this(ss);
        DataInputStream in = new DataInputStream(new BufferedInputStream(fis));

        /* Message for exceptions, shouldn't be externalized */
        final String errorMessage = "The attribute tree file section is either invalid or corrupted."; //$NON-NLS-1$

        ArrayList<String[]> list = new ArrayList<String[]>();
        byte[] curByteArray;
        String curFullString;
        String[] curStringArray;
        int res, remain, size;
        int expectedSize = 0;
        int total = 0;

        /* Read the header of the Attribute Tree file (or file section) */
        res = in.readInt(); /* Magic number */
        if (res != ATTRIB_TREE_MAGIC_NUMBER) {
            throw new IOException(errorMessage);
        }

        /* Expected size of the section */
        expectedSize = in.readInt();
        if (expectedSize <= 12) {
            throw new IOException(errorMessage);
        }

        /* How many entries we have to read */
        remain = in.readInt();
        total += 12;

        /* Read each entry */
        for (; remain > 0; remain--) {
            /* Read the first byte = the size of the entry */
            size = in.readByte();
            curByteArray = new byte[size];
            res = in.read(curByteArray);
            if (res != size) {
                throw new IOException(errorMessage);
            }

            /*
             * Go buffer -> byteArray -> String -> String[] -> insert in list.
             * bleh
             */
            curFullString = new String(curByteArray);
            curStringArray = curFullString.split("/"); //$NON-NLS-1$
            list.add(curStringArray);

            /* Read the 0'ed confirmation byte */
            res = in.readByte();
            if (res != 0) {
                throw new IOException(errorMessage);
            }
            total += curByteArray.length + 2;
        }

        if (total != expectedSize) {
            throw new IOException(errorMessage);
        }

        /*
         * Now we have 'list', the ArrayList of String arrays representing all
         * the attributes. Simply create attributes the normal way from them.
         */
        for (String[] attrib : list) {
            this.getQuarkAndAdd(-1, attrib);
        }
    }

    /**
     * Tell the Attribute Tree to write itself somewhere. The passed
     * FileOutputStream defines where (which file/position).
     *
     * @param fos
     *            Where to write. Make sure it's seeked at the right position
     *            you want.
     * @return The total number of bytes written.
     */
    int writeSelf(File file, long pos) {
        RandomAccessFile raf = null;
        int total = 0;
        byte[] curByteArray;

        try {
            raf = new RandomAccessFile(file, "rw"); //$NON-NLS-1$
            raf.seek(pos);

            /* Write the almost-magic number */
            raf.writeInt(ATTRIB_TREE_MAGIC_NUMBER);

            /* Placeholder for the total size of the section... */
            raf.writeInt(-8000);

            /* Write the number of entries */
            raf.writeInt(this.attributeList.size());
            total += 12;

            /* Write the attributes themselves */
            for (Attribute entry : this.attributeList) {
                curByteArray = entry.getFullAttributeName().getBytes();
                if (curByteArray.length > Byte.MAX_VALUE) {
                    throw new IOException("Attribute with name \"" //$NON-NLS-1$
                            + Arrays.toString(curByteArray) + "\" is too long."); //$NON-NLS-1$
                }
                /* Write the first byte = size of the array */
                raf.writeByte((byte) curByteArray.length);

                /* Write the array itself */
                raf.write(curByteArray);

                /* Write the 0'ed byte */
                raf.writeByte((byte) 0);

                total += curByteArray.length + 2;
            }

            /* Now go back and write the actual size of this section */
            raf.seek(pos + 4);
            raf.writeInt(total);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return total;
    }

    /**
     * Return the number of attributes this system as seen so far. Note that
     * this also equals the integer value (quark) the next added attribute will
     * have.
     *
     * @return
     */
    int getNbAttributes() {
        return attributeList.size();
    }

    /**
     * This is the version to specifically add missing attributes.
     *
     * If 'numericalNode' is true, all the new attributes created will be of
     * type 'NumericalNode' instead of 'AlphaNumNode'. Be careful with this, if
     * you do not want ALL added attributes to be numerical, call this function
     * first with 'false' to create the parent nodes, then call it again to make
     * sure only the final node is numerical.
     *
     * @throws AttributeNotFoundException
     */
    int getQuarkDontAdd(int startingNodeQuark, String... subPath)
            throws AttributeNotFoundException {
        assert (startingNodeQuark >= -1);

        Attribute prevNode;

        /* If subPath is empty, simply return the starting quark */
        if (subPath == null || subPath.length == 0) {
            return startingNodeQuark;
        }

        /* Get the "starting node" */
        if (startingNodeQuark == -1) {
            prevNode = attributeTreeRoot;
        } else {
            prevNode = attributeList.get(startingNodeQuark);
        }

        int knownQuark = prevNode.getSubAttributeQuark(subPath);
        if (knownQuark == -1) {
            /*
             * The attribute doesn't exist, but we have been specified to NOT
             * add any new attributes.
             */
            throw new AttributeNotFoundException();
        }
        /*
         * The attribute was already existing, return the quark of that
         * attribute
         */
        return knownQuark;
    }

    // FIXME synchronized here is probably quite costly... maybe only locking
    // the "for" would be enough?
    synchronized int getQuarkAndAdd(int startingNodeQuark, String... subPath) {
        assert (subPath != null && subPath.length > 0);
        assert (startingNodeQuark >= -1);

        Attribute nextNode = null;
        Attribute prevNode;

        /* Get the "starting node" */
        if (startingNodeQuark == -1) {
            prevNode = attributeTreeRoot;
        } else {
            prevNode = attributeList.get(startingNodeQuark);
        }

        int knownQuark = prevNode.getSubAttributeQuark(subPath);
        if (knownQuark == -1) {
            /*
             * The attribute was not in the table previously, and we want to add
             * it
             */
            for (String curDirectory : subPath) {
                nextNode = prevNode.getSubAttributeNode(curDirectory);
                if (nextNode == null) {
                    /* This is where we need to start adding */
                    nextNode = new AlphaNumAttribute(prevNode, curDirectory,
                            attributeList.size());
                    prevNode.addSubAttribute(nextNode);
                    attributeList.add(nextNode);
                    ss.addEmptyAttribute();
                }
                prevNode = nextNode;
            }
            return attributeList.size() - 1;
        }
        /*
         * The attribute was already existing, return the quark of that
         * attribute
         */
        return knownQuark;
    }

    int getSubAttributesCount(int quark) {
        return attributeList.get(quark).getSubAttributesList().size();
    }

    /**
     * Returns the sub-attributes of the quark passed in parameter
     *
     * @param attributeQuark
     * @param recursive
     * @return
     * @throws AttributeNotFoundException
     */
    List<Integer> getSubAttributes(int attributeQuark, boolean recursive)
            throws AttributeNotFoundException {
        List<Integer> listOfChildren = new ArrayList<Integer>();
        Attribute startingAttribute;

        /* Check if the quark is valid */
        if (attributeQuark < -1 || attributeQuark >= attributeList.size()) {
            throw new AttributeNotFoundException();
        }

        /* Set up the node from which we'll start the search */
        if (attributeQuark == -1) {
            startingAttribute = attributeTreeRoot;
        } else {
            startingAttribute = attributeList.get(attributeQuark);
        }

        /* Iterate through the sub-attributes and add them to the list */
        addSubAttributes(listOfChildren, startingAttribute, recursive);

        return listOfChildren;
    }

    private void addSubAttributes(List<Integer> list, Attribute curAttribute,
            boolean recursive) {
        for (Attribute childNode : curAttribute.getSubAttributesList()) {
            list.add(childNode.getQuark());
            if (recursive) {
                addSubAttributes(list, childNode, true);
            }
        }
    }

    String getAttributeName(int quark) {
        return attributeList.get(quark).getName();
    }

    String getFullAttributeName(int quark) {
        if (quark >= attributeList.size() || quark < 0) {
            return null;
        }
        return attributeList.get(quark).getFullAttributeName();
    }

    void debugPrint(PrintWriter writer) {
        attributeTreeRoot.debugPrint(writer);
    }

}