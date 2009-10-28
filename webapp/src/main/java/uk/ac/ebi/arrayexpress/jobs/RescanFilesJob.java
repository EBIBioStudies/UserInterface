package uk.ac.ebi.arrayexpress.jobs;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationJob;
import uk.ac.ebi.arrayexpress.components.Files;

public class RescanFilesJob extends ApplicationJob
{
    public void execute() throws InterruptedException
    {
        ((Files) Application.getInstance().getComponent("Files")).rescan();
    }
}
