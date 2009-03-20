package uk.ac.ebi.arrayexpress.jobs;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationJob;
import uk.ac.ebi.arrayexpress.components.DownloadableFilesRegistry;

public class RescanFilesJob extends ApplicationJob
{
    public void execute() throws InterruptedException
    {
        ((DownloadableFilesRegistry) Application.getInstance().getComponent("DownloadableFilesRegistry")).rescan();
    }
}
