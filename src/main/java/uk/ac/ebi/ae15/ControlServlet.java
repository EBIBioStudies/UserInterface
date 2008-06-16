package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ControlServlet extends ApplicationServlet
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

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
        if (m.find()) {
            command = m.group(1);
            if (0 < m.group(2).length())
                params = m.group(2);
        }

        if (command.equals("reload-xml")) {
            if (0 == params.length()) {
                params = "aepub1";
            }
            boolean onlyPublic = getApplication().getPreferences().get("ae.experiments.publiconly").toString().toLowerCase().equals("true");
            getApplication().getExperiments().reloadExperiments(params, onlyPublic);
        } else if (command.equals("rescan-files")) {
            if (0 < params.length()) {
                getApplication().getFilesRegistry().setRootFolder(params);
            }
            // TODO: redo this that is kicks off the scheduler
            // getApplication().getFilesDirectory().rescan();
        }
    }
}
