package uk.ac.ebi.microarray.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class ApplicationServlet extends HttpServlet
{
    public Application getApplication()
    {
        return Application.getInstance();
    }

    public ApplicationComponent getComponent(String name)
    {
        return getApplication().getComponent(name);
    }

    public ApplicationPreferences getPreferences()
    {
        return getApplication().getPreferences();
    }

    protected void logRequest(Logger logger, HttpServletRequest request)
    {
        logger.info("Processing request: {}{}",
            request.getRequestURL()
            , null != request.getQueryString() ? "?" + request.getQueryString() : "");
    }
}
