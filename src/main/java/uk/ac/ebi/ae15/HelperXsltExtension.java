package uk.ac.ebi.ae15;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.apache.commons.logging.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class HelperXsltExtension {

    private static final Log log = org.apache.commons.logging.LogFactory.getLog(HelperXsltExtension.class);

    public static String concatAll( NodeList nl ) {
		StringBuffer buf = new StringBuffer();

        try {
            for (int i = 0; i < nl.getLength(); i++) {
                Node elt = nl.item(i);

                if ( elt.hasChildNodes() )
                    buf.append( concatAll( elt.getChildNodes() )).append(' ');

                if ( elt.hasAttributes() ) {
                    NamedNodeMap attrs = elt.getAttributes();
                    for ( int j = 0; j < attrs.getLength(); j++) {
                        buf.append( attrs.item(j).getNodeValue() ).append(' ');
                    }
                }

                if ( null != elt.getNodeValue() ) {
                    buf.append( elt.getNodeValue() ).append(' ');
                }
            }
        } catch ( Throwable t ) {
            log.debug("Caught an exception:", t);
        }

        return buf.toString();
	}

    public static boolean testRegexp( NodeList nl, String pattern, String flags ) {
        return testRegexp( concatAll(nl), pattern, flags );
    }

    public static boolean testRegexp( String input, String pattern, String flags ) {
        boolean result = false;
        try {
            int patternFlags = ( flags.indexOf("i") >= 0 ? Pattern.CASE_INSENSITIVE : 0 );

            String inputStr = ( input == null ? "" : input );
            String patternStr = ( pattern == null ? "" : pattern );

            Pattern p = Pattern.compile(patternStr, patternFlags);
            Matcher matcher = p.matcher(inputStr);
            result = matcher.find();
        } catch ( Throwable t ) {
            log.debug("Caught an exception:", t);
        }

        return result;
    }

    public static boolean testKeywords( NodeList nl, String keywords, boolean wholeWords ) {
        return testKeywords( concatAll(nl), keywords, wholeWords );
    }

    public static boolean testKeywords( String input, String keywords, boolean wholeWords ) {
        String[] kwdArray = keywords.split("\\s");
        String li = input.toLowerCase();
        int index = li.indexOf(keywords.toLowerCase());
        for ( int i = 0; i < kwdArray.length; i++ ) {
            String pattern = ( wholeWords ? "\\b" + kwdArray[i] + "\\b" : kwdArray[i] );
            if ( testRegexp( input, pattern, "i" ) ) {
                if ( -1 == index ) {
                    log.debug("false positive!!!");
                }
                return true;
            }
        }
        if ( -1 != index ) {
            log.debug("oops, missed something!!!");
        }
        return false;
    }
}
