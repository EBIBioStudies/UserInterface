package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.om.NodeInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Querier
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private IndexEnvironment env;

    public Querier( IndexEnvironment env )
    {
        this.env = env;
    }

    public List<String> getTerms( String fieldName )
    {
        List<String> termsList = null;
        
        try {
            IndexReader ir = IndexReader.open(this.env.indexDirectory, true);
            TermEnum terms = ir.terms(new Term(fieldName, ""));
            while (fieldName.equals(terms.term().field())) {
                if (null == termsList)
                    termsList = new ArrayList<String>();

                termsList.add(terms.term().text());
            if (!terms.next())
                break;
            }
            // TODO: this should go to 'finally' clause
            terms.close();
            ir.close();
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
        return termsList;
    }

    public List<NodeInfo> query( BooleanQuery query )
    {
        List<NodeInfo> result = null;
        try {
            IndexReader ir = IndexReader.open(this.env.indexDirectory, true);

            // empty query returns everything
            if (query.clauses().isEmpty()) {
                return this.env.documentNodes;
            }

            // to show _all_ available experiments
            IndexSearcher isearcher = new IndexSearcher(ir);
            TopDocs hits = isearcher.search(query, this.env.documentNodes.size());

            result = new ArrayList<NodeInfo>(hits.totalHits);
            for (ScoreDoc d : hits.scoreDocs) {
                result.add(this.env.documentNodes.get(d.doc));
            }

            isearcher.close();
            ir.close();
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }

    return result;
    }

    // query cache
    private final static int QUERY_CACHE_SIZE = 25;
    private static Map<String,Query> queryCache = new HashMap<String,Query>(QUERY_CACHE_SIZE);

    public String highlightQuery(BooleanQuery query, String text)
    {
        String fieldName = "keywords";
        try {
            SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("\u00ab", "\u00bb");
            Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query, fieldName));
            highlighter.setTextFragmenter(new NullFragmenter());

            String str = highlighter.getBestFragment(this.env.indexAnalyzer, fieldName, text);
            return null != str ? str : text;
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
        return text;
    }
}
