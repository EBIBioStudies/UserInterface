package uk.ac.ebi.arrayexpress.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.utils.EmailSender;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Application
{
    // logging machinery
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private String name;
    private ApplicationPreferences prefs;
    private Map<String, ApplicationComponent> components;
    private EmailSender emailer;

    public Application( String appName )
    {
        name = appName;
        prefs = new ApplicationPreferences(getName());
        components = new LinkedHashMap<String, ApplicationComponent>();
        // setting application instance available to whoever wants it
        appInstance = this;
    }

    public String getName()
    {
        return name;
    }

    public abstract URL getResource( String path ) throws MalformedURLException;

    public void addComponent( ApplicationComponent component )
    {
        if (components.containsKey(component.getName())) {
            logger.error("The component [{}] has already been added to the application", component.getName());
        } else {
            components.put(component.getName(), component);
        }
    }

    public ApplicationComponent getComponent( String name )
    {
        if (components.containsKey(name))
            return components.get(name);
        else
            return null;
    }

    public ApplicationPreferences getPreferences()
    {
        return prefs;
    }

    public void initialize()
    {
        logger.debug("Initializing the application...");
        prefs.initialize();
        emailer = new EmailSender(getPreferences().getString("ae.reports.smtp.server"));

        for (ApplicationComponent c : components.values()) {
            logger.info("Initializing component [{}]", c.getName());
            try {
                c.initialize();
            } catch (Exception x) {
                logger.error("Caught an exception while initializing [" + c.getName() + "]:", x);
            } catch (Error x) {
                logger.error("[SEVERE] Caught an error while initializing [" + c.getName() + "]:", x);
                sendExceptionReport("[SEVERE] Caught an error while initializing [" + c.getName() + "]", x);
            }
        }
    }

    public void terminate()
    {
        logger.debug("Terminating the application...");
        ApplicationComponent[] compArray = components.values().toArray(new ApplicationComponent[components.size()]);

        for (int i = compArray.length - 1; i >= 0; --i) {
            ApplicationComponent c = compArray[i];
            logger.info("Terminating component [{}]", c.getName());
            try {
                c.terminate();
            } catch (Throwable x) {
                logger.error("Caught an exception while terminating [" + c.getName() + "]:", x);
            }
        }
        // release references to application components
        components.clear();
        components = null;

        // remove reference to self
        appInstance = null;
    }

    public void sendEmail( String subject, String message )
    {
        try {

            emailer.send(getPreferences().getStringArray("ae.reports.recipients")
                    , subject
                    , message
                    , getPreferences().getString("ae.reports.originator")
            );

        } catch (Throwable x) {
            logger.error("[SEVERE] Cannot even send an email without an exception:", x);
        }
    }

    public void sendExceptionReport( String message, Throwable x )
    {
        sendEmail("AE Interface Runtime Exception Report"
                , message + ": " + x.getMessage() + "\n\n" + getStackTrace(x)
        );
    }

    private String getStackTrace( Throwable x )
    {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        x.printStackTrace(printWriter);
        return result.toString();
    }

    public static Application getInstance()
    {
        if (null == appInstance) {
            logger.error("Attempted to obtain application instance before initialization or after destruction");
        }
        return appInstance;
    }

    public static ApplicationComponent getAppComponent( String name )
    {
        return getInstance().getComponent(name);
    }

    private static Application appInstance = null;
}
