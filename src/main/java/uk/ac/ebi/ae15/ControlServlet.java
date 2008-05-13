package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

public class ControlServlet extends HttpServlet {

    // logging macinery
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(ControlServlet.class);

    // Respond to HTTP GET requests from browsers.
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        log.debug(
            new StringBuilder("Processing request: ")
                .append(request.getRequestURL())
                .append("?")
                .append(request.getQueryString())
        );

        String dsName = request.getParameter("ds");
        if ( null == dsName ) {
            dsName = "aepub1";
        }
        Application.Experiments().reloadExperiments( dsName, false );
    }
}
