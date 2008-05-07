package uk.ac.ebi.ae15;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.xml.sax.SAXException;
/*

 Mamonoff is a MicroArray Monitor ON-OFF, and, in fact, a very simple class
 that catches webapp startup and shutdown events and then kicks a configured
 shell script per such event

 */
public class Mamonoff implements ServletContextListener {

private static final Log log = org.apache.commons.logging.LogFactory.getLog(Mamonoff.class);

    public synchronized void contextInitialized( ServletContextEvent sce )
    {
        log.info("context initialized");
        // this is just a sandbox for all these fancy technologies

        Document dom = loadAndParseXml();
        List<Document> domList = new ArrayList<Document>();
        for ( int i = 1; i <= 10; i++ ) {
            domList.add((Document)dom.cloneNode(true));
            log.info("loaded document #" + i);
        }
    }


    public synchronized void contextDestroyed( ServletContextEvent sce )
    {
        log.info("context destroyed");
    }



    private Document loadAndParseXml()
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document dom = null;
		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			dom = db.parse("/Volumes/Workspace/Users/kolais/Projects/AE/ae-experiments.xml");


		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
        
        return dom;
    }







}
