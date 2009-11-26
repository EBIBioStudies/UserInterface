package uk.ac.ebi.arrayexpress.utils.search;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IQueryExpander;

import java.util.Map;

public final class EFOQueryExpander implements IQueryExpander
{

    public final class QueryTag
    {
        public static final String SYNONYM = "Synonym";
        public static final String EFO = "EFO";
    }

    public Query expandQuery( Query originalQuery, Map<String, String> queryParams )
    {
        boolean shouldExpandEfo = "true".equals(queryParams.get("expandefo"));
        
        return expand(originalQuery, shouldExpandEfo);
    }

    private Query expand( Query query, boolean shouldExpandEfo )
    {
        Query result;

        if (query instanceof BooleanQuery) {
            result = new BooleanQuery();

            BooleanClause[] clauses = ((BooleanQuery)query).getClauses();
            for (BooleanClause c : clauses) {
                ((BooleanQuery)result).add(
                        expand(c.getQuery(), shouldExpandEfo)
                        , c.getOccur()
                        );
            }
        } else {
            result = doExpand(query, shouldExpandEfo);
        }
        return result;
    }

    private Query doExpand( Query query, boolean shouldExpandEfo )
    {
        return query;
    }
}
