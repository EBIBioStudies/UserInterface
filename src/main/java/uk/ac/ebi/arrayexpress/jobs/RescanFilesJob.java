package uk.ac.ebi.arrayexpress.jobs;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.components.DownloadableFilesRegistry;

public class RescanFilesJob extends ApplicationJob
{
    public void execute( Application app ) throws InterruptedException
    {
        if (null != app) {
            ((DownloadableFilesRegistry) app.getComponent("DownloadableFilesRegistry")).rescan();
        }
    }
}
