package uk.ac.ebi.ae15.jobs;

import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.components.DownloadableFilesRegistry;

public class RescanFilesJob extends ApplicationJob
{
    public void execute( Application app ) throws InterruptedException
    {
        if (null != app) {
            ((DownloadableFilesRegistry) app.getComponent("DownloadableFilesRegistry")).rescan();
        }
    }
}
