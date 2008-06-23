package uk.ac.ebi.ae15.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

public class ApplicationServlet extends HttpServlet
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    public Application getApplication()
    {
        ServletContext servletContext = getServletContext();
        Object app = servletContext.getAttribute(Application.class.getName());
        if (null == app) {
            log.error("Cannot get application instance from servlet context attributes");
        }
        return (Application) app;
    }

    public ApplicationComponent getComponent( String name )
    {
        return getApplication().getComponent(name);
    }

    public ApplicationPreferences getPreferences()
    {
        return getApplication().getPreferences();
    }
}
