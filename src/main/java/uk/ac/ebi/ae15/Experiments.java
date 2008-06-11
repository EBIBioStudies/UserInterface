package uk.ac.ebi.ae15;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import org.apache.xml.serialize.*;

public class Experiments {

    // logging macinery
    private final Log log = LogFactory.getLog(getClass());

    // sql to get a list of experiments from the database
    private static String getExperimentsSql = "select distinct e.id, i.identifier as accession, case when v.user_id = 1 then 1 else 0 end as \"public\"" +
            " from tt_experiment e left outer join tt_identifiable i on i.id = e.id" +
            "  left outer join tt_extendable ext on ext.id = e.id" +
            "  left outer join pl_visibility v on v.label_id = ext.label_id" +
//          " where i.identifier like 'E-GEOD-20%'" +
            " order by" +
            "  i.identifier asc";

    private static int numOfParallelConnections = 25;
    private static int initialXmlStringBufferSize = 20000000;  // 20 Mb

    private final static String XML_DOCUMENT_VERSION = "1.0.080604.2";

    public Experiments()
    {
        experimentsXmlCacheLocation = getCachedXmlLocation();
    }

    public Document getExperiments()
    {
        if ( null == experimentsDoc ) {
            if ( !loadExperimentsFromCache() ) {
                log.warn("Experiments XML Cache was NOT loaded, possible fresh start up?");
                experimentsDoc = createExperimentsDocument();
            }
            //TODO: temp
            saveSpeciesAndArraysToCache();
            experimentSearch.buildText(experimentsDoc);

        }
        return experimentsDoc;
    }

    //TODO: refactor!
    public String getArrays()
    {
        if ( null == arraysString ) {
            arraysString = loadStringFromCache("ae-arrays.xml");
        }
        return arraysString;
    }

    private static String arraysString = null;

    public String getSpecies()
    {
        if ( null == speciesString ) {
            speciesString = loadStringFromCache("ae-species.xml");
        }
        return speciesString;
    }

    private static String speciesString = null;

    private String loadStringFromCache( String name )
    {
        String result = "";
        try {
            BufferedReader r = new BufferedReader( new InputStreamReader( new FileInputStream( System.getProperty("java.io.tmpdir") + File.separator + name )));
            result = r.readLine();
        } catch ( Throwable x ) {
            log.debug( "Caught an exception:", x );
            //TODO:error loggging
        }
        return result;
    }

    public ExperimentSearch Search()
    {
        return experimentSearch;
    }

