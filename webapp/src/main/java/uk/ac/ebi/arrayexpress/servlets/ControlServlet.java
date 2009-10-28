package uk.ac.ebi.arrayexpress.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationServlet;
import uk.ac.ebi.arrayexpress.components.Experiments;
import uk.ac.ebi.arrayexpress.components.Files;
import uk.ac.ebi.arrayexpress.components.JobsController;
import uk.ac.ebi.arrayexpress.components.Users;
import uk.ac.ebi.arrayexpress.utils.RegExpHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ControlServlet extends ApplicationServlet
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    // Respond to HTTP GET requests from browsers.
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        logRequest(logger, request);

        String command = "";
        String params = "";

        String[] requestArgs = new RegExpHelper("servlets/control/([^/]+)/?(.*)", "i")
                .match(request.getRequestURL().toString());
        if (null != requestArgs) {
            command = requestArgs[0];
            params = requestArgs[1];
        }

        if (command.equals("reload-atlas-info")) {
            ((JobsController) getComponent("JobsController")).executeJob(command);
        } else if (command.equals("reload-xml")) {
            if (0 < params.length()) {
                ((Experiments) getComponent("Experiments")).setDataSource(params);
            }
            ((JobsController) getComponent("JobsController")).executeJob(command);
        } else if (command.equals("rescan-files")) {
            if (0 < params.length()) {
                ((Files) getComponent("Files")).setRootFolder(params);
            }
            ((JobsController) getComponent("JobsController")).executeJob(command);
        } else if (command.equals("verify-login")) {
            response.setContentType("text/plain; charset=ISO-8859-1");
            // Disable cache no matter what (or we're fucked on IE side)
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
            response.addHeader("Cache-Control", "must-revalidate");
            response.addHeader("Expires", "Fri, 16 May 2008 10:00:00 GMT"); // some date in the past

            // Output goes to the response PrintWriter.
            PrintWriter out = response.getWriter();
            out.print(((Users) getComponent("Users")).hashLogin(
                    request.getParameter("u")
                    , request.getParameter("p")
                    , request.getRemoteAddr().concat(request.getHeader("User-Agent"))
            ));
            out.close();
        }
    }
}
