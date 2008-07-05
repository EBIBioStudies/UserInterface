package uk.ac.ebi.ae15.servlets;

import uk.ac.ebi.ae15.app.ApplicationServlet;
import uk.ac.ebi.ae15.components.DownloadableFilesRegistry;
import uk.ac.ebi.ae15.components.Experiments;
import uk.ac.ebi.ae15.components.JobsController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ControlServlet extends ApplicationServlet
{
    // Respond to HTTP GET requests from browsers.
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        logRequest(request);

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
            if (0 < params.length()) {
                ((Experiments) getComponent("Experiments")).setDataSource(params);
            }
            ((JobsController) getComponent("JobsController")).executeJob("reload-xml");
        } else if (command.equals("rescan-files")) {
            if (0 < params.length()) {
                ((DownloadableFilesRegistry) getComponent("DownloadableFilesRegistry")).setRootFolder(params);
            }
            ((JobsController) getComponent("JobsController")).executeJob("rescan-files");
        }
    }
}
