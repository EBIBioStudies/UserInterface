package uk.ac.ebi.ae15.servlets;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
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

            String url = new StringBuilder("http://www.ebi.ac.uk/").append(path).append(null != queryString ? "?" + queryString : "").toString();
            log.debug("Will access [" + url + "]");

            HttpClient httpClient = new HttpClient();
            GetMethod getMethod = new GetMethod(url);

            try {
                // establish a connection within 5 seconds
                httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

                httpClient.executeMethod(getMethod);

                int responseStatus = getMethod.getStatusCode();
                String contentLength = getMethod.getResponseHeader("Content-Length").getValue();

                log.debug("Response: http status [" + String.valueOf(responseStatus) + "], length [" + contentLength + "]");

                if (0 < Long.parseLong(contentLength) && 200 == responseStatus) {

                    String contentType = getMethod.getResponseHeader("Content-Type").getValue();
                    if (null != contentType) {
                        response.setContentType(contentType);
                    }

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(getMethod.getResponseBodyAsStream()));

                    ServletOutputStream out = response.getOutputStream();

                    String inputLine;
                    while ( (inputLine = in.readLine()) != null ) {
                        out.println(inputLine);
                    }

                    in.close();
                    out.close();
                } else {
                    String err = "Response from [" + url + "] was invalid: http status [" + String.valueOf(responseStatus) + "], length [" + contentLength + "]";
                    log.error(err);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
                }
            } catch ( Throwable x ) {
                log.error("Caught an exception:", x);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, x.getMessage());
            } finally {
                getMethod.releaseConnection();
            }
        }
    }
}
