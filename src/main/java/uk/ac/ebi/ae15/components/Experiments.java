package uk.ac.ebi.ae15.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.app.ApplicationComponent;
import uk.ac.ebi.ae15.utils.persistence.PersistableDocumentContainer;
import uk.ac.ebi.ae15.utils.persistence.PersistableString;
import uk.ac.ebi.ae15.utils.persistence.TextFilePersistence;
import uk.ac.ebi.ae15.utils.search.ExperimentSearch;

import java.io.File;

public class Experiments extends ApplicationComponent
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private TextFilePersistence<PersistableDocumentContainer> experiments;
    private TextFilePersistence<PersistableString> species;
    private TextFilePersistence<PersistableString> arrays;

    private ExperimentSearch experimentSearch;
    private String dataSource;

    public Experiments( Application app )
    {
        super(app, "Experiments");

        experiments = new TextFilePersistence<PersistableDocumentContainer>(
                new PersistableDocumentContainer()
                , new File(System.getProperty("java.io.tmpdir"), getPreferences().get("ae.experiments.cache.filename"))
        );

        experimentSearch = new ExperimentSearch();

        species = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(System.getProperty("java.io.tmpdir"), getPreferences().get("ae.species.cache.filename"))

        );

        arrays = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(System.getProperty("java.io.tmpdir"), getPreferences().get("ae.arrays.cache.filename"))
        );
    }

    public void initialize()
    {
    }

    public void terminate()
    {
    }

    public Document getExperiments()
    {
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

    public ExperimentSearch getSearch()
    {
        return experimentSearch;
    }

    public String getDataSource()
    {
        if (null == dataSource) {
            dataSource = getPreferences().get("ae.experiments.datasource.default");
        }

        return dataSource;
    }

    public void setDataSource( String ds )
    {
        dataSource = ds;
    }

    public void reload() throws InterruptedException
    {
        log.info("Rescan of downloadable files from [" + getDataSource() + "] requested");
    }
/*********
 public void reloadExperiments(String dsName, boolean onlyPublic)
 {
 experiments.setObject(
 new PersistableDocumentContainer(
 loadExperimentsFromDataSource(dsName, onlyPublic)
 )
 );

 species.setObject(
 new PersistableString(
 ((XsltHelper)getApplication().getComponent("XsltHelper")).transformDocumentToString(
 experiments.getObject().getDocument()
 , "preprocess-species-html.xsl"
 , null
 )
 )
 );

 arrays.setObject(
 new PersistableString(
 ((XsltHelper)getApplication().getComponent("XsltHelper")).transformDocumentToString(
 experiments.getObject().getDocument()
 , "preprocess-arrays-html.xsl"
 , null
 )
 )
 );

 experimentSearch.buildText(experiments.getObject().getDocument());
 }

 private Document loadExperimentsFromString(String xmlString)
 {
 Document doc = ((XsltHelper)getComponent("XsltHelper")).transformStringToDocument(xmlString, "preprocess-experiments-xml.xsl", null);
 if (null == doc) {
 log.error("Pre-processing returned an error, will have an empty document");
 return null;
 }
 return doc;
 }

 public Document loadExperimentsFromDataSource(String dataSourceName, boolean onlyPublic)
 {
 log.info("ArrayExpress Repository XML reload started.");

 Document doc = null;

 boolean isLoaded = false;

 StringBuilder xmlBuf = new StringBuilder(initialXmlStringBufferSize);
 xmlBuf.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><experiments>");

 DataSource ds = getJdbcDataSource(dataSourceName);
 if (null != ds) {

 try {
 // get a list of experiments populated into expList
 List<ExperimentListEntry> expList = new ArrayList<ExperimentListEntry>();

 Connection conn = ds.getConnection();

 Statement stmt = conn.createStatement();
 ResultSet rs = stmt.executeQuery(getExperimentsSql);

 while (rs.next()) {
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
 int poolSize = Math.min(expList.size(), numOfParallelConnections);

 // so we create a pool of threads :)
 List<ExperimentXmlRetrieverThread> threadPool = new ArrayList<ExperimentXmlRetrieverThread>();
 for (int i = 0; i < poolSize; ++i) {
 ExperimentXmlRetrieverThread th = new ExperimentXmlRetrieverThread(ds, i);
 th.start();
 threadPool.add(th);
 }

 int expListIndex = 0;
 int threadPoolIndex = 0;
 int expCount = 0;

 isLoaded = true;

 while (expListIndex < expList.size() && isLoaded) {
 if (onlyPublic && !expList.get(expListIndex).isPublic) {
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
 while (threadPool.size() > 0) {
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
 if (!isLoaded) {
 log.warn("There are indications that at least one thread has failed getting experiments from the database, expect problems down the road");
 }
 log.info("ArrayExpress Repository XML reload completed (" + expCount + "/" + expList.size() + " experiments loaded).");

 } catch (Throwable x) {
 log.error("Caught an exception:", x);
 isLoaded = false;
 }

 } else {
 log.error("Unable to obtain data source object, no experiments retrieved.");
 }

 xmlBuf.append("</experiments>");

 if (isLoaded) {
 String xml = xmlBuf.toString().replaceAll("[^\\p{Print}]", " ");    // replace is a nasty hack to get rid of non-printable characters altogether
 doc = loadExperimentsFromString(xml);
 } else {
 log.error("Experiments Document WAS NOT loaded from database, expect problems down the road");
 }

 return doc;
 }

 private DataSource getJdbcDataSource(String dataSourceName)
 {
 DataSource dataSource = null;

 try {
 Context initContext = new InitialContext();
 Context envContext = (Context) initContext.lookup("java:/comp/env");

 dataSource = (DataSource) envContext.lookup("jdbc/" + dataSourceName.toLowerCase());
 } catch (Throwable x) {
 log.error("Caught an exception:", x);
 }

 return dataSource;
 }
 ****/
}
