package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Map;

public abstract class XsltHelper {
    // logging macinery
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(XsltHelper.class);

    public synchronized static boolean transformDocumentToFile( Document srcDocument, String stylesheet, Map<String,String[]> params, File dstFile )
    {
        try {
            return transform( new DOMSource(srcDocument), stylesheet, params, new StreamResult( new FileOutputStream(dstFile) ));
        } catch ( Throwable x ) {
            log.debug( "Caught an exceptiom:", x );
            //TODO: error logging
        }

        return false;
    }

    public synchronized static Document transformStringToDocument( String srcXmlString, String stylesheet, Map<String,String[]> params )
    {
        try {
            InputStream inStream = new ByteArrayInputStream(srcXmlString.getBytes("ISO-8859-1"));
            DOMResult dst = new DOMResult();
            if ( transform( new StreamSource(inStream), stylesheet, params, dst ) ) {
                if ( null != dst.getNode() ) {
                    return (Document)dst.getNode();
                }
            }
        } catch ( Throwable x ) {
            log.debug( "Caught an exception:", x );
        }
        return null;
    }

    public synchronized static boolean transformDocumentToPrintWriter( Document srcDocument, String stylesheet, Map<String,String[]> params, PrintWriter dstWriter )
    {
        return transform( new DOMSource(srcDocument), stylesheet, params, new StreamResult(dstWriter) );
    }

    private static boolean transform( Source src, String stylesheet, Map<String,String[]> params, Result dst )
    {
        boolean result = false;
        try {
            // create a transformer factory if null
            if ( null == tFactory ) {
                tFactory = TransformerFactory.newInstance();
                tFactory.setURIResolver( new AppURIResolver() );

            }

            // Open the stylesheet
            Source xslSource = new StreamSource(
                    Application.Instance().ServletContext().getResource( "/WEB-INF/server-assets/stylesheets/" + stylesheet ).openStream()
            );

            // Generate the transformer.
            Transformer transformer = tFactory.newTransformer(xslSource);

            // assign the parameters (if not null)
            if ( null != params ) {
                for ( Map.Entry<String, String[]> param : params.entrySet() ) {
                    transformer.setParameter( param.getKey(), arrayToString( param.getValue() ));
                }
            }

            // Perform the transformation, sending the output to the response.
            log.debug("about to start transformer.transform() with stylesheet [" + stylesheet + "]");
            transformer.transform(src, dst);
            log.debug("transformer.transform() completed");

            result = true;
        } catch ( Throwable x ) {
            log.debug( "Caught an exception:", x );
            log.error( "There was an [" + x.getClass().getName() + "] transforming with [" + stylesheet + "]: " + x.getMessage() );
        }
        return result;
    }

    private static String arrayToString( String[] array )
    {
        StringBuilder sb = new StringBuilder();
        for ( String item : array ) {
            sb.append(item).append(' ');
        }
        return sb.toString().trim();
    }

    private static TransformerFactory tFactory = null;
}

class AppURIResolver implements URIResolver {

    // logging macinery
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(AppURIResolver.class);

    public Source resolve( String href, String base ) throws TransformerException
    {
        Source src;
        try {
            src = new StreamSource(
                    Application.Instance().ServletContext().getResource( "/WEB-INF/server-assets/stylesheets/" + href ).openStream()
            );
        } catch ( Exception x ) {
            log.debug( "Caught an exception:", x );
            throw new TransformerException(x.getMessage());
        }

    return src;
    }
}