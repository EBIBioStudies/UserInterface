package uk.ac.ebi.arrayexpress.utils.saxon.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.Version;

public class NumericRangeQueryParser extends QueryParser
{
    private IndexEnvironment env;

    public NumericRangeQueryParser( IndexEnvironment env, String f, Analyzer a )
    {
        super(Version.LUCENE_29, f, a);
        this.env = env;
    }

    protected Query getRangeQuery( String field,
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
                    parseLong(query.getLowerTerm()),
                    parseLong(query.getUpperTerm()),
                    query.includesLower(),
                    query.includesUpper());
        } else {
            return query;
        }
    }

    protected Query getFieldQuery( String field, String queryText, int slop ) throws ParseException
    {
        Query query = super.getFieldQuery(field, queryText, slop);
        if (env.fields.containsKey(field) && "integer".equals(env.fields.get(field).type)) {
            return NumericRangeQuery.newLongRange(
                    field,
                    parseLong(queryText),
                    parseLong(queryText),
                    true,
                    true);
        } else {
            return query;
        }
    }

    protected Query getFieldQuery( String field, String queryText ) throws ParseException
    {
        Query query = super.getFieldQuery(field, queryText);
        if (env.fields.containsKey(field) && "integer".equals(env.fields.get(field).type)) {
            return NumericRangeQuery.newLongRange(
                    field,
                    parseLong(queryText),
                    parseLong(queryText),
                    true,
                    true);
        } else {
            return query;
        }
    }

    private Long parseLong( String text ) throws ParseException
    {
        Long value;

        try {
            value = Long.parseLong(text);
        } catch (NumberFormatException x) {
            throw new ParseException(x.getMessage());
        }

        return value;
    }
}
