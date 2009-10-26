package uk.ac.ebi.arrayexpress.components;

import net.sf.saxon.om.DocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableExperimentsContainer;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableString;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableStringList;
import uk.ac.ebi.arrayexpress.utils.persistence.TextFilePersistence;
import uk.ac.ebi.arrayexpress.utils.saxon.search.Controller;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Experiments extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String dataSource;
    private TextFilePersistence<PersistableExperimentsContainer> experiments;
    private TextFilePersistence<PersistableStringList> experimentsInWarehouse;
    private TextFilePersistence<PersistableString> species;
    private TextFilePersistence<PersistableString> arrays;
    private TextFilePersistence<PersistableString> experimentTypes;

    private Controller indexController;

    public Experiments()
    {
        super("Experiments");
    }

    public void initialize()
    {
        String tmpDir = System.getProperty("java.io.tmpdir");
        experiments = new TextFilePersistence<PersistableExperimentsContainer>(
                new PersistableExperimentsContainer()
                , new File(tmpDir, getPreferences().getString("ae.experiments.cache.filename"))
        );

        experimentsInWarehouse = new TextFilePersistence<PersistableStringList>(
                new PersistableStringList()
                , new File(tmpDir, getPreferences().getString("ae.warehouseexperiments.cache.filename"))
        );

        species = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(tmpDir, getPreferences().getString("ae.species.cache.filename"))

        );

        arrays = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(tmpDir, getPreferences().getString("ae.arrays.cache.filename"))
        );

        experimentTypes = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(tmpDir, getPreferences().getString("ae.exptypes.cache.filename"))
        );

        try {
            indexController = Controller.getController(getApplication().getResource("/WEB-INF/classes/aeindex.xml"));
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }

        indexExperiments();
    }

    public void terminate()
    {
    }

    public synchronized DocumentInfo getExperiments()
    {
        return experiments.getObject().getDocument();
    }

    public Integer addQuery( Map<String,String> params)
    {
        return indexController.addQuery("experiments", params);   
    }

    public boolean isAccessible( String accession, String userId )
    {
        return false;
    }

    public boolean isInWarehouse( String accession )
    {
        return experimentsInWarehouse.getObject().contains(accession);
    }

    public String getSpecies()
    {
        return species.getObject().get();
    }

    public String getArrays()
    {
        return arrays.getObject().get();
    }

    public String getExperimentTypes()
    {
        return experimentTypes.getObject().get();
    }


    public String getDataSource()
    {
        if (null == dataSource) {
            dataSource = getPreferences().getString("ae.experiments.datasources");
        }

        return dataSource;
    }

    public void setDataSource( String ds )
    {
        dataSource = ds;
    }

    public void reload( String xmlString )
    {
        DocumentInfo doc = loadExperimentsFromString(xmlString);
        if (null != doc) {
            setExperiments(doc);
            
            indexExperiments();
        }
    }

    public void updateFiles()
    {
    }

    public void setExperimentsInWarehouse( List<String> expList )
    {
    }

    private synchronized void setExperiments( DocumentInfo doc )
    {
        if (null != doc ) {
            experiments.setObject(new PersistableExperimentsContainer(doc));
        } else {
            logger.error("Experiments NOT updated, NULL document passed");
        }
    }

    private DocumentInfo loadExperimentsFromString( String xmlString )
    {
        DocumentInfo doc = ((SaxonEngine) getComponent("SaxonEngine")).transform(xmlString, "preprocess-experiments-xml.xsl", null);
        if (null == doc) {
            logger.error("Transformation [preprocess-experiments-xml.xsl] returned an error, returning null");
            return null;
        }
        return doc;
    }

    private void indexExperiments()
    {
        try {
            indexController.index("experiments", experiments.getObject().getDocument());
            List<String> expDesign = indexController.getTerms("experiments", "expdesign");
            logger.debug("Retrieved experiment design list, size [{}]", expDesign.size());
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
    }
}
