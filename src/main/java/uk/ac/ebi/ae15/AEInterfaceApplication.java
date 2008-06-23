package uk.ac.ebi.ae15;

import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.components.DownloadableFilesRegistry;
import uk.ac.ebi.ae15.components.Experiments;
import uk.ac.ebi.ae15.components.JobsController;
import uk.ac.ebi.ae15.components.XsltHelper;

public class AEInterfaceApplication extends Application
{
    public AEInterfaceApplication()
    {
        super("arrayexpress");
        addComponent(new Experiments(this));
        addComponent(new DownloadableFilesRegistry(this));
        addComponent(new XsltHelper(this));
        addComponent(new JobsController(this));
    }
}
