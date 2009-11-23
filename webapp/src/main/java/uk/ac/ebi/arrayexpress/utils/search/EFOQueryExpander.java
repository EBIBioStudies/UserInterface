package uk.ac.ebi.arrayexpress.utils.search;

import org.apache.lucene.search.Query;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IQueryExpander;

import java.util.Map;

public final class EFOQueryExpander implements IQueryExpander
{
    public Query expandQuery( Query originalQuery, Map<String, String> queryParams )
    {
        return originalQuery;
    }
}
