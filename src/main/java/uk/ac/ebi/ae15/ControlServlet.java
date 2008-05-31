package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
        String command = "";
        String params = "";

        Pattern p = Pattern.compile("servlets/control/([^/]+)/?(.*)");
        Matcher m = p.matcher(request.getRequestURL());
        if ( m.find() )
        {
            command = m.group(1);
            if ( 0 < m.group(2).length() )
                params = m.group(2);
        }

        if ( command.equals("reload-xml") ) {
            if ( 0 == params.length() ) {
                params = "aepub1";
            }
            Application.Experiments().reloadExperiments( params, false );
        } else if ( command.equals("rescan-files") ) {
            if ( 0 < params.length() ) {
                Application.FilesDirectory().setRootFolder(params);
            }
            Application.FilesDirectory().rescan();
        }
    }
}
