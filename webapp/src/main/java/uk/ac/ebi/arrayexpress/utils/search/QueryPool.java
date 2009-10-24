package uk.ac.ebi.arrayexpress.utils.search;

import org.apache.lucene.search.BooleanQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.utils.LRUMap;
import uk.ac.ebi.arrayexpress.utils.saxon.search.Controller;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class QueryPool
{
    // there should be a single instance of QueryPool in the runtime environment
    private static QueryPool self;

    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AtomicInteger queryKey;

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

    private QueryPool()
    {
        this.queryKey = new AtomicInteger(0);
    }

    public Integer addQuery( String indexId, Map<String, String> queryParams )
    {
        QueryInfo info = new QueryInfo(queryParams);
        info.parsedQuery = Controller.getController().constructQuery(indexId, queryParams);
        this.queries.put(this.queryKey.addAndGet(1), info);

        return this.queryKey.get();
    }

    public QueryInfo getQueryInfo( Integer queryKey )
    {
        QueryInfo info = null;

        if (queries.containsKey(queryKey)) {
            info = queries.get(queryKey);
        }

        return info;
    }

    public static QueryPool getInstance()
    {
        if (null == self) {
            self = new QueryPool();
        }
        return self;
    }
}
