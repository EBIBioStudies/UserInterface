package uk.ac.ebi.arrayexpress.utils.saxon.search;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryHighlighter
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private IndexEnvironment env;

    public QueryHighlighter( IndexEnvironment env )
    {
        this.env = env;
    }

    public String highlightQuery( Query query, String fieldName, String text, String openMark, String closeMark )
    {
        try {
            SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter(openMark, closeMark);
            Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query, fieldName, this.env.defaultField));
            highlighter.setTextFragmenter(new NullFragmenter());

            String str = highlighter.getBestFragment(this.env.indexAnalyzer, fieldName, text);

            return null != str ? str : text;
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
        return text;
    }
}
