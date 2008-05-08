package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

public class AppContextListener implements ServletContextListener {

    // logging macinery
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(AppContextListener.class);

    public synchronized void contextInitialized( ServletContextEvent sce )
    {
        log.info("Starting up ArrayExpress...");
        Application.Instance();

    }

    public synchronized void contextDestroyed( ServletContextEvent sce )
    {
        log.info("Shutting down ArrayExpress...");
    }
}
