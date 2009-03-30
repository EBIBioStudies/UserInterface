package uk.ac.ebi.arrayexpress.servlets;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import uk.ac.ebi.arrayexpress.app.ApplicationServlet;
import uk.ac.ebi.arrayexpress.utils.RegExpHelper;

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
                Header contentLength = getMethod.getResponseHeader("Content-Length");

                log.debug("Response: http status [" + String.valueOf(responseStatus) + "], length [" + (null != contentLength ? contentLength.getValue() : "null") + "]");

                if (null != contentLength && 0 < Long.parseLong(contentLength.getValue()) && 200 == responseStatus) {

                    Header contentType = getMethod.getResponseHeader("Content-Type");
                    if (null != contentType) {
                        response.setContentType(contentType.getValue());
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
                    String err = "Response from [" + url + "] was invalid: http status [" + String.valueOf(responseStatus) + "], length [" + (null == contentLength ? "null" : contentLength.getValue())  + "]";
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
