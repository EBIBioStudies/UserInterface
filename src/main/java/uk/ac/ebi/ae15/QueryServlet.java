package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

public class QueryServlet extends HttpServlet {

    // logging macinery
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(QueryServlet.class);

    public final static String FS = System.getProperty("file.separator");

    // Respond to HTTP GET requests from browsers.
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        log.debug(
            new StringBuffer("Processing request: ")
                .append(request.getRequestURL())
                .append("?")
                .append(request.getQueryString())
        );

        String type = request.getParameter("type");
        if ( null == type ) {
            type = "xml";
        }

        String stylesheet = request.getParameter("stylesheet");
        if ( null == stylesheet ) {
            stylesheet = "default";
        }


        // Set content type for HTML.
        response.setContentType("text/" + type + "; charset=ISO-8859-1");
        // Output goes to the response PrintWriter.
        PrintWriter out = response.getWriter();
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            //get the real path for xml and xsl files.
            String ctx = getServletContext().getRealPath("") + FS;
            // Get the XML input document and the stylesheet, both in the servlet
            // engine document directory.
            Source xmlSource = new DOMSource(Application.Experiments().getExperiments());

            Source xslSource = new StreamSource( new java.net.URL("file", "", ctx + "WEB-INF/server-assets/stylesheets/" + stylesheet + "-" + type + ".xsl").openStream() );
            // Generate the transformer.
            Transformer transformer = tFactory.newTransformer(xslSource);

            // Stuff transformer with all the parameters supplied via the request
            Enumeration e = request.getParameterNames();
            while ( e.hasMoreElements() ) {
                String name = (String)e.nextElement();
                transformer.setParameter( name, request.getParameter(name) );
            }

            // Perform the transformation, sending the output to the response.
            transformer.transform(xmlSource, new StreamResult(out));
        }
        // If an Exception occurs, return the error to the client.
        catch ( Exception e ) {
            log.debug( "Caught an exception:", e );
            out.write(e.getMessage());
        }
        // Close the PrintWriter.
        out.close();
    }

}
