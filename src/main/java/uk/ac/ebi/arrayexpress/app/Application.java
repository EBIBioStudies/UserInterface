package uk.ac.ebi.arrayexpress.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class Application implements ServletContextListener
{
    // logging machinery
    private static final Log log = LogFactory.getLog(Application.class);

    private String name;
    private ServletContext servletContext;
    private Map<String, ApplicationComponent> components;

    public Application( String appName )
    {
        name = appName;
        components = new LinkedHashMap<String, ApplicationComponent>();
        addComponent(new ApplicationPreferences(this, getName()));

        // setting applivation instance for whoever wants us
        appInstance = this;
    }

    public String getName()
    {
        return name;
    }

    public URL getResource(String path) throws MalformedURLException
    {
        return null != servletContext ? servletContext.getResource(path) : null;
    }

    public void addComponent( ApplicationComponent component )
    {
        if (components.containsKey(component.getName())) {
            log.error("The component [" + component.getName() + "] has already been added to the application");
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

    public synchronized void contextInitialized( ServletContextEvent sce )
    {
        servletContext = sce.getServletContext();
        log.info("****************************************************************************************************************************");
        log.info("*");
        log.info("*  " + servletContext.getServletContextName());
        log.info("*");
        log.info("****************************************************************************************************************************");

        initialize();
    }

    public synchronized void contextDestroyed( ServletContextEvent sce )
    {
        terminate();
        appInstance = null;
        servletContext = null;
        log.info("****************************************************************************************************************************\n\n");
    }

    private void initialize()
    {
        log.debug("Initializing the application...");
        for ( ApplicationComponent c : components.values() ) {
            log.info("Initializing component [" + c.getName() + "]");
            try {
                c.initialize();
            } catch ( Throwable x ) {
                log.error("Caught an exception while initializing[" + c.getName() + "]:", x);
            }
        }
    }

    private void terminate()
    {
        log.debug("Terminating the application...");
        ApplicationComponent[] compArray = components.values().toArray(new ApplicationComponent[components.size()]);

        for ( int i = compArray.length - 1; i >= 0; --i ) {
            ApplicationComponent c = compArray[i];
            log.info("Terminating component [" + c.getName() + "]");
            try {
                c.terminate();
            } catch ( Throwable x ) {
                log.error("Caught an exception while terminating [" + c.getName() + "]:", x);
            }
        }
        // release references to application components
        components.clear();
        components = null;
    }

    public static Application getInstance()
    {
        if (null == appInstance) {
            log.error("Attempted to obtain application instance before initialization or after destruction");
        }
        return appInstance;
    }

    private static Application appInstance = null;
}
