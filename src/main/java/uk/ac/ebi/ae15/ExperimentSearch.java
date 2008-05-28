package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;
import java.util.ArrayList;

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
        return ( -1 != expText.get(idx).species.indexOf( species.trim() ) );
    }

    public boolean matchArray( String textIdx, String array )
    {
        int idx = Integer.parseInt(textIdx);
        return ( -1 != expText.get(idx).array.indexOf( array.trim() ) );
    }

}
