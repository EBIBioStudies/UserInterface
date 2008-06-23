package uk.ac.ebi.ae15.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;

public class ApplicationPreferences extends ApplicationComponent
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private String propertiesFileName;
    private Properties properties;

    public ApplicationPreferences(Application app, String fileName)
    {
        super(app, "Preferences");

        propertiesFileName = fileName;
        properties = new Properties();
    }

    public void initialize()
    {
        try {
            properties.load(
                    getApplication().getServletContext().getResource(
                            "/WEB-INF/classes/" + propertiesFileName + ".properties"
                    ).openStream()
            );
        } catch (Throwable e) {
            log.error("Caught an exception:", e);
        }

    }

    public void terminate()
    {
        properties = null;
    }

    public String get(String key)
    {
        return (String) properties.get(key);
    }
}
