package uk.ac.ebi.ae15.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;

// application-aware servlet

public class ApplicationServlet extends HttpServlet
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    public Application getApplication()
    {
        Application app = (Application) getServletContext().getAttribute("aeApplication");
        if (null == app) {
            log.error("Cannot get application instance from servlet context attributes");
        }
        return app;
    }
}
