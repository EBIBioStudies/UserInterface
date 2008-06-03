package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;

public class AppContextListener implements ServletContextListener {

    // logging macinery
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(AppContextListener.class);

    public synchronized void contextInitialized( ServletContextEvent sce )
    {
        log.info("ARRAYEXPRESS 1.1 UP -------------------------------------------------------------------------------");

        // creates the application (which hosts all the necessary machinery)
        new Application(sce.getServletContext());
    }

    public synchronized void contextDestroyed( ServletContextEvent sce )
    {
        log.info("ARRAYEXPRESS GOES DOWN ----------------------------------------------------------------------------");
    }
}
