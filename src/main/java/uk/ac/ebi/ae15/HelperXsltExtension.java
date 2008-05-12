package uk.ac.ebi.ae15;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.apache.commons.logging.Log;
import org.apache.xalan.extensions.ExpressionContext;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;

import javax.xml.transform.TransformerException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class HelperXsltExtension {

    private static final Log log = org.apache.commons.logging.LogFactory.getLog(HelperXsltExtension.class);

    public static String concatAll( NodeList nl )
    {
		StringBuffer buf = new StringBuffer();

        try {
            for (int i = 0; i < nl.getLength(); i++) {
                Node elt = nl.item(i);

                if ( null != elt.getNodeValue() )
                    buf.append( elt.getNodeValue() ).append(' ');

                if ( elt.hasAttributes() ) {
                    NamedNodeMap attrs = elt.getAttributes();
                    for ( int j = 0; j < attrs.getLength(); j++) {
                        buf.append( attrs.item(j).getNodeValue() ).append(' ');
                    }
                }

                if ( elt.hasChildNodes() )
                    buf.append( concatAll( elt.getChildNodes() )).append(' ');
            }
        } catch ( Throwable t ) {
            log.debug("Caught an exception:", t);
        }

        return buf.toString();
	}

    public static String toUpperCase( String str )
    {
        return str.toUpperCase();
    }
    public static boolean testRegexp( NodeList nl, String pattern, String flags )
    {
        return testRegexp( concatAll(nl), pattern, flags );
    }

    public static boolean testRegexp( String input, String pattern, String flags )
    {
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

    public static boolean testKeywords( NodeList nl, String keywords, boolean wholeWords )
    {
        return testKeywords( concatAll(nl), keywords, wholeWords );
    }

    public static boolean testKeywords( String input, String keywords, boolean wholeWords )
    {
        String[] kwdArray = keywords.split("\\s");
        for ( String keyword : kwdArray ) {
            String pattern = ( wholeWords ? "\\b" + keyword + "\\b" : keyword );
            if ( testRegexp( input, pattern, "i" ) )
                return true;
        }

        return false;
    }

    public static void logInfo( XSLProcessorContext c, ElemExtensionCall extElt )
    {
        try {
            log.info(extElt.getAttribute("select", c.getContextNode(), c.getTransformer()));
        } catch (TransformerException e) {
            log.debug( "Caught an exception:", e );
        }
    }

    public static void logDebug( XSLProcessorContext c, ElemExtensionCall extElt )
    {
        try {
            log.debug(extElt.getAttribute("select", c.getContextNode(), c.getTransformer()));
        } catch (TransformerException e) {
            log.debug( "Caught an exception:", e );
        }
    }
}