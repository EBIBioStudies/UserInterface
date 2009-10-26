package uk.ac.ebi.arrayexpress.utils.saxon.search;

import org.apache.lucene.search.BooleanQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.utils.LRUMap;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class QueryPool
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AtomicInteger queryId;

    public class QueryInfo
    {
        public Map<String,String> queryParams;
        public BooleanQuery parsedQuery;

        public QueryInfo(Map<String,String> queryParams)
        {
            this.queryParams = queryParams;
        }
    }

    private Map<Integer, QueryInfo> queries = Collections.synchronizedMap(new LRUMap<Integer, QueryInfo>(50));

    public QueryPool()
    {
        this.queryId = new AtomicInteger(0);
    }

    public Integer addQuery( String indexId, Map<String, String> queryParams )
    {
        QueryInfo info = new QueryInfo(queryParams);
        info.parsedQuery = Controller.getInstance().constructQuery(indexId, queryParams);
        this.queries.put(this.queryId.addAndGet(1), info);

        return this.queryId.get();
    }

    public QueryInfo getQueryInfo( Integer queryId )
    {
        QueryInfo info = null;

        if (queries.containsKey(queryId)) {
            info = queries.get(queryId);
        }

        return info;
    }
}
