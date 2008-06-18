package uk.ac.ebi.ae15.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.ae15.AEInterfaceApplication;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());
    private final String APPLICATION_ATTR_NAME = "uk.ac.ebi.ae15.app.Application";

    public synchronized void contextInitialized( ServletContextEvent sce )
    {
        ServletContext sc = sce.getServletContext();
        log.info("****************************************************************************************************************************");
        log.info("*");
        log.info("*  " + sc.getServletContextName());
        log.info("*");
        log.info("****************************************************************************************************************************");

        // create the application (which hosts all the necessary machinery)
        Application app = newApplication(sc);
        sc.setAttribute(APPLICATION_ATTR_NAME, app);

        app.initializeComponents();
    }

    public synchronized void contextDestroyed( ServletContextEvent sce )
    {
        ServletContext sc = sce.getServletContext();
        Application app = (Application) sc.getAttribute(APPLICATION_ATTR_NAME);

        app.terminateComponents();

        // remove all the references to the application to help garbage-collect it :)
        sc.setAttribute(APPLICATION_ATTR_NAME, null);
        log.info("****************************************************************************************************************************\n\n");
    }

    protected Application newApplication( ServletContext sc )
    {
        return new AEInterfaceApplication(sc);
    }
}
