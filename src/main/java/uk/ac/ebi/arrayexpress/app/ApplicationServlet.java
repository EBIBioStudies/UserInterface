package uk.ac.ebi.arrayexpress.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class ApplicationServlet extends HttpServlet
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

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

    protected void logRequest(HttpServletRequest request)
    {
        logger.info("Processing request: {}{}",
            request.getRequestURL()
            , null != request.getQueryString() ? "?" + request.getQueryString() : "");
    }
}
