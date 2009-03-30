package uk.ac.ebi.arrayexpress.components;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.core.CoreContainer;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.model.ExperimentBean;

import java.io.File;
import java.util.List;

public class SearchEngine extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CoreContainer cores;
    private SolrServer server;

    public SearchEngine()
    {
        super("SearchEngine");
    }

    public void initialize()
    {
        try {
            String tmpDir = System.getProperty("java.io.tmpdir");
            File configFile = new File(System.getProperty("java.io.tmpdir"), "ae-solr-index/ae-solr-index.xml");
            cores =
                new CoreContainer(
                    new File(
                        tmpDir
                        , getPreferences().getString("ae.solr.index.directory")).getAbsolutePath()
                    , configFile
                );
            server = new EmbeddedSolrServer(cores, "experiments");

        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
    }

    public void terminate()
    {
        cores.shutdown();
    }

    public void addToIndex(ExperimentBean experiment)
    {
        try {
            server.addBean(experiment);
            // not sure if we need it here?
            server.commit();
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
    }

    public void queryIndex(String queryString)
    {
        SolrQuery query = new SolrQuery();
        query.setQuery(queryString);
        query.addHighlightField("name");

        try {
            QueryResponse rsp = server.query(query);
            List<ExperimentBean> experiments = rsp.getBeans(ExperimentBean.class);

            for (ExperimentBean experiment : experiments) {
                logger.info(experiment.getAccession());
            }

        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
    }
}
