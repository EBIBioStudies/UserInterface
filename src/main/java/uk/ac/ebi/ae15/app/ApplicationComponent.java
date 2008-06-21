package uk.ac.ebi.ae15.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract public class ApplicationComponent
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private Application application;
    private String name;

    public ApplicationComponent( Application app, String name )
    {
        if (null == app) {
            log.error("Null application reference just passed to the component, expect problems down the road");
        }
        application = app;
    }

    public Application getApplication()
    {
        return application;
    }

    public String getName()
    {
        return name;
    }

    public abstract void initialize();
    public abstract void terminate();
}
