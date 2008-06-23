package uk.ac.ebi.ae15.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashMap;
import java.util.Map;

public class Application implements ServletContextListener
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private String name;
    private ServletContext servletContext;
    private Map<String, ApplicationComponent> components;

    public Application( String appName )
    {
        name = appName;
        components = new HashMap<String, ApplicationComponent>();
    }

    public String getName()
    {
        return name;
    }

    public ServletContext getServletContext()
    {
        return servletContext;
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
        servletContext.setAttribute(Application.class.getName(), this);

        addComponent(new ApplicationPreferences(this, getName()));
        initialize();
    }

    public synchronized void contextDestroyed( ServletContextEvent sce )
    {
        terminate();

        servletContext.setAttribute(Application.class.getName(), null);
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
        for ( ApplicationComponent c : components.values() ) {
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
}
