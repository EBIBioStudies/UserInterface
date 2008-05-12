package uk.ac.ebi.ae15;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.logging.Log;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.FactoryConfigurationError;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class Experiments {

    // logging macinery
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(Experiments.class);

    public Experiments()
    {
        experimentsXmlCacheLocation = getCachedXmlLocation();
    }

    public Document getExperiments()
    {
        if ( null == experimentsDoc ) {
            if ( !loadExperimentsFromCache() ) {
                log.warn("Experiments XML Cache was NOT loaded, fresh start up possible");
                experimentsDoc = createExperimentsDocument();
            }

        }
        return experimentsDoc;
    }

    //  returns the location of xml cache ({java.io.tmpdir}/{ae.experiments.cache.location})
    private URL getCachedXmlLocation()
    {
        URL url = null;

        String tmpDir = System.getProperty("java.io.tmpdir");

        if ( null != tmpDir ) {
            String relCacheLocation = Application.Preferences().getProperty("ae.experiments.cache.location");
            if ( null != relCacheLocation ) {
                try {
                    url = new File( tmpDir + "/" + relCacheLocation ).toURL();
                } catch ( MalformedURLException e ) {
                    log.error( "Caught an exception while attempting to form a URL for XML cache. tmpDir: " + tmpDir + ", relCacheLocation: " + relCacheLocation + ", message: " + e.getMessage() );
                }
            }
        }

        if ( null == url ) {
            log.error("Experiments XML cache location was not determined, expect problems down the road");
        }

        return url;
    }

    private boolean loadExperimentsFromCache()
    {
        boolean isLoaded = false;

        if ( null != experimentsXmlCacheLocation ) {
            Document doc = null;
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                //Using factory get an instance of document builder
                DocumentBuilder db = dbf.newDocumentBuilder();

                //parse using builder to get DOM representation of the XML file
                doc = db.parse(experimentsXmlCacheLocation.toURI().toString());

                if ( null != doc ) {
                    experimentsDoc = doc;
                    isLoaded = true;
                }
            } catch ( Throwable e ) {
                log.debug( "Caught an exception:", e );
            }
        }

        return isLoaded;
    }


    private Document createExperimentsDocument()
    {
        Document doc = null;

            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                //Using factory get an instance of document builder
                DocumentBuilder db = dbf.newDocumentBuilder();

                doc = db.newDocument();

                Element expElement = doc.createElement("experiments");
                expElement.setAttribute( "total", "0" );

                doc.appendChild(expElement);

            } catch ( Throwable e ) {
                log.debug( "Caught an exception:", e );
            }

        if ( null == doc ) {
            log.error("Experiments Document WAS NOT created, expect problems down the road");
        }
        
        return doc;
    }
    
    // url that represents the location of xml file (for persistence)
    private URL experimentsXmlCacheLocation = null;

    //
    private Document experimentsDoc = null;
}
