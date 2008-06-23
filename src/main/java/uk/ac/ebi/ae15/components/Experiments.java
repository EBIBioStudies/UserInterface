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
    }

    public void initialize()
    {
        experiments = new TextFilePersistence<PersistableDocumentContainer>(
                new PersistableDocumentContainer()
                , new File(System.getProperty("java.io.tmpdir"), getPreferences().getString("ae.experiments.cache.filename"))
        );

        experimentSearch = new ExperimentSearch();

        species = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(System.getProperty("java.io.tmpdir"), getPreferences().getString("ae.species.cache.filename"))

        );

        arrays = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(System.getProperty("java.io.tmpdir"), getPreferences().getString("ae.arrays.cache.filename"))
        );
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
        experiments.setObject(
                new PersistableDocumentContainer(
                        loadExperimentsFromString(xmlString)
                )
        );
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

        experimentSearch.buildText(experiments.getObject().getDocument());

    }

    private Document loadExperimentsFromString( String xmlString )
    {
        Document doc = ((XsltHelper) getComponent("XsltHelper")).transformStringToDocument(xmlString, "preprocess-experiments-xml.xsl", null);
        if (null == doc) {
            log.error("Pre-processing returned an error, returning null");
            return null;
        }
        return doc;
    }
}
