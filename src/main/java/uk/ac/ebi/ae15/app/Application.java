package uk.ac.ebi.ae15.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;

abstract public class Application
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private ServletContext servletContext;

    public Application(ServletContext context)
    {
        servletContext = context;
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }

    abstract public void initializeComponents();
    abstract public void terminateComponents();
}
