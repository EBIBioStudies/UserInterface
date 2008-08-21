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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Experiments extends ApplicationComponent
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private TextFilePersistence<PersistableDocumentContainer> experiments;
    private TextFilePersistence<PersistableStringList> experimentsInWarehouse;
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

        experimentsInWarehouse = new TextFilePersistence<PersistableStringList>(
                new PersistableStringList()
                , new File(System.getProperty("java.io.tmpdir"), getPreferences().getString("ae.warehouseexperiments.cache.filename"))
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
        return experimentSearch;
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
    }

    public void setExperimentsInWarehouse( List<String> expList )
    {
        experimentsInWarehouse.setObject(new PersistableStringList(expList));    
    }

    // method attempts to extract experiment accession number from file location path and if found
    // checks the expeirment presence in xml index
    public boolean isFilePublic( String file )
    {
        boolean result = true;

        Pattern p = Pattern.compile("/(E-[^-]+-[0-9]+)/");
        Matcher m = p.matcher(file);
        if (m.find()) {
            String accession = m.group(1);
            result = getSearch().doesPresent(accession);
        }
        return result;
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
            log.error("Pre-processing returned an error, returning null");
            return null;
        }
        return doc;
    }
}
