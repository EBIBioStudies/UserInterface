package uk.ac.ebi.arrayexpress;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.components.*;

public class AEInterfaceApplication extends Application
{
    public AEInterfaceApplication()
    {
        super("arrayexpress");
        addComponent(new SaxonEngine(this));
        addComponent(new Experiments(this));
        addComponent(new Users(this));
        addComponent(new DownloadableFilesRegistry(this));
        addComponent(new JobsController(this));
    }
}
