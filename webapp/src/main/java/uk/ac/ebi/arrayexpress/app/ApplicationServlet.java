package uk.ac.ebi.arrayexpress.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class ApplicationServlet extends HttpServlet
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Application getApplication()
    {
        return Application.getInstance();
    }

    public ApplicationComponent getComponent( String name )
    {
        return getApplication().getComponent(name);
    }

    public ApplicationPreferences getPreferences()
    {
        return getApplication().getPreferences();
    }

    protected enum RequestType
    {
        HEAD, GET, POST;

        public String toString()
        {
            switch (this) {
                case HEAD:
                    return "HEAD";
                case GET:
                    return "GET";
                case POST:
                    return "POST";
            }
            throw new AssertionError("Unknown type: " + this);
        }
    }

    @Override
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        processRequest(request, response, RequestType.GET);
    }

    @Override
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        processRequest(request, response, RequestType.POST);
    }

    @Override
    public void doHead( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        processRequest(request, response, RequestType.HEAD);
    }

    private void processRequest( HttpServletRequest request, HttpServletResponse response, RequestType requestType )
            throws ServletException, IOException
    {
        try {
            if (canAcceptRequest(request, requestType)) {
                doRequest(request, response, requestType);
            } else {
                logger.error("Request of type [{}] is unsupported", requestType.toString());
                response.sendError(405, "The requested method HEAD is not allowed here");
            }
        } catch (Throwable x) {
            logger.error("[SEVERE] Runtime error while processing request:", x);
            getApplication().sendExceptionReport(
                    "[SEVERE] Runtime error while processing " + requestToString(request, requestType)
                    , x
            );
            response.sendError(500);

        }
    }

    protected abstract boolean canAcceptRequest( HttpServletRequest request, RequestType requestType );

    protected abstract void doRequest( HttpServletRequest request, HttpServletResponse response, RequestType requestType )
            throws ServletException, IOException;

    protected void logRequest( Logger logger, HttpServletRequest request, RequestType requestType )
    {
        logger.info("Processing {}", requestToString(request, requestType));
    }

    protected String requestToString( HttpServletRequest request, RequestType requestType )
    {
        return "["
                + requestType.toString()
                + "] request ["
                + request.getRequestURL().append(
                    null != request.getQueryString() ? "?" + request.getQueryString() : ""
                )
                + "]";
    }

}
