package uk.ac.ebi.ae15;

import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.components.*;

public class AEInterfaceApplication extends Application
{
    public AEInterfaceApplication()
    {
        super("arrayexpress");
        addComponent(new Experiments(this));
        addComponent(new Users(this));
        addComponent(new DownloadableFilesRegistry(this));
        addComponent(new XsltHelper(this));
        addComponent(new JobsController(this));
    }
}
