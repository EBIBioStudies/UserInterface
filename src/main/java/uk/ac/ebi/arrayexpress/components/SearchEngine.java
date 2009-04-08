package uk.ac.ebi.arrayexpress.components;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.model.ExperimentBean;


public class SearchEngine extends ApplicationComponent
{
    // logging machinery
//    private final Logger logger = LoggerFactory.getLogger(getClass());

//    private CoreContainer cores;
//    private SolrServer server;

    public SearchEngine()
    {
        super("SearchEngine");
    }

    public void initialize()
    {
//        try {
//            String tmpDir = System.getProperty("java.io.tmpdir");
//            File configFile = new File(System.getProperty("java.io.tmpdir"), "ae-solr-index/ae-solr-index.xml");
//            cores =
//                new CoreContainer(
//                    new File(
//                        tmpDir
//                        , getPreferences().getString("ae.solr.index.directory")).getAbsolutePath()
//                    , configFile
//                );
//            server = new EmbeddedSolrServer(cores, "experiments");
//
//        } catch (Throwable x) {
//            logger.error("Caught an exception:", x);
//        }
    }

    public void terminate()
    {
//        if (null != cores) {
//            cores.shutdown();
//        }
    }

    public void addToIndex(ExperimentBean experiment)
    {
//        try {
//            server.addBean(experiment);
//            // not sure if we need it here?
//            server.commit();
//        } catch (Throwable x) {
//            logger.error("Caught an exception:", x);
//        }
    }

    public void queryIndex(String queryString)
    {
//        SolrQuery query = new SolrQuery();
//        query.setQuery(queryString);
//        query.addHighlightField("name");
//
//        try {
//            QueryResponse rsp = server.query(query);
//            List<ExperimentBean> experiments = rsp.getBeans(ExperimentBean.class);
//
//            for (ExperimentBean experiment : experiments) {
//                logger.info(experiment.getAccession());
//            }
//
//        } catch (Throwable x) {
//            logger.error("Caught an exception:", x);
//        }
    }
}
