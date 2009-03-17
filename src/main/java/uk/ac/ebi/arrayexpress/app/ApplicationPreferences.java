package uk.ac.ebi.arrayexpress.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;

public class ApplicationPreferences extends ApplicationComponent
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private String propertiesFileName;
    private Properties properties;

    public ApplicationPreferences( Application app, String fileName )
    {
        super(app, "Preferences");

        propertiesFileName = fileName;
        properties = new Properties();
    }

    public void initialize()
    {
        load();
    }

    public void terminate()
    {
        properties = null;
    }

    public String getString( String key )
    {
        return properties.getProperty(key);
    }

    public Long getLong( String key )
    {
        Long value = null;
        String strVal = null;
        try {
            strVal = properties.getProperty(key);
            if (null != strVal)
                value = Long.valueOf(strVal);
        } catch ( NumberFormatException x ) {
            log.error("Value [" + strVal + "] of preference [" + key + "] is expected to be a number");
        } catch ( Throwable x ) {
            log.error("Caught an exception while converting value [" + properties.getProperty(key) + "] of preference [" + key + "] to Long:", x);
        }

        return value;
    }

    public boolean getBoolean( String key )
    {
        String value = properties.getProperty(key);
        return (null != value && value.toLowerCase().equals("true"));
    }

    private void load()
    {
        try {
            properties.load(
                    getApplication().getResource(
                            "/WEB-INF/classes/" + propertiesFileName + ".properties"
                    ).openStream()
            );
        } catch ( Throwable e ) {
            log.error("Caught an exception:", e);
        }
    }
}
