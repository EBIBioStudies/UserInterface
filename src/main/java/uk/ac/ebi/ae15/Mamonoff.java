package uk.ac.ebi.ae15;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;

/*

 Mamonoff is a MicroArray Monitor ON-OFF, and, in fact, a very simple class
 that catches webapp startup and shutdown events and then kicks a configured
 shell script per such event

 */
public class Mamonoff implements ServletContextListener {

private static final Log log = org.apache.commons.logging.LogFactory.getLog(Mamonoff.class);

    public synchronized void contextInitialized( ServletContextEvent sce )
    {
        log.info("context initialized");
    }


    public synchronized void contextDestroyed( ServletContextEvent sce )
    {
        log.info("context destroyed");
    }

}
