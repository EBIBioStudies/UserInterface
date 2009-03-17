package uk.ac.ebi.arrayexpress.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class ApplicationServlet extends HttpServlet
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

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
        log.info(
                new StringBuilder("Processing request: ")
                        .append(request.getRequestURL())
                        .append(null != request.getQueryString() ? "?" + request.getQueryString() : "")
                        .toString()
        );
    }
}
