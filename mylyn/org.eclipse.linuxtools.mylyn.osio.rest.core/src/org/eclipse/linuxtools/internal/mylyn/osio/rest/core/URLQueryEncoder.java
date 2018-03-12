package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

public class URLQueryEncoder {

	public final static String hexchar = "0123456789ABCDEF"; //$NON-NLS-1$
	
    public static String transform(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isSpecial(ch)) {
                resultStr.append('%'); //$NON-NLS-1$
                resultStr.append(hexchar.charAt((ch & 0xff) >> 4));
                resultStr.append(hexchar.charAt(ch & 0xF));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static boolean isSpecial(char ch) {
        if (ch > 128 || ch < 0)
            return true;
        return " $&+/;?@<>#%[]{}:\"".indexOf(ch) >= 0; //$NON-NLS-1$
    }

}


