package uk.ac.ebi.arrayexpress.components;

import net.sf.saxon.om.DocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableDocumentContainer;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableString;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableStringList;
import uk.ac.ebi.arrayexpress.utils.persistence.TextFilePersistence;
import uk.ac.ebi.arrayexpress.utils.saxon.DocumentSource;

import java.io.File;
import java.util.List;

public class Experiments extends ApplicationComponent implements DocumentSource
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String dataSource;
    private TextFilePersistence<PersistableDocumentContainer> experiments;
    private TextFilePersistence<PersistableStringList> experimentsInAtlas;
    private TextFilePersistence<PersistableString> species;
    private TextFilePersistence<PersistableString> arrays;
    private TextFilePersistence<PersistableString> experimentTypes;

    private SaxonEngine saxon;

    public Experiments()
    {
        super("Experiments");
    }

    public void initialize()
    {
        saxon = (SaxonEngine)getComponent("SaxonEngine");

        this.experiments = new TextFilePersistence<PersistableDocumentContainer>(
                new PersistableDocumentContainer()
                , new File(getPreferences().getString("ae.experiments.file.location"))
        );

        this.experimentsInAtlas = new TextFilePersistence<PersistableStringList>(
                new PersistableStringList()
                , new File(getPreferences().getString("ae.atlasexperiments.file.location"))
        );

        this.species = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(getPreferences().getString("ae.species.file.location"))

        );

        this.arrays = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(getPreferences().getString("ae.arrays.file.location"))
        );

        this.experimentTypes = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(getPreferences().getString("ae.exptypes.file.location"))
        );

        indexExperiments();
        saxon.registerDocumentSource(this);
    }

    public void terminate()
    {
        saxon = null;
    }

    // implementation of DocumentSource.getDocument()
    public String getDocumentURI()
    {
        return "experiments.xml";
    }

    // implementation of DocumentSource.getDocument()
    public synchronized DocumentInfo getDocument()
    {
        return this.experiments.getObject().getDocument();
    }

    public boolean isAccessible( String accession, String userId )
    {
        if ("0".equals(userId)) {
            return true;
        } else {
            return Boolean.parseBoolean(
                saxon.evaluateXPathSingle(
                        getDocument()
                        , "exists(//experiment[accession = '" + accession + "' and user = '" + userId + "'])"
                )
            );
        }
    }

    public boolean isInAtlas( String accession )
    {
        return this.experimentsInAtlas.getObject().contains(accession);
    }

    public String getSpecies()
    {
        return this.species.getObject().get();
    }

    public String getArrays()
    {
        return this.arrays.getObject().get();
    }

    public String getExperimentTypes()
    {
        return this.experimentTypes.getObject().get();
    }


    public String getDataSource()
    {
        if (null == this.dataSource) {
            this.dataSource = getPreferences().getString("ae.experiments.datasources");
        }

        return this.dataSource;
    }

    public void setDataSource( String dataSource )
    {
        this.dataSource = dataSource;
    }

    public void reload( String xmlString )
    {
        DocumentInfo doc = loadExperimentsFromString(xmlString);
        if (null != doc) {
            setExperiments(doc);
            buildSpeciesArraysExpTypes(doc);
            indexExperiments();
        }
    }

    public void setExperimentsInAtlas( List<String> expList )
    {
        this.experimentsInAtlas.setObject(new PersistableStringList(expList));
    }

    private synchronized void setExperiments( DocumentInfo doc )
    {
        if (null != doc) {
            this.experiments.setObject(new PersistableDocumentContainer(doc));
        } else {
            this.logger.error("Experiments NOT updated, NULL document passed");
        }
    }

    private DocumentInfo loadExperimentsFromString( String xmlString )
    {
        DocumentInfo doc = saxon.transform(xmlString, "preprocess-experiments-xml.xsl", null);
        if (null == doc) {
            this.logger.error("Transformation [preprocess-experiments-xml.xsl] returned an error, returning null");
            return null;
        }
        return doc;
    }

    private void indexExperiments()
    {
        try {
            ((SearchEngine)getComponent("SearchEngine")).getController().index("experiments", experiments.getObject().getDocument());
            //List<String> expDesign = Controller.getInstance().getTerms("experiments", "expdesign");
            //logger.debug("Retrieved experiment design list, size [{}]", expDesign.size());
        } catch (Throwable x) {
            this.logger.error("Caught an exception:", x);
        }
    }

    private void buildSpeciesArraysExpTypes( DocumentInfo doc )
    {
        String speciesString = saxon.transformToString(doc, "build-species-list-html.xsl", null);
        this.species.setObject(new PersistableString(speciesString));

        String arraysString = saxon.transformToString(doc, "build-arrays-list-html.xsl", null);
        this.arrays.setObject(new PersistableString(arraysString));

        String expTypesString = saxon.transformToString(doc, "build-exptypes-list-html.xsl", null);
        this.experimentTypes.setObject(new PersistableString(expTypesString));
    }
}
