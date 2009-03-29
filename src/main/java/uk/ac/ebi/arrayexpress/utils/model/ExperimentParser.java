package uk.ac.ebi.arrayexpress.utils.model;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.arrayexpress.model.ExperimentBean;

import java.io.StringReader;

public class ExperimentParser
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

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
    }

    public ExperimentBean parse(String xml)
    {
        ExperimentBean exp = null;
        try {
            exp = (ExperimentBean)digester.parse(new StringReader(xml));
        } catch (Throwable x) {
            log.error("Caught an exception:", x);
        }
        return exp;
    }
}
