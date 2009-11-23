package uk.ac.ebi.arrayexpress.utils.saxon.search;

import org.apache.lucene.search.Query;

import java.util.Map;

public interface IQueryExpander
{
    public Query expandQuery( Query originalQuery, Map<String, String> queryParams );
}
