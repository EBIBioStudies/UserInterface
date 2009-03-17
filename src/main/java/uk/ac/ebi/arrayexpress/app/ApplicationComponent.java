package uk.ac.ebi.arrayexpress.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract public class ApplicationComponent
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private Application application;
    private String componentName;

    public ApplicationComponent( Application app, String name )
    {
        if (null == app) {
            log.error("Null application reference just passed to the component, expect problems down the road");
        }
        application = app;
        componentName = name;
    }

    public String getName()
    {
        return componentName;
    }

    public Application getApplication()
    {
        return application;
    }

    public ApplicationComponent getComponent( String name )
    {
        return getApplication().getComponent(name);
    }

    public ApplicationPreferences getPreferences()
    {
        return getApplication().getPreferences();
    }

    public abstract void initialize();

    public abstract void terminate();
}
