package uk.ac.ebi.ae15.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.components.Experiments;

public class ReloadExperimentsJob extends ApplicationJob
{
    // logging facitlity
    private final Log log = LogFactory.getLog(getClass());

    public void execute( Application app ) throws InterruptedException
    {
        if (null != app) {
            ((Experiments) app.getComponent("Experiments")).reload();
        }
    }
}
