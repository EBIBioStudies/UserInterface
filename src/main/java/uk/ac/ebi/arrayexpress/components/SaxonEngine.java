package uk.ac.ebi.arrayexpress.components;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.event.SequenceWriter;
import net.sf.saxon.instruct.TerminationException;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.tinytree.TinyBuilder;
import net.sf.saxon.xpath.XPathEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SaxonEngine extends ApplicationComponent implements URIResolver, ErrorListener
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // logging writer for the transformations
    private LoggerWriter loggerWriter;

    TransformerFactoryImpl trFactory;
    private Map<String,Templates> templatesCache = new HashMap<String,Templates>();

    private final String XML_STRING_ENCODING = "ISO-8859-1";

    public SaxonEngine()
    {
        super("SaxonEngine");
    }

    public void initialize()
    {
        try {
            // this is to make sure we don't depend on rutime configuration much
            System.setProperty(
                "javax.xml.transform.TransformerFactory"
                , "net.sf.saxon.TransformerFactoryImpl"
            );
            
            trFactory = (TransformerFactoryImpl)TransformerFactory.newInstance();
            
            trFactory.setErrorListener(this);
            trFactory.setURIResolver(this);
            loggerWriter = new LoggerWriter(logger);
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
    }

    public void terminate()
    {
        loggerWriter = null;
    }

    // implements URIResolver.resolve
    public Source resolve( String href, String base ) throws TransformerException
    {
        Source src;
        try {
            URL resource = Application.getInstance().getResource("/WEB-INF/server-assets/stylesheets/" + href);
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
            logger.error("Caught an exception:", x);
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
        logger.warn("There was a warning while transforming:", x);
    }

    public String serializeDocument( DocumentInfo document )
    {
        String string = null;
        try {
            Transformer transformer = trFactory.newTransformer();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();

            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

            transformer.transform(document, new StreamResult(outStream));
            string = outStream.toString(XML_STRING_ENCODING);
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }

        return string;
    }

    public DocumentInfo buildDocument( String xml )
    {
        StringReader reader = new StringReader(xml);
        DocumentInfo document = null;
        try {
            Configuration config = trFactory.getConfiguration();
            document = config.buildDocument(new StreamSource(reader));
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }

        return document;
    }

    public String evaluateXPathSingle( DocumentInfo doc, String xpath )
    {
        try {
            XPath xp = new XPathEvaluator(trFactory.getConfiguration());
            XPathExpression xpe = xp.compile(xpath);
            return xpe.evaluate(doc);
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }

        return null;
    }
//
//    public XdmValue evaluateXPath( XdmNode node, String xpath )
//    {
//        try {
//            XPathSelector selector = xpathCompiler.compile(xpath).load();
//            selector.setContextItem(node);
//            return selector.evaluate();
//        } catch (Throwable x) {
//            logger.error("Caught an exception:", x);
//        }
//
//        return null;
//    }
//
//    public String concatAllText( XdmValue nodes )
//    {
//        StringBuilder buf = new StringBuilder();
//        if (null != nodes) {
//            for (XdmItem node : nodes) {
//                buf.append(concatAllText((XdmNode)node)).append(' ');
//            }
//        }
//        return buf.toString();
//    }
//
//    public String concatAllText( XdmNode node )
//    {
//        StringBuilder buf = new StringBuilder();
//
//        if (null != node) {
//            AxisIterator nodesItor = node.getUnderlyingNode().iterateAxis(net.sf.saxon.om.Axis.DESCENDANT_OR_SELF);
//            do {
//                NodeInfo next = (NodeInfo)nodesItor.next();
//
//                // if null there is no next
//                if (null == next) {
//                    break;
//                }
//
//                if ( Type.TEXT == next.getNodeKind()) {
//                    if (null != next.getStringValue() && 0 != next.getStringValue().length()) {
//                        buf.append(next.getStringValue()).append(' ');
//                    }
//                } else if (Type.ELEMENT == next.getNodeKind()) {
//                    // iterate over attributes and collect values from there
//                    AxisIterator attributesItor = next.iterateAxis(net.sf.saxon.om.Axis.ATTRIBUTE);
//                    do {
//                        NodeInfo nextAttr = (NodeInfo)attributesItor.next();
//
//                        // if null there is no next
//                        if (null == nextAttr) {
//                            break;
//                        }
//
//                        // append attribute value (if any)
//                        if (null == nextAttr.getStringValue() || 0 != nextAttr.getStringValue().length()) {
//                            buf.append(nextAttr.getStringValue()).append(' ');
//                        }
//
//                    } while (true);
//                }
//
//            } while (true);
//        }
//
//        return buf.toString();
//    }
//
    public boolean transformToWriter( DocumentInfo srcDocument, String stylesheet, Map<String,String> params, Writer dstWriter )
    {
        return transform(srcDocument, stylesheet, params, new StreamResult(dstWriter));
    }

    public boolean transformToFile( DocumentInfo srcDocument, String stylesheet, Map<String,String> params, File dstFile )
    {
        return transform(srcDocument, stylesheet, params, new StreamResult(dstFile));
    }

    public String transformToString( DocumentInfo srcDocument, String stylesheet, Map<String,String> params )
    {
        String str = null;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        if (transform(srcDocument, stylesheet, params, new StreamResult(str))) {
            try {
                str = outStream.toString(XML_STRING_ENCODING);
            } catch (Throwable x) {
                logger.error("Caught an exception:", x);
            }
            return str;
        } else {
            return null;
        }
    }

    public DocumentInfo transform( String srcXmlString, String stylesheet, Map<String,String> params )
    {
        try {
            Source src = new StreamSource(new StringReader(srcXmlString));
            TinyBuilder dstDocument = new TinyBuilder();
            if (transform(src, stylesheet, params, dstDocument)) {
                return (DocumentInfo)dstDocument.getCurrentRoot();
            }
        } catch ( Throwable x ) {
            logger.error("Caught an exception:", x);
        }
        return null;
    }

    public DocumentInfo transform( DocumentInfo srcDocument, String stylesheet, Map<String,String> params )
    {
        try {
            TinyBuilder dstDocument = new TinyBuilder();
            if (transform(srcDocument, stylesheet, params, dstDocument)) {
                return (DocumentInfo)dstDocument.getCurrentRoot();
            }
        } catch ( Throwable x ) {
            logger.error("Caught an exceptiom:", x);
        }

        return null;
    }

    private boolean transform( Source src, String stylesheet, Map<String,String> params, Result dst )
    {
        boolean result = false;
        try {
            Templates templates;
            if (!templatesCache.containsKey(stylesheet)) {
                logger.debug("Caching prepared stylesheet [{}]", stylesheet);
                // Open the stylesheet
                Source xslSource = resolve(stylesheet, null);

                templates = trFactory.newTemplates(xslSource);
                templatesCache.put(stylesheet, templates);
            } else {
                logger.debug("Getting prepared stylesheet [{}] from cache", stylesheet);
                templates = templatesCache.get(stylesheet);
            }
            Transformer xslt = templates.newTransformer();

            // redirect all messages to logger
            ((Controller)xslt).setMessageEmitter(loggerWriter);

            // assign the parameters (if not null)
            if (null != params) {
                for ( Map.Entry<String, String> param : params.entrySet() ) {
                    xslt.setParameter(param.getKey(), param.getValue());
                }
            }

            // Perform the transformation, sending the output to the response.
            logger.debug("about to start transformer.transform() with stylesheet [{}]", stylesheet);
            xslt.transform(src, dst);
            logger.debug("transformer.transform() completed");

            result = true;
        } catch (TerminationException x ) {
            logger.error("Transformation has been terminated by xsl instruction, please inspect log for details");
        } catch ( Throwable x ) {
            if (x.getMessage().contains("java.lang.InterruptedException")) {
                logger.error("Transformation has been interruped");
            } else {
                logger.error("Caught an exception transforming [" + stylesheet + "]:", x);
            }
        }
        return result;
    }

    class LoggerWriter extends SequenceWriter
    {
        private Logger logger;

        public LoggerWriter(Logger logger)
        {
            this.logger = logger;
        }

        public void write(Item item)
        {
            logger.info("[xsl:message] {}", item.getStringValue());
        }
    }
}
