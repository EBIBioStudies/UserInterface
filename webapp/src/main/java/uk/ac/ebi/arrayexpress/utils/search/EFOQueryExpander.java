package uk.ac.ebi.arrayexpress.utils.search;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IQueryExpander;
import uk.ac.ebi.arrayexpress.utils.saxon.search.TaggedBooleanQuery;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        Query result = query;

        // todo: that's test, test! ))
        Set<Term> terms = new HashSet<Term>();
        query.extractTerms(terms);

        for (Term t : terms) {
            if (("keywords".equals(t.field()))) {
                if ("tumor".equals(t.text())) {
                    BooleanQuery b = new BooleanQuery();
                    b.add(query, BooleanClause.Occur.SHOULD);
                    //

                    TaggedBooleanQuery syns = new TaggedBooleanQuery(QueryTag.SYNONYM);
                    syns.add(new TermQuery(new Term(t.field(), "tumour")), BooleanClause.Occur.SHOULD);
                    b.add(syns, BooleanClause.Occur.SHOULD);
                    return b;
                }
            }
        }
        return result;
    }
}
