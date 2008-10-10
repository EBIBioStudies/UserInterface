package uk.ac.ebi.ae15.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.app.ApplicationComponent;
import uk.ac.ebi.ae15.utils.persistence.PersistableDocumentContainer;
import uk.ac.ebi.ae15.utils.persistence.PersistableString;
import uk.ac.ebi.ae15.utils.persistence.PersistableStringList;
import uk.ac.ebi.ae15.utils.persistence.TextFilePersistence;
import uk.ac.ebi.ae15.utils.search.ExperimentSearch;

import java.io.File;
import java.util.List;

public class Experiments extends ApplicationComponent
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private TextFilePersistence<PersistableDocumentContainer> experiments;
    private TextFilePersistence<PersistableStringList> experimentsInWarehouse;
    private TextFilePersistence<PersistableString> species;
    private TextFilePersistence<PersistableString> arrays;
    private TextFilePersistence<PersistableString> experimentTypes;

    private ExperimentSearch experimentSearch;
    private String dataSource;

    public Experiments( Application app )
    {
        super(app, "Experiments");
    }

    public void initialize()
    {
        String tmpDir = System.getProperty("java.io.tmpdir");
        experiments = new TextFilePersistence<PersistableDocumentContainer>(
                new PersistableDocumentContainer()
                , new File(tmpDir, getPreferences().getString("ae.experiments.cache.filename"))
        );

        experimentsInWarehouse = new TextFilePersistence<PersistableStringList>(
                new PersistableStringList()
                , new File(tmpDir, getPreferences().getString("ae.warehouseexperiments.cache.filename"))
        );

        experimentSearch = new ExperimentSearch();

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
    }

    public void terminate()
    {
    }

    public synchronized Document getExperiments()
    {
        Document doc = experiments.getObject().getDocument();

        if (experimentSearch.isEmpty()) {
            experimentSearch.buildText(doc);
        }

        return doc;
    }

    public synchronized ExperimentSearch getSearch()
    {
        if (experimentSearch.isEmpty()) {
            experimentSearch.buildText(experiments.getObject().getDocument());
        }

        return experimentSearch;
    }

    public boolean isAccessible( String accession, String userId )
    {
        return getSearch().isAccessible(accession, userId);
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
        setExperiments(loadExperimentsFromString(xmlString));

        species.setObject(
                new PersistableString(
                        ((XsltHelper) getApplication().getComponent("XsltHelper")).transformDocumentToString(
                                experiments.getObject().getDocument()
                                , "preprocess-species-html.xsl"
                                , null
                        )
                )
        );

        arrays.setObject(
                new PersistableString(
                        ((XsltHelper) getApplication().getComponent("XsltHelper")).transformDocumentToString(
                                experiments.getObject().getDocument()
                                , "preprocess-arrays-html.xsl"
                                , null
                        )
                )
        );

        experimentTypes.setObject(
                new PersistableString(
                        ((XsltHelper) getApplication().getComponent("XsltHelper")).transformDocumentToString(
                                experiments.getObject().getDocument()
                                , "preprocess-exptypes-html.xsl"
                                , null
                        )
                )
        );
    }

    public void setExperimentsInWarehouse( List<String> expList )
    {
        experimentsInWarehouse.setObject(new PersistableStringList(expList));    
    }

    private synchronized void setExperiments( Document doc )
    {
        experiments.setObject(new PersistableDocumentContainer(doc));
        experimentSearch.buildText(doc);
    }

    private Document loadExperimentsFromString( String xmlString )
    {
        Document doc = ((XsltHelper) getComponent("XsltHelper")).transformStringToDocument(xmlString, "preprocess-experiments-xml.xsl", null);
        if (null == doc) {
            log.error("Transformation [preprocess-experiments-xml.xsl] returned an error, returning null");
            return null;
        }
        doc = ((XsltHelper) getComponent("XsltHelper")).transformDocument(doc, "preprocess-experiment-files-xml.xsl", null);
        if (null == doc) {
            log.error("Transformation [preprocess-experiment-files-xml.xsl] returned an error, returning null");
            return null;
        }
        return doc;
    }
}
