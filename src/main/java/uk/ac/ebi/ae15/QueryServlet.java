package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.*;

public class QueryServlet extends HttpServlet {

    // logging macinery
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(QueryServlet.class);

    // Respond to HTTP GET requests from browsers.
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        log.debug(
            new StringBuilder("Processing request: ")
                .append(request.getRequestURL())
                .append("?")
                .append(request.getQueryString())
        );


        String type = "xml";
        String stylesheet = "default";

        Pattern p = Pattern.compile("servlets/query/([^/]+)/?([^/]*)");
        Matcher m = p.matcher(request.getRequestURL());
        if ( m.find() )
        {
            stylesheet = m.group(1);
            if ( 0 < m.group(2).length() )
                type = m.group(2);
        }
        
        // Set content type for HTML/XML
        response.setContentType("text/" + type + "; charset=ISO-8859-1");

        // Disable cache no matter what (or we fucked on IE side)
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "must-revalidate");
        response.addHeader("Expires", "Fri, 16 May 2008 10:00:00 GMT"); // some date in the past

        // Output goes to the response PrintWriter.
        PrintWriter out = response.getWriter();
        if ( stylesheet.equals("arrays-select") ) {
            out.print(Application.Experiments().getArrays());
        } else if ( stylesheet.equals("species-select") ) {
            out.print(Application.Experiments().getSpecies());
        } else {
            String stylesheetName = new StringBuilder(stylesheet).append('-').append(type).append(".xsl").toString();

            if ( !XsltHelper.transformDocumentToPrintWriter( Application.Experiments().getExperiments(), stylesheetName, request.getParameterMap(), out ) ) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        out.close();
    }
}

