package uk.ac.ebi.ae15.jobs;

import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.components.Experiments;

public class ReloadExperimentsJob extends ApplicationJob
{
    public void execute( Application app ) throws InterruptedException
    {
        if (null != app) {
            ((Experiments) app.getComponent("Experiments")).reload();
        }
    }
}