    public void reloadExperiments( String dsName, boolean onlyPublic )
    {
        if ( loadExperimentsFromDataSource( dsName, onlyPublic ) ) {
            saveExperimentsToCache();
            saveSpeciesAndArraysToCache();
        } else {
            log.error("Unable to reload experiments from [" + dsName + "], check log for messages" );
            experimentsDoc = createExperimentsDocument();            
        }
        experimentSearch.buildText(experimentsDoc);
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
                    url = new File( tmpDir + File.separator + relCacheLocation ).toURL();
                } catch ( MalformedURLException x ) {
                    if (log.isDebugEnabled()) {
                        log.debug( "Caught an exception:", x );
                    } else {
                        log.error( "Caught an exception while attempting to form a URL for XML cache. tmpDir: " + tmpDir + ", relCacheLocation: " + relCacheLocation + ", message: " + x.getMessage() );
                    }
                }
            }
        }

        if ( null == url ) {
            log.error("Experiments XML cache location was not determined, expect problems down the road");
        }

        return url;
    }

    private void saveExperimentsToCache()
    {
        try {
            OutputFormat format = new OutputFormat();
            format.setLineSeparator(LineSeparator.Web);
            format.setIndenting(true);
            format.setLineWidth(0);
            format.setPreserveSpace(true);
            FileWriter fw = new FileWriter(experimentsXmlCacheLocation.getFile());
            XMLSerializer serializer = new XMLSerializer(fw, format);
            serializer.asDOMSerializer();
            serializer.serialize(experimentsDoc);
            fw.flush();
            fw.close();
        } catch ( Throwable x ) {
            log.debug( "Caught an exception:", x );
        }
    }

    private void saveSpeciesAndArraysToCache()
    {
        XsltHelper.transformDocumentToFile( experimentsDoc, "preprocess-species-html.xsl", null, new File(System.getProperty("java.io.tmpdir") + File.separator + "ae-species.xml") );
        XsltHelper.transformDocumentToFile( experimentsDoc, "preprocess-arrays-html.xsl", null, new File(System.getProperty("java.io.tmpdir") + File.separator + "ae-arrays.xml") );
    }

    private boolean loadExperimentsFromCache()
    {
        boolean isLoaded = false;

        if ( null != experimentsXmlCacheLocation ) {
            Document doc;

            try {
                //parse using builder to get DOM representation of the XML file
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                doc = docBuilder.parse( experimentsXmlCacheLocation.getFile() );

                if ( null != doc ) {
                    String docVer = doc.getDocumentElement().getAttribute("version");
                    if ( !XML_DOCUMENT_VERSION.equals(docVer) ) {
                        log.error( "Cache XML document version mismatch: loaded [" + docVer + "], expected [" + XML_DOCUMENT_VERSION + "]" );
                    } else {
                        log.info("Successfuly loaded experiments from XML cache, version " + docVer );
                        experimentsDoc = doc;
                        isLoaded = true;
                    }
                }
            } catch ( Throwable x ) {
                log.debug( "Caught an exception:", x );
            }
        }

        return isLoaded;
    }

    private boolean loadExperimentsFromString( String xmlString )
    {
        Document dom = XsltHelper.transformStringToDocument( xmlString, "preprocess-experiments-xml.xsl", null );
        if ( null == dom ) {
            log.error("Pre-processing returned an error, will have an empty document");
            return false;
        } else {
            experimentsDoc = dom;
            return true;
        }
    }


    public boolean loadExperimentsFromDataSource( String dataSourceName, boolean onlyPublic )
    {
        log.info("ArrayExpress Repository XML reload started.");

        boolean result = false;

        StringBuilder xmlBuf = new StringBuilder(initialXmlStringBufferSize);
        xmlBuf.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><experiments version=\"" + XML_DOCUMENT_VERSION + "\">");

        DataSource ds = getJdbcDataSource( dataSourceName );
        if ( null != ds ) {

            try {
                // get a list of experiments populated into expList
                List<ExperimentListEntry> expList = new ArrayList<ExperimentListEntry>();

                Connection conn = ds.getConnection();

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(getExperimentsSql);

                while ( rs.next() ) {
                    ExperimentListEntry entry = new ExperimentListEntry(
                            rs.getInt("id")
                            , rs.getString("accession")
                            , rs.getBoolean("public")
                    );
                    expList.add(entry);
                }

                rs.close();
                conn.close();

                //
                int poolSize = Math.min( expList.size(), numOfParallelConnections );

                // so we create a pool of threads :)
                List<ExperimentXmlRetrieverThread> threadPool = new ArrayList<ExperimentXmlRetrieverThread>();
                for ( int i = 0; i < poolSize; ++i ) {
                    ExperimentXmlRetrieverThread th = new ExperimentXmlRetrieverThread( ds, i );
                    th.start();
                    threadPool.add(th);
                }

                int expListIndex = 0;
                int threadPoolIndex = 0;
                int expCount = 0;

                result = true;

                while ( expListIndex < expList.size() && result ) {
                    if ( onlyPublic && !expList.get(expListIndex).isPublic ) {
                        // skipping this private experiment
                        expListIndex++;
                    } else {
                        ExperimentXmlRetrieverThread th = threadPool.get(threadPoolIndex);
                        if (th.isNew()) { // this thread is new - load it with work, fast!
                            th.setId(expList.get(expListIndex).id);
                            expListIndex++;
                        } else if (th.isFaulty()) { // this thread is faulty, discard it
                            threadPool.remove(threadPoolIndex);
                            th.retire();
                            result = false;
                        } else if (th.isDone()) { // this thread has done its job, reload it with more work :)
                            xmlBuf.append(th.getXml());
                            expCount++; // increment number of resulting experiments
                            th.setId(expList.get(expListIndex).id);
                            expListIndex++;
                        }
                        threadPoolIndex++;
                        if (threadPoolIndex >= threadPool.size())
                            threadPoolIndex = 0;

                        Thread.yield();
                    }
                }

                threadPoolIndex = 0;
                while ( threadPool.size() > 0 ) {
                    ExperimentXmlRetrieverThread th = threadPool.get(threadPoolIndex);
                    if (th.isDone()) {
                        xmlBuf.append(th.getXml());
                        expCount++; // increment number of resulting experiments
                        threadPool.remove(threadPoolIndex);
                        th.retire();
                    }
                    threadPoolIndex++;
                    if (threadPoolIndex >= threadPool.size())
                        threadPoolIndex = 0;

                    Thread.yield();
                }
                if ( !result ) {
                    log.warn("There are indications that at least one thread has failed getting experiments from the database, expect problems down the road");
                }
                log.info("ArrayExpress Repository XML reload completed (" + expCount + "/" + expList.size() + " experiments loaded).");

            } catch ( Throwable x ) {
                if ( log.isDebugEnabled() ) {
                    log.debug( "Caught an exception:", x );
                } else {
                    log.error( "loadExperimentsFromDataSource Exception: " + x.getMessage() );
                }
                result = false;
            }

        } else {
            log.error("Unable to obtain data source object, no experiments retrieved.");
        }

        xmlBuf.append("</experiments>");

        if ( result ) {
            String xml = xmlBuf.toString().replaceAll("[^\\p{Print}]"," ");    // replace is a nasty hack to get rid of non-printable characters altogether
            result = loadExperimentsFromString(xml);
        } else  {
            log.error("Experiments Document WAS NOT loaded from database, expect problems down the road");
        }
        
        return result;
    }

    private Document createExperimentsDocument()
    {
        Document doc = null;

            try {
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                 doc = docBuilder.newDocument();

                Element expElement = doc.createElement("experiments");
                expElement.setAttribute( "total", "0" );
                expElement.setAttribute( "version", XML_DOCUMENT_VERSION );

                doc.appendChild(expElement);

            } catch ( Throwable e ) {
                log.debug( "Caught an exception:", e );
            }

        if ( null == doc ) {
            log.error("Experiments Document WAS NOT created, expect problems down the road");
        }
        
        return doc;
    }

    private DataSource getJdbcDataSource( String dataSourceName )
    {
        DataSource dataSource = null;

        try {
            Context initContext = new InitialContext();
            Context envContext = (Context)initContext.lookup( "java:/comp/env" );

            dataSource = (DataSource)envContext.lookup( "jdbc/" + dataSourceName.toLowerCase() );
        } catch ( NamingException x ) {
            if (log.isDebugEnabled()) {
                log.debug( "Caught an exception:", x );
            } else {
                log.error( "getJdbcDataSource NamingException: " + x.getMessage() );
            }
        }

        return dataSource;
    }

    // url that represents the location of xml file (for persistence)
    private URL experimentsXmlCacheLocation = null;

    // the document that contains the basis for all experiments
    private Document experimentsDoc = null;

    private ExperimentSearch experimentSearch = new ExperimentSearch();
}
