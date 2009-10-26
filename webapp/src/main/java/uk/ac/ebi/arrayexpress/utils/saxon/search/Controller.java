package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;
import org.apache.lucene.search.BooleanQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // there should be a single instance of Controller in the runtime environment
    private static Controller self;

    private Configuration config;
    private QueryPool queryPool;

    private Map<String, IndexEnvironment> environment = new HashMap<String, IndexEnvironment>();

    private Controller( URL configFile )
    {
        this.config = new Configuration(configFile);
        this.queryPool = new QueryPool();
    }

    public IndexEnvironment getEnvironment( String indexId )
    {
        if (!this.environment.containsKey(indexId)) {
            this.environment.put(indexId, new IndexEnvironment(config.getIndexConfig(indexId)));
        }

        return this.environment.get(indexId);
    }

    public void index( String indexId, DocumentInfo document )
    {
        logger.info("Started indexing for index id [{}]", indexId);
        getEnvironment(indexId).putDocumentInfo(
                document.hashCode()
                , new Indexer(getEnvironment(indexId)).index(document)
        );
        logger.info("Indexing for index id [{}] completed", indexId);
    }

    public List<String> getTerms( String indexId, String fieldName )
    {
        return new Querier(getEnvironment(indexId)).getTerms(fieldName);
    }

    public BooleanQuery constructQuery( String indexId, Map<String, String> querySource )
    {
        return new QueryConstructor(getEnvironment(indexId)).construct(querySource);
    }

    public Integer addQuery( String indexId, Map<String, String> queryParams )
    {
        return queryPool.addQuery(indexId, queryParams);
    }

    public List<NodeInfo> queryIndex( String indexId, Integer queryId )
    {
        return new Querier(getEnvironment(indexId)).query(queryPool.getQueryInfo(queryId).parsedQuery);
    }

    public String highlightQuery( String indexId, Integer queryId, String text, String openMark, String closeMark )
    {
        return new Querier(getEnvironment(indexId)).highlightQuery(queryPool.getQueryInfo(queryId).parsedQuery, text, openMark, closeMark);
    }

    public static Controller getController( URL configFile )
    {
        if (null == self) {
            self = new Controller(configFile);
        }

        return self;
    }

    public static Controller getInstance()
    {
        return self;
    }
}
