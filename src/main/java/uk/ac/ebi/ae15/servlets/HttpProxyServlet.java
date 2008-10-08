package uk.ac.ebi.ae15.servlets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.ae15.app.ApplicationServlet;
import uk.ac.ebi.ae15.utils.RegExpHelper;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpProxyServlet extends ApplicationServlet
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    // Respond to HTTP GET requests from browsers.
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        logRequest(request);

        String path = new RegExpHelper("servlets/proxy/(.+)", "i")
                .matchFirst(request.getRequestURL().toString());
        String queryString = request.getQueryString();

        if (0 < path.length()) {
            try {
                URL url = new URL("http://www.ebi.ac.uk/" + path + (null != queryString ? "?" + queryString : ""));
                log.debug("Will access [" + url.toString() + "]");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(true);
                conn.connect();
                int responseStatus = conn.getResponseCode();
                int contentLength = conn.getContentLength();

                log.debug("Response: http status [" + String.valueOf(responseStatus) + "], length [" + String.valueOf(contentLength) + "]");

                if (0 < contentLength && 200 == responseStatus) {

                    String contentType = conn.getContentType();
                    if (null != contentType) {
                        response.setContentType(contentType);
                    }

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));

                    ServletOutputStream out = response.getOutputStream();

                    String inputLine;
                    while ( (inputLine = in.readLine()) != null ) {
                        out.println(inputLine);
                    }

                    in.close();
                    out.close();
                } else {
                    String err = "Response from [" + url.toString() + "] was invalid: http status [" + String.valueOf(responseStatus) + "], length [" + String.valueOf(contentLength) + "]";
                    log.error(err);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
                }

            } catch ( Exception e ) {
                log.error("Caught an exception:", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }
}
