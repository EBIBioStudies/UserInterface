package uk.ac.ebi.arrayexpress.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.util.Map;

public class SaxonEngine extends ApplicationComponent implements URIResolver, ErrorListener
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private TransformerFactory tFactory;

    public SaxonEngine( Application app )
    {
        super(app, "SaxonEngine");
    }

    public void initialize()
    {
    }

    public void terminate()
    {
    }

    // implements URIResolver.resolve
    public Source resolve( String href, String base ) throws TransformerException
    {
        Source src;
        try {
            URL resource = getApplication().getResource("/WEB-INF/server-assets/stylesheets/" + href);
            if (null == resource) {
                throw new TransformerException("Unable to locate stylesheet resource [" + href + "]");
            }
            InputStream input = resource.openStream();
            if (null == input) {
                throw new TransformerException("Unable to open stream for resource [" + resource + "]");
            }
            src = new StreamSource(input);
        } catch ( TransformerException x ) {
            throw x;
        } catch ( Exception x ) {
            log.error("Caught an exception:", x);
            throw new TransformerException(x.getMessage());
        }

        return src;
    }

    // implements ErrorListener.error
    public void error( TransformerException x ) throws TransformerException
    {
        throw x;
    }

    // implements ErrorListener.fatalError
    public void fatalError( TransformerException x ) throws TransformerException
    {
        throw x;
    }

    // implements ErrorListenet.warning
    public void	warning( TransformerException x )
    {
        log.warn("There was a warning while transforming:", x);
    }

    public boolean transformDocumentToFile( Document srcDocument, String stylesheet, HttpServletRequestParameterMap params, File dstFile )
    {
        try {
            return transform(new DOMSource(srcDocument), stylesheet, params, new StreamResult(new FileOutputStream(dstFile)));
        } catch ( Throwable x ) {
            log.error("Caught an exceptiom:", x);
        }

        return false;
    }

    public String transformDocumentToString( Document srcDocument, String stylesheet, HttpServletRequestParameterMap params )
    {
        try {
            StringWriter sw = new StringWriter();
            if (transform(new DOMSource(srcDocument), stylesheet, params, new StreamResult(sw))) {
                return sw.toString();
            }
        } catch ( Throwable x ) {
            log.error("Caught an exceptiom:", x);
        }

        return null;
    }

    public Document transformStringToDocument( String srcXmlString, String stylesheet, HttpServletRequestParameterMap params )
    {
        try {
            InputStream inStream = new ByteArrayInputStream(srcXmlString.getBytes("ISO-8859-1"));
            DOMResult dst = new DOMResult();
            if (transform(new StreamSource(inStream), stylesheet, params, dst)) {
                if (null != dst.getNode()) {
                    return (Document) dst.getNode();
                }
            }
        } catch ( Throwable x ) {
            log.error("Caught an exception:", x);
        }
        return null;
    }

    public Document transformDocument( Document srcDocument, String stylesheet, HttpServletRequestParameterMap params )
    {
        try {
            DOMResult dst = new DOMResult();
            if (transform(new DOMSource(srcDocument), stylesheet, params, dst)) {
                if (null != dst.getNode()) {
                    return (Document) dst.getNode();
                }
            }
        } catch ( Throwable x ) {
            log.error("Caught an exceptiom:", x);
        }

        return null;
    }

    public boolean transformDocumentToPrintWriter( Document srcDocument, String stylesheet, HttpServletRequestParameterMap params, PrintWriter dstWriter )
    {
        return transform(new DOMSource(srcDocument), stylesheet, params, new StreamResult(dstWriter));
    }

    private boolean transform( Source src, String stylesheet, HttpServletRequestParameterMap params, Result dst )
    {
        boolean result = false;
        try {
            // create a transformer factory if null
            if (null == tFactory) {
                tFactory = TransformerFactory.newInstance();
                tFactory.setURIResolver(this);
            }

            // Open the stylesheet
            Source xslSource = resolve(stylesheet, null);

            // Generate the transformer.
            Transformer transformer = tFactory.newTransformer(xslSource);

            // assign the parameters (if not null)
            if (null != params) {
                for ( Map.Entry<String, String> param : params.entrySet() ) {
                    transformer.setParameter(param.getKey(), param.getValue());
                }
            }

            // set the error event listener so we catch problems and don't generate result
            transformer.setErrorListener(this);
            // Perform the transformation, sending the output to the response.
            log.debug("about to start transformer.transform() with stylesheet [" + stylesheet + "]");
            transformer.transform(src, dst);
            log.debug("transformer.transform() completed");

            result = true;
        } catch ( Throwable x ) {
            if (x.getMessage().contains("java.lang.InterruptedException")) {
                log.error("Transformation has been interruped");

            } else {
                log.error("Caught an exception transforming [" + stylesheet + "]:", x);
            }
        }
        return result;
    }
}
