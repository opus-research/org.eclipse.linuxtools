/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event.metadata;

/**
 * Strings generated from the TSDL grammar. Note that they are static final so
 * they get quarked. See CTF specs for more details
 *
 * @author Matthew Khouzam and All
 *
 */
@SuppressWarnings("nls")
public interface CTFStrings {

    /** None */
    public static final String NONE = "none";
    /** Ascii */
    public static final String ASCII = "ASCII";
    /** UTF8 */
    public static final String UTF8 = "UTF8";
    /** b (for binary like b11010010 */
    public static final String BIN = "b";
    /** Binary */
    public static final String BINARY = "binary";
    /** Octal like o177 */
    public static final String OCTAL_CTE = "o";
    /** Octal like oct177 */
    public static final String OCT = "oct";
    /** Octal like octal177 */
    public static final String OCTAL = "octal";
    /** Pointer (memory address for all the hardcore Java gurus out there)*/
    public static final String POINTER = "p";
    /** X for hex */
    public static final String X2 = "X";
    /** x for hex */
    public static final String X = "x";
    /** hex */
    public static final String HEX = "hex";
    /** Hexadecimal */
    public static final String HEXADECIMAL = "hexadecimal";
    /** unsigned like in 10000ul */
    public static final String UNSIGNED_CTE = "u";
    /** Decimal */
    public static final String DEC_CTE = "d";
    /** Integer like 1000i */
    public static final String INT_MOD = "i";
    /** Decimal */
    public static final String DEC = "dec";
    /** Decimal */
    public static final String DECIMAL = "decimal";
    /** native for byteorders*/
    public static final String NATIVE = "native";
    /** network for byteorders*/
    public static final String NETWORK = "network";
    /** Big endian */
    public static final String BE = "be";
    /** Little endian */
    public static final String LE = "le";
    /** Alignment of a field */
    public static final String ALIGN = "align";
    /** Mantissa digits */
    public static final String MANT_DIG = "mant_dig";
    /** Exponent digits */
    public static final String EXP_DIG = "exp_dig";
    /** Loglevel */
    public static final String LOGLEVEL2 = "loglevel";
    /** Name */
    public static final String NAME2 = "name";
    /** Event context */
    public static final String EVENT_CONTEXT = "event.context";
    /** Fields */
    public static final String FIELDS_STRING = "fields";
    /** context */
    public static final String CONTEXT = "context";
    /** Stream ID */
    public static final String STREAM_ID = "stream_id";
    /** Packet context */
    public static final String PACKET_CONTEXT = "packet.context";
    /** ID */
    public static final String ID = "id";
    /** Packet Header */
    public static final String PACKET_HEADER = "packet.header";
    /** Event Header */
    public static final String EVENT_HEADER = "event.header";
    /** Byte order */
    public static final String BYTE_ORDER = "byte_order";
    /** UUID */
    public static final String UUID_STRING = "uuid";
    /** False */
    public static final String FALSE2 = "FALSE";
    /** False */
    public static final String FALSE = "false";
    /** True */
    public static final String TRUE2 = "TRUE";
    /** True */
    public static final String TRUE = "true";
    /** Minor (Version)*/
    public static final String MINOR = "minor";
    /** Major (Version)*/
    public static final String MAJOR = "major";
    /** EMF URI */
    public static final String MODEL_EMF_URI = "model.emf.uri";

}
