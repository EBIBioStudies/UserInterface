package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.Properties;


// class Preferences
//
// reads all configuration properties from /WEB-INF/classes/arrayexpress.properties

public class Preferences extends ApplicationComponent {

    public Preferences( Application app )
    {
        super(app);
        properties = new Properties();
    }

    public void load() {
        try {
            properties.load(
                    getApplication().getServletContext().getResource("/WEB-INF/classes/arrayexpress.properties").openStream()
            );
        } catch ( Throwable e ) {
            log.error( "Caught an exception:", e );
        }
    }

    public Object get( Object key )
    {
        return properties.get(key);
    }
    private Properties properties;

    // logging macinery
    private final Log log = LogFactory.getLog(getClass());
}
