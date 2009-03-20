package uk.ac.ebi.arrayexpress;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.components.*;

public class AEInterfaceApplication extends Application
{
    public AEInterfaceApplication()
    {
        super("arrayexpress");
        addComponent(new SaxonEngine());
        addComponent(new Experiments());
        addComponent(new Users());
        addComponent(new DownloadableFilesRegistry());
        addComponent(new JobsController());
    }
}
