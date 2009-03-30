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
