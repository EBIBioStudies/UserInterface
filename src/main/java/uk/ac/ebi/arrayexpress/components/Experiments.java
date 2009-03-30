package uk.ac.ebi.arrayexpress.components;

import net.sf.saxon.s9api.XdmNode;
import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableDocumentContainer;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableString;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableStringList;
import uk.ac.ebi.arrayexpress.utils.persistence.TextFilePersistence;
import uk.ac.ebi.arrayexpress.utils.search.ExperimentSearch;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Experiments extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private TextFilePersistence<PersistableDocumentContainer> experiments;
    private TextFilePersistence<PersistableStringList> experimentsInWarehouse;
    private TextFilePersistence<PersistableString> species;
    private TextFilePersistence<PersistableString> arrays;
    private TextFilePersistence<PersistableString> experimentTypes;

    private ExperimentSearch experimentSearch;
    private String dataSource;

    public Experiments()
    {
        super("Experiments");
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

    public synchronized XdmNode getExperiments()
    {
        XdmNode doc = experiments.getObject().getDocument();

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
        // TODO: ugly shortcut to bypass security for Array Designs
        return accession.toUpperCase().startsWith("A") || getSearch().isAccessible(accession, userId);
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
        XdmNode doc = loadExperimentsFromString(xmlString);
        if (null != doc) {
            setExperiments(doc);

            PersistableString speciesList = new PersistableString(
                ((SaxonEngine) Application.getInstance().getComponent("SaxonEngine")).transformToString(
                    experiments.getObject().getDocument()
                    , "preprocess-species-html.xsl"
                    , null
                )
            );

            if (null != speciesList.get()) {
                species.setObject(speciesList);
            } else {
                logger.error("Species list NOT updated, NULL string passed");
            }

            PersistableString arraysList = new PersistableString(
                ((SaxonEngine) Application.getInstance().getComponent("SaxonEngine")).transformToString(
                    experiments.getObject().getDocument()
                    , "preprocess-arrays-html.xsl"
                    , null
                )
            );

            if (null != arraysList.get()) {
                arrays.setObject(arraysList);
            } else {
                logger.error("Arrays list NOT updated, NULL string passed");
            }

            PersistableString experimentTypesList = new PersistableString(
                ((SaxonEngine) Application.getInstance().getComponent("SaxonEngine")).transformToString(
                    experiments.getObject().getDocument()
                    , "preprocess-exptypes-html.xsl"
                    , null
                )
            );

            if (null != experimentTypesList.get()) {
                experimentTypes.setObject(experimentTypesList);
            } else {
                logger.error("Experiment Types list NOT updated, NULL string passed");
            }
        } else {
            logger.error("Experiments NOT updated, NULL document passed");
        }
    }

    public void updateFiles()
    {
        logger.info("Experiments: file info update requested");
        XdmNode doc = ((SaxonEngine) getComponent("SaxonEngine")).transform(getExperiments(), "preprocess-experiment-files-xml.xsl", null);
        if (null != doc) {
            setExperiments(doc);
            logger.info("Experiments: file info update completed");
        } else {
            logger.error("Transformation [preprocess-experiment-files-xml.xsl] returned an error, experiments NOT updated");
        }
    }

    public void setExperimentsInWarehouse( List<String> expList )
    {
        if (null != expList && 0 < expList.size()) {
            experimentsInWarehouse.setObject(new PersistableStringList(expList));
        } else {
            logger.error("List of warehouse experiments NOT updated, attempted to assign NULL or EMPTY list");
        }
    }

    private synchronized void setExperiments( XdmNode doc )
    {
        if (null != doc ) {
            experiments.setObject(new PersistableDocumentContainer(doc));
            experimentSearch.buildText(doc);
        } else {
            logger.error("Experiments NOT updated, NULL document passed");
        }
    }

    private XdmNode loadExperimentsFromString( String xmlString )
    {
        XdmNode doc = ((SaxonEngine) getComponent("SaxonEngine")).transform(xmlString, "preprocess-experiments-xml.xsl", null);
        if (null == doc) {
            logger.error("Transformation [preprocess-experiments-xml.xsl] returned an error, returning null");
            return null;
        }
        doc = ((SaxonEngine) getComponent("SaxonEngine")).transform(doc, "preprocess-experiment-files-xml.xsl", null);
        if (null == doc) {
            logger.error("Transformation [preprocess-experiment-files-xml.xsl] returned an error, returning null");
            return null;
        }
        return doc;
    }
}
