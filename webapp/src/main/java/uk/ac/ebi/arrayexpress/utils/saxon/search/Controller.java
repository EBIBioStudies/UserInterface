package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;
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

    private Configuration config;
    private QueryPool queryPool;

    private Map<String, IndexEnvironment> environment = new HashMap<String, IndexEnvironment>();

    public Controller( URL configFile )
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

    public Integer addQuery( String indexId, Map<String, String> queryParams )
    {
        return queryPool.addQuery(new QueryConstructor(getEnvironment(indexId)), queryParams);
    }

    public List<NodeInfo> queryIndex( String indexId, Integer queryId )
    {
        return new Querier(getEnvironment(indexId)).query(queryPool.getQueryInfo(queryId).parsedQuery);
    }

    public String highlightQuery( String indexId, Integer queryId, String fieldName, String text, String openMark, String closeMark )
    {
        return new Querier(getEnvironment(indexId)).highlightQuery(queryPool.getQueryInfo(queryId).parsedQuery, fieldName, text, openMark, closeMark);
    }
}
