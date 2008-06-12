package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;

public class AppContextListener implements ServletContextListener {

    public synchronized void contextInitialized( ServletContextEvent sce )
    {
        ServletContext sc = sce.getServletContext();
        log.info( "************************************************************************" );
        log.info( "*" );
        log.info( "*  " + sc.getServletContextName() );
        log.info( "*" );
        log.info( "************************************************************************" );

        // creates the application (which hosts all the necessary machinery)
        Application app = new Application(sc);
        sc.setAttribute("aeApplication", app);
    }

    public synchronized void contextDestroyed( ServletContextEvent sce )
    {
        ServletContext sc = sce.getServletContext();
        sc.setAttribute("aeApplication", null);
        log.info( "************************************************************************" );
    }

    // logging macinery
    private final Log log = LogFactory.getLog(getClass());
}
