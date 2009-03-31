package uk.ac.ebi.arrayexpress.utils.model;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.model.ExperimentBean;

import java.io.StringReader;

public class ExperimentParser
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Digester digester;
    
    public ExperimentParser()
    {
        digester = new Digester();
        digester.setValidating(false);
        digester.setXIncludeAware(false);

        digester.addObjectCreate("experiment", ExperimentBean.class);

        digester.addCallMethod("experiment", "setAttributes", 5);
        digester.addCallParam("experiment", 0, "id");
        digester.addCallParam("experiment", 1, "accession");
        digester.addCallParam("experiment", 2, "name");
        digester.addCallParam("experiment", 3, "releasedate");
        digester.addCallParam("experiment", 4, "miamegold");

        digester.addCallMethod("experiment/user", "addUser", 1);
        digester.addCallParam("experiment/user", 0);

        digester.addCallMethod("experiment/secondaryaccession", "addSecondaryAccession", 1);
        digester.addCallParam("experiment/secondaryaccession", 0);

        digester.addCallMethod("experiment/sampleattribute", "addSampleAttribute", 2);
        digester.addCallParam("experiment/sampleattribute", 0, "category");
        digester.addCallParam("experiment/sampleattribute", 1, "value");

        digester.addCallMethod("experiment/experimentalfactor", "addExperimentalFactor", 2);
        digester.addCallParam("experiment/experimentalfactor", 0, "name");
        digester.addCallParam("experiment/experimentalfactor", 1, "value");

        digester.addCallMethod("experiment/miamescore", "addMiameScore", 2);
        digester.addCallParam("experiment/miamescore", 0, "name");
        digester.addCallParam("experiment/miamescore", 1, "value");

        digester.addCallMethod("experiment/arraydesign", "addArrayDesign", 4);
        digester.addCallParam("experiment/arraydesign", 0, "id");
        digester.addCallParam("experiment/arraydesign", 1, "accession");
        digester.addCallParam("experiment/arraydesign", 2, "name");
        digester.addCallParam("experiment/arraydesign", 3, "count");

        digester.addCallMethod("experiment/bioassaydatagroup", "addBioAssayDataGroup", 7);
        digester.addCallParam("experiment/bioassaydatagroup", 0, "id");
        digester.addCallParam("experiment/bioassaydatagroup", 1, "name");
        digester.addCallParam("experiment/bioassaydatagroup", 2, "bioassaydatacubes");
        digester.addCallParam("experiment/bioassaydatagroup", 3, "arraydesignprovider");
        digester.addCallParam("experiment/bioassaydatagroup", 4, "dataformat");
        digester.addCallParam("experiment/bioassaydatagroup", 5, "bioassays");
        digester.addCallParam("experiment/bioassaydatagroup", 6, "isderived");

        digester.addCallMethod("experiment/bibliography", "addBibliography", 9);
        digester.addCallParam("experiment/bibliography", 0, "accession");
        digester.addCallParam("experiment/bibliography", 1, "publication");
        digester.addCallParam("experiment/bibliography", 2, "authors");
        digester.addCallParam("experiment/bibliography", 3, "title");
        digester.addCallParam("experiment/bibliography", 4, "year");
        digester.addCallParam("experiment/bibliography", 5, "volume");
        digester.addCallParam("experiment/bibliography", 6, "issue");
        digester.addCallParam("experiment/bibliography", 7, "pages");
        digester.addCallParam("experiment/bibliography", 8, "uri");

        digester.addCallMethod("experiment/provider", "addProvider", 3);
        digester.addCallParam("experiment/provider", 0, "contact");
        digester.addCallParam("experiment/provider", 1, "email");
        digester.addCallParam("experiment/provider", 2, "role");

        digester.addCallMethod("experiment/experimentdesign", "addExperimentDesign", 1);
        digester.addCallParam("experiment/experimentdesign", 0);

        digester.addCallMethod("experiment/experimenttype", "addExperimentType", 1);
        digester.addCallParam("experiment/experimenttype", 0);

        digester.addCallMethod("experiment/description", "addDescription", 2);
        digester.addCallParam("experiment/description", 0, "id");
        digester.addCallParam("experiment/description", 1);
    }

    public ExperimentBean parse(String xml)
    {
        ExperimentBean exp = null;
        try {
            exp = (ExperimentBean)digester.parse(new StringReader(xml));
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
        return exp;
    }
}
