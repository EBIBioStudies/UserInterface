package uk.ac.ebi.arrayexpress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.components.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.MalformedURLException;
import java.net.URL;

public class AEInterfaceApplication extends Application implements ServletContextListener
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ServletContext servletContext;

    public AEInterfaceApplication()
    {
        super("arrayexpress");

        addComponent(new SaxonEngine());
        addComponent(new SearchEngine());
        addComponent(new Experiments());
        addComponent(new Users());
        addComponent(new Files());
        addComponent(new JobsController());
        addComponent(new Ontologies());
    }

    public URL getResource( String path ) throws MalformedURLException
    {
        return null != servletContext ? servletContext.getResource(path) : null;
    }


    public synchronized void contextInitialized( ServletContextEvent sce )
    {
        servletContext = sce.getServletContext();

        logger.info("****************************************************************************************************************************");
        logger.info("*");
        logger.info("*  {}", servletContext.getServletContextName());
        logger.info("*");
        logger.info("****************************************************************************************************************************");

        // re-route all subsequent java.util.logging calls via slf4j
        SLF4JBridgeHandler.install();

        initialize();
    }

    public synchronized void contextDestroyed( ServletContextEvent sce )
    {
        terminate();

        // restore java.util.logging calls to the original state
        SLF4JBridgeHandler.uninstall();

        servletContext = null;

        logger.info("****************************************************************************************************************************\n\n");
    }
}
