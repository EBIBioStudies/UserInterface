package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ApplicationComponent {

    public ApplicationComponent( Application app )
    {
        if ( null == app ) {
            log.error("Null application reference just passed to the component, expect problems down the road");
        }
        application = app;
    }

    public Application getApplication()
    {
        return application;
    }

    private Application application;

    // logging macinery
    private final Log log = LogFactory.getLog(getClass());
}
