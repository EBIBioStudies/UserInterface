package uk.ac.ebi.ae15.servlets;

import uk.ac.ebi.ae15.app.ApplicationServlet;
import uk.ac.ebi.ae15.components.Experiments;
import uk.ac.ebi.ae15.components.Users;
import uk.ac.ebi.ae15.components.XsltHelper;
import uk.ac.ebi.ae15.utils.CookieMap;
import uk.ac.ebi.ae15.utils.HttpServletRequestParameterMap;
import uk.ac.ebi.ae15.utils.RegExpHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QueryServlet extends ApplicationServlet
{
    // Respond to HTTP GET requests from browsers.
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        logRequest(request);

        String type = "xml";
        String stylesheet = "default";

        String[] requestArgs = new RegExpHelper("servlets/query/([^/]+)/?([^/]*)", "i")
                .match(request.getRequestURL().toString());
        if (null != requestArgs) {
            if (!requestArgs[0].equals("")) {
                stylesheet = requestArgs[0];
            }
            if (!requestArgs[1].equals("")) {
                type = requestArgs[1];
            }
        }

        if (type.equals("xls")) {
            // special case for Excel docs
            // we actually send tab-delimited file but mimick it as XLS doc
            String timestamp = new SimpleDateFormat("yyMMdd-HHmmss").format(new Date());
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment; filename=\"ArrayExpress-Experiments-" + timestamp + ".xls\"");
            type = "tab";
        } else if (type.equals("tab")) {
            // special case for tab-delimited files
            // we send tab-delimited file as an attachment
            String timestamp = new SimpleDateFormat("yyMMdd-HHmmss").format(new Date());
            response.setContentType("text/plain; charset=ISO-8859-1");
            response.setHeader("Content-disposition", "attachment; filename=\"ArrayExpress-Experiments-" + timestamp + ".txt\"");
            type = "tab";
        } else {
            // Set content type for HTML/XML/plain
            response.setContentType("text/" + type + "; charset=ISO-8859-1");
        }
        // Disable cache no matter what (or we're fucked on IE side)
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "must-revalidate");
        response.addHeader("Expires", "Fri, 16 May 2008 10:00:00 GMT"); // some date in the past

        // Output goes to the response PrintWriter.
        PrintWriter out = response.getWriter();
        Experiments experiments = (Experiments) getComponent("Experiments");
        if (stylesheet.equals("arrays-select")) {
            out.print(experiments.getArrays());
        } else if (stylesheet.equals("species-select")) {
            out.print(experiments.getSpecies());
        } else if (stylesheet.equals("exptypes-select")) {
            out.print(experiments.getExperimentTypes());
        } else {
            String stylesheetName = new StringBuilder(stylesheet).append('-').append(type).append(".xsl").toString();

            HttpServletRequestParameterMap params = new HttpServletRequestParameterMap(request);
            // to make sure nobody sneaks in the other value w/o proper authentication
            params.put("userid", "1");

            CookieMap cookies = new CookieMap(request.getCookies());
            if (cookies.containsKey("AeLoggedUser") && cookies.containsKey("AeLoginToken")) {
                Users users = (Users) getComponent("Users");
                String user = cookies.get("AeLoggedUser").getValue();
                String passwordHash = cookies.get("AeLoginToken").getValue();
                if (users.verifyLogin(user, passwordHash, request.getRemoteAddr().concat(request.getHeader("User-Agent")))) {
                    params.put("userid", String.valueOf(users.getUserRecord(user).getId()));
                }
            }

            XsltHelper xsltHelper = (XsltHelper) getComponent("XsltHelper");
            if (!xsltHelper.transformDocumentToPrintWriter(
                    experiments.getExperiments(),
                    stylesheetName,
                    params,
                    out)) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        out.close();
    }
}

