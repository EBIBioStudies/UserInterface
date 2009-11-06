package uk.ac.ebi.arrayexpress.utils.saxon.search;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class QueryConstructor
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private IndexEnvironment env;

    public QueryConstructor( IndexEnvironment env )
    {
        this.env = env;
    }

    public BooleanQuery construct( Map<String, String> querySource )
    {
        BooleanQuery result = new BooleanQuery();
        try {
            for ( Map.Entry<String, String> queryItem : querySource.entrySet() ) {
                if (env.fields.containsKey(queryItem.getKey()) && !"".equals(queryItem.getValue().trim())) {
                    QueryParser parser = new NumericRangeQueryParser(env, queryItem.getKey(), this.env.indexAnalyzer);
                    parser.setDefaultOperator(QueryParser.Operator.AND);
                    Query q = parser.parse(queryItem.getValue());
                    result.add(q, BooleanClause.Occur.MUST);
                }
            }
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
        return result;    
    }
}
