package uk.ac.ebi.ae15;

import org.w3c.dom.Document;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class Experiments {

    public Experiments()
    {
        experiments = new TextFilePersistence<PersistableDocumentContainer>(
                new PersistableDocumentContainer()
                , new File( System.getProperty("java.io.tmpdir") + File.separator + "ae-experiments.xml" )
        );

        experimentSearch = new ExperimentSearch();
        
        species = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File( System.getProperty("java.io.tmpdir") + File.separator + "ae-species.xml" )

        );

        arrays = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File( System.getProperty("java.io.tmpdir") + File.separator + "ae-arrays.xml" )
        );
    }

    public Document getExperiments() {
        Document doc = experiments.getObject().getDocument();

        if (experimentSearch.isEmpty()) {
            experimentSearch.buildText(doc);
        }

        return doc;
    }

    public String getSpecies()
    {
        return species.getObject().get();    
    }

    public String getArrays()
    {
        return arrays.getObject().get();
    }

    public ExperimentSearch Search()
    {
        return experimentSearch;
    }

    public void reloadExperiments( String dsName, boolean onlyPublic )
    {
        experiments.setObject(
                new PersistableDocumentContainer(
                        loadExperimentsFromDataSource( dsName, onlyPublic )
                )
        );

        species.setObject(
                new PersistableString(
                        XsltHelper.transformDocumentToString(
                                experiments.getObject().getDocument()
                                , "preprocess-species-html.xsl"
                                , null
                        )
                )
        );

        arrays.setObject(
                new PersistableString(
                        XsltHelper.transformDocumentToString(
                                experiments.getObject().getDocument()
                                , "preprocess-arrays-html.xsl"
                                , null
                        )
                )
        );

        experimentSearch.buildText(experiments.getObject().getDocument());
    }

    private Document loadExperimentsFromString( String xmlString )
    {
        Document doc = XsltHelper.transformStringToDocument( xmlString, "preprocess-experiments-xml.xsl", null );
        if ( null == doc ) {
            log.error("Pre-processing returned an error, will have an empty document");
            return null;
        }
        return doc;
    }


    public Document loadExperimentsFromDataSource( String dataSourceName, boolean onlyPublic )
    {
        log.info("ArrayExpress Repository XML reload started.");

        Document doc = null;

        boolean isLoaded = false;

        StringBuilder xmlBuf = new StringBuilder(initialXmlStringBufferSize);
        xmlBuf.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><experiments>");

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

                isLoaded = true;

                while ( expListIndex < expList.size() && isLoaded ) {
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
                            isLoaded = false;
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
                if ( !isLoaded ) {
                    log.warn("There are indications that at least one thread has failed getting experiments from the database, expect problems down the road");
                }
                log.info("ArrayExpress Repository XML reload completed (" + expCount + "/" + expList.size() + " experiments loaded).");

            } catch ( Throwable x ) {
                log.error( "Caught an exception:", x );
                isLoaded = false;
            }

        } else {
            log.error("Unable to obtain data source object, no experiments retrieved.");
        }

        xmlBuf.append("</experiments>");

        if ( isLoaded ) {
            String xml = xmlBuf.toString().replaceAll("[^\\p{Print}]"," ");    // replace is a nasty hack to get rid of non-printable characters altogether
            doc = loadExperimentsFromString(xml);
        } else  {
            log.error("Experiments Document WAS NOT loaded from database, expect problems down the road");
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

    private TextFilePersistence<PersistableDocumentContainer> experiments;

    private TextFilePersistence<PersistableString> species;
    private TextFilePersistence<PersistableString> arrays;

    private ExperimentSearch experimentSearch;

    // sql to get a list of experiments from the database
    private static String getExperimentsSql = "select distinct e.id, i.identifier as accession, case when v.user_id = 1 then 1 else 0 end as \"public\"" +
            " from tt_experiment e left outer join tt_identifiable i on i.id = e.id" +
            "  left outer join tt_extendable ext on ext.id = e.id" +
            "  left outer join pl_visibility v on v.label_id = ext.label_id" +
//          " where i.identifier like 'E-GEOD-20%'" +
            " order by" +
            "  i.identifier asc";

    private final static int numOfParallelConnections = 25;
    private final static int initialXmlStringBufferSize = 20000000;  // 20 Mb

    // logging macinery
    private final Log log = LogFactory.getLog(getClass());
}
