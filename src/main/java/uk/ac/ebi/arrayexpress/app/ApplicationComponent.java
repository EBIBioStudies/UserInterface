package uk.ac.ebi.arrayexpress.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract public class ApplicationComponent
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private String componentName;

    public ApplicationComponent( String name )
    {
        componentName = name;
    }

    public String getName()
    {
        return componentName;
    }

    public ApplicationComponent getComponent( String name )
    {
        return Application.getInstance().getComponent(name);
    }

    public ApplicationPreferences getPreferences()
    {
        return Application.getInstance().getPreferences();
    }

    public abstract void initialize();

    public abstract void terminate();
}
