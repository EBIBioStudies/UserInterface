package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.app.ApplicationComponent;

import java.util.Properties;

public class Preferences extends ApplicationComponent
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    public Preferences( Application app)
    {
        super(app);
        properties = new Properties();
    }

    protected void initializeComponent()
    {

    }

    public void terminateComponent()
    {

    }

    public void load()
    {
        try {
            properties.load(
                    getApplication().getServletContext().getResource("/WEB-INF/classes/arrayexpress.properties").openStream()
            );
        } catch (Throwable e) {
            log.error("Caught an exception:", e);
        }
    }

    public String get(String key)
    {
        return (String) properties.get(key);
    }

    private Properties properties;
}
