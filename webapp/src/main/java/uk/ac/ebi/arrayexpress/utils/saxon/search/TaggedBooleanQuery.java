package uk.ac.ebi.arrayexpress.utils.saxon.search;

import org.apache.lucene.search.BooleanQuery;

public class TaggedBooleanQuery extends BooleanQuery
{
    private String tag;

    public TaggedBooleanQuery( String tag )
    {
        super();
        this.tag = tag;
    }

    public String getTag()
    {
        return tag;
    }
}
