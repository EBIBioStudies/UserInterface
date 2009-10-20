package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.xpath.XPathEvaluator;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathConstants;

public class Indexer
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Configuration config;

    private TransformerFactoryImpl trFactory;

    public Indexer( Configuration config )
    {
        this.config = config;
    }

    public List<NodeInfo> index( String indexId, DocumentInfo document )
    {
        // so how this works?
        // 1. read the configuration and determine the location of index
        // 2. read the document xpath and apply it to the document so we iterate over (capturing NodeInfo)
        // 3. read fields xpath and add fields, applying results to the document
        // 4. commit the index and return

        List<NodeInfo> indexedNodes;

        HierarchicalConfiguration indexConfig = this.config.getIndexConfig(indexId);
        if ( null != indexConfig ) {
            String documentPath = indexConfig.getString("document[@path]");

            try {
                XPath xp = new XPathEvaluator(document.getConfiguration());
                XPathExpression xpe = xp.compile(documentPath);
                List documentNodes = (List)xpe.evaluate(document, XPathConstants.NODESET);
                indexedNodes = new ArrayList<NodeInfo>(documentNodes.size());
                if (null != documentNodes) {
                    for (Object node : documentNodes) {
                        // get all the fields taken care of
                        indexedNodes.add((NodeInfo)node);
                    }
                }
            } catch (XPathExpressionException x) {
                logger.error("Caught an exception:", x);
            }
            
        }
        return null;
    }
}
