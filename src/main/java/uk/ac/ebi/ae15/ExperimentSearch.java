package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *  Search experiments based on full-text information stored in ExperimentText class
 */
public class ExperimentSearch {

    // logging facility
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(ExperimentSearch.class);

    // array of ExperimentText objects in the same order they exist in the document
    private List<ExperimentText> expText;
    
    public void buildText( Document experiments )
    {
        if ( null != experiments ) {
            try {
                if ( experiments.hasChildNodes() && experiments.getDocumentElement().hasChildNodes() ) {
                    NodeList expList = experiments.getDocumentElement().getChildNodes();

                    expText = new ArrayList<ExperimentText>(expList.getLength());

                    for ( int i = 0; i < expList.getLength(); ++i ) {
                        Element expElt = (Element)expList.item(i);
                        expText.add( ExperimentText.newTextFromDomElement( expElt ) );
                        expElt.setAttribute( "textIdx", Integer.toString(i) );
                    }

                }
            } catch ( Exception x ) {
                log.debug( "Caught an exception:", x );
            }
        }
    }

    public boolean matchText( String textIdx, String keywords, boolean wholeWords )
    {
        int idx = Integer.parseInt(textIdx);
        return matchString( expText.get(idx).text, keywords, wholeWords );
    }

    public boolean matchSpecies( String textIdx, String species )
    {
        int idx = Integer.parseInt(textIdx);
        return ( -1 != expText.get(idx).species.indexOf( species.trim().toLowerCase() ) );
    }

    public boolean matchArray( String textIdx, String array )
    {
        int idx = Integer.parseInt(textIdx);
        return ( -1 != expText.get(idx).array.indexOf( array.trim().toLowerCase() ) );
    }

    private boolean matchRegexp( String input, String pattern, String flags )
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

    private static String keywordToPattern( String keyword, boolean wholeWord ) {
        return ( wholeWord ? "\\b\\Q" + keyword + "\\E\\b" : "\\Q" + keyword + "\\E" );
    }

    private boolean matchString( String input, String keywords, boolean wholeWords )
    {
        // trim spaces on both sides
        keywords = keywords.trim();

        // by default (i.e. no keywords) it'll always match
        // otherwise any keyword fails -> no match :)
        if ( 0 < keywords.length() ) {
            String[] kwdArray = keywords.split("\\s");
            for ( String keyword : kwdArray ) {
                if ( !matchRegexp( input, keywordToPattern( keyword, wholeWords ), "i" ) )
                    return false;
            }
        }
        return true;
    }
}
