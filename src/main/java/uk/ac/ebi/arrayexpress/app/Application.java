package uk.ac.ebi.microarray.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Application
{
    // logging machinery
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private String name;
    private Map<String, ApplicationComponent> components;

    public Application( String appName )
    {
        name = appName;
        components = new LinkedHashMap<String, ApplicationComponent>();
        addComponent(new ApplicationPreferences(getName()));

        // setting applivation instance for whoever wants us
        appInstance = this;
    }

    public String getName()
    {
        return name;
    }

    public abstract URL getResource(String path) throws MalformedURLException;

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
        return (ApplicationPreferences) getComponent("Preferences");
    }

    public void initialize()
    {
        logger.debug("Initializing the application...");
        for ( ApplicationComponent c : components.values() ) {
            logger.info("Initializing component [{}]", c.getName());
            try {
                c.initialize();
            } catch ( Throwable x ) {
                logger.error("Caught an exception while initializing [" + c.getName() + "]:", x);
            }
        }
    }

    public void terminate()
    {
        logger.debug("Terminating the application...");
        ApplicationComponent[] compArray = components.values().toArray(new ApplicationComponent[components.size()]);

        for ( int i = compArray.length - 1; i >= 0; --i ) {
            ApplicationComponent c = compArray[i];
            logger.info("Terminating component [{}]", c.getName());
            try {
                c.terminate();
            } catch ( Throwable x ) {
                logger.error("Caught an exception while terminating [" + c.getName() + "]:", x);
            }
        }
        // release references to application components
        components.clear();
        components = null;

        // remove reference to self
        appInstance = null;
    }

    public static Application getInstance()
    {
        if (null == appInstance) {
            logger.error("Attempted to obtain application instance before initialization or after destruction");
        }
        return appInstance;
    }

    public static ApplicationComponent getAppComponent(String name)
    {
        return getInstance().getComponent(name);
    }

    private static Application appInstance = null;
}
