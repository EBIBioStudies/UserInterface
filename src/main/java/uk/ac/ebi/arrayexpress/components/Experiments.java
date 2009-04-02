package uk.ac.ebi.arrayexpress.components;

import net.sf.saxon.s9api.XdmNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.model.ExperimentList;
import uk.ac.ebi.arrayexpress.utils.model.parser.ExperimentParser;
import uk.ac.ebi.arrayexpress.utils.model.parser.ExperimentParserMode;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableString;
import uk.ac.ebi.arrayexpress.utils.persistence.TextFilePersistence;

import java.io.File;
import java.util.List;

public class Experiments extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String dataSource;
    private TextFilePersistence<PersistableString> experimentsXml;
    private ExperimentList experiments;

    public Experiments()
    {
        super("Experiments");
    }

    public void initialize()
    {
        String tmpDir = System.getProperty("java.io.tmpdir");
        experimentsXml = new TextFilePersistence<PersistableString>(
            new PersistableString()
            , new File(tmpDir, "ae-experiments-2.xml")
        );
        if (!experimentsXml.getObject().isEmpty()) {
            experiments = new ExperimentParser(ExperimentParserMode.MULTIPLE_EXPERIMENTS).parseMultiple(
                experimentsXml.getObject().get()
            );    
        }
    }

    public void terminate()
    {
    }

    public synchronized XdmNode getExperiments()
    {
        return null;
    }

    public boolean isAccessible( String accession, String userId )
    {
        return false;
    }

    public boolean isInWarehouse( String accession )
    {
        return false;
    }

    public String getSpecies()
    {
        return "";
    }

    public String getArrays()
    {
        return "";
    }

    public String getExperimentTypes()
    {
        return "";
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
        experimentsXml.setObject(new PersistableString(xmlString));
        experiments = new ExperimentParser(ExperimentParserMode.MULTIPLE_EXPERIMENTS).parseMultiple(xmlString);
        if (null == experiments) {
            logger.error("Null experiments received, expect problems down the road");
        }
    }

    public void updateFiles()
    {
    }

    public void setExperimentsInWarehouse( List<String> expList )
    {
    }
}
