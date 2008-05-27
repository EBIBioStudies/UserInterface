package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

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
                        expText.add( ExperimentText.newTextFromDomElement( (Element)expList.item(i) ) );
                    }

                }
            } catch ( Exception x ) {
                log.debug( "Caught an exception:", x );
            }
        }
    }

}
