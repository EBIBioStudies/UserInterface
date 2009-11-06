package uk.ac.ebi.arrayexpress.utils.saxon.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;

public class NumericRangeQueryParser extends QueryParser
{
    private IndexEnvironment env;

    public NumericRangeQueryParser( IndexEnvironment env, String f, Analyzer a )
    {
        super(f, a);
        this.env = env;
    }
    
    public Query getRangeQuery( String field,
                                String part1,
                                String part2,
                                boolean inclusive )
            throws ParseException
    {
        TermRangeQuery query = (TermRangeQuery)
                super.getRangeQuery(field, part1, part2,
                        inclusive);
        if (env.fields.containsKey(field) && "integer".equals(env.fields.get(field).type)) {
            return NumericRangeQuery.newLongRange(
                    field,
                    Long.parseLong(
                            query.getLowerTerm()),
                    Long.parseLong(
                            query.getUpperTerm()),
                    query.includesLower(),
                    query.includesUpper());
        } else {
            return query;
        }
    }

}
