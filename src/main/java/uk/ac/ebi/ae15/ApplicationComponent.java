package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ApplicationComponent
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private Application application;

    public ApplicationComponent( Application app )
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
}
