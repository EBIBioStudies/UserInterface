package uk.ac.ebi.arrayexpress.app;

import org.slf4j.Logger;

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

    protected enum RequestType {
        HEAD, GET, POST;

        public String toString() {
            switch (this) {
                case HEAD:  return "HEAD";
                case GET:   return "GET";
                case POST:  return "POST";
            }
            throw new AssertionError("Unknown type: " + this);
        }
    }

    protected void logRequest(Logger logger, HttpServletRequest request, RequestType requestType)
    {
        logger.info("Processing {} request: {}"
                , requestType.toString()
                , request.getRequestURL().append(
                        null != request.getQueryString() ? "?" + request.getQueryString() : ""
                )
        );
    }
}
