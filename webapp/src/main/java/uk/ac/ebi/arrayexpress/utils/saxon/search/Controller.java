package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;

import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Controller
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // there should be a single instance of Controller in the runtime environment
    private static Controller self;

    private Configuration config;

    private class IndexInfo
    {
        public int documentHashCode;
        public List<NodeInfo> documentIndexedNodes;

        public IndexInfo( int hashCode, List<NodeInfo> indexedNodes )
        {
            this.documentHashCode = hashCode;
            this.documentIndexedNodes = indexedNodes;
        }
    }

    private Map<String, IndexInfo> indicesInfo = new HashMap<String, IndexInfo>();

    private Controller( URL configFile )
    {
        this.config = new Configuration(configFile);
    }

    public void index( String indexId, DocumentInfo document )
    {
        logger.info("Started indexing for index id [{}]", indexId);
        this.indicesInfo.put(indexId, new IndexInfo(document.hashCode(), new Indexer(config).index(indexId, document)));
        logger.info("Indexing for index id [{}] completed", indexId);
    }

    public List<String> getTerms( String indexId, String fieldName )
    {
        return new Querier(config).getTerms(indexId, fieldName);
    }

    public static Controller getController( URL configFile )
    {
        if (null == self) {
            self = new Controller(configFile);
        }

        return self;
    }
}
