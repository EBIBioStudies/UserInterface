package uk.ac.ebi.ae15;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.apache.commons.logging.Log;

/**
 *  A transitional class holding text that will be matched when searching for experiments.
 *  To be replaced by Lucene engine in 1.2.
 */

public class ExperimentText {

    // logging facility
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(ExperimentText.class);

    // all text, concatenated
    public String text;

    // all species, concatenated
    public String species;

    // all array info, concatenated
    public String array;


    public static ExperimentText newTextFromDomElement( Element elt )
    {
        ExperimentText expText = new ExperimentText();

        expText.text = concatAll(elt);
        expText.species = concatAll(elt.getElementsByTagName("species"));
        expText.array = concatAll(elt.getElementsByTagName("arraydesign"));

        return expText;
    }

    private static String concatAll( Element elt )
    {
        if ( elt.hasChildNodes() ) {
            return concatAll(elt.getChildNodes());
        } else {
            return "";
        }

    }

    private static String concatAll( NodeList nl )
    {
		StringBuilder buf = new StringBuilder();

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

        return buf.toString().trim();
	}

}
