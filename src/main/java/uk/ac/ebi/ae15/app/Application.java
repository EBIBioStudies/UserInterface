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

    private ServletContext servletContext;
    private Map<String,ApplicationComponent> components;

    public Application()
    {
        components = new HashMap<String,ApplicationComponent>();
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }

    public void addComponent( ApplicationComponent component )
    {
        if ( components.containsKey(component.getName()) ) {
            log.error("The component [" + component.getName() + "] has already been added to the application");
        } else {
            log.debug("Adding component [" + component.getName() + "]");
            components.put(component.getName(), component);
        }
    }

    public synchronized void contextInitialized( ServletContextEvent sce )
    {
        ServletContext sc = sce.getServletContext();
        log.info("****************************************************************************************************************************");
        log.info("*");
        log.info("*  " + sc.getServletContextName());
        log.info("*");
        log.info("****************************************************************************************************************************");

        sc.setAttribute(getClass().getName(), this);

        initialize();
    }

    public synchronized void contextDestroyed( ServletContextEvent sce )
    {
        ServletContext sc = sce.getServletContext();
        Application app = (Application) sc.getAttribute(getClass().getName());

        terminate();

        // remove all the references to the application to help garbage-collect it :)
        sc.setAttribute(getClass().getName(), null);
        log.info("****************************************************************************************************************************\n\n");
    }

    //
    private void initialize()
    {
        log.debug("Initializing the application...");
   //   for ( HashMap<String,ApplicationComponent>.Entry c : components ) {
   //
   //   }
    }

    private void terminate()
    {
        log.debug("Terminating the application...");
    }
}
