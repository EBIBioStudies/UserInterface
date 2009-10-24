package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.om.NodeInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.QueryParser;
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

    private Controller.IndexEnvironment env;

    public Querier( Controller.IndexEnvironment env )
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

    public List<NodeInfo> queryIndex( String queryString )
    {
        List<NodeInfo> result = null;
        try {
            IndexReader ir = IndexReader.open(this.env.indexDirectory, true);

            BooleanQuery query = new BooleanQuery();

            if (null != queryString && !queryString.trim().equals("")) {
                queryString = queryString.trim();
                Query q;


                QueryParser parser = new QueryParser("keywords", this.env.indexAnalyzer);
                parser.setDefaultOperator(QueryParser.Operator.AND);
                q = parser.parse(queryString).rewrite(ir);
                logger.info("Query [{}] was rewritten to [{}]", queryString, q.toString());
                //
                query.add(q, BooleanClause.Occur.MUST);


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
            }
            ir.close();
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }

    return result;
    }

    // query cache
    private final static int QUERY_CACHE_SIZE = 25;
    private static Map<String,Query> queryCache = new HashMap<String,Query>(QUERY_CACHE_SIZE);

    public String highlightQuery(String queryString, String text)
    {
        if (null != queryString && !queryString.trim().equals("")) {
            try {
                Query q;
                if (queryCache.containsKey(queryString)) {
                    q = queryCache.get(queryString);
                } else {
                    QueryParser parser = new QueryParser("keywords", this.env.indexAnalyzer);
                    parser.setDefaultOperator(QueryParser.Operator.AND);
                    q = parser.parse(queryString);
                    logger.info("Query [{}] was parsed to [{}]", queryString, q.toString());
                    queryCache.put(queryString, q);
                }
                
                SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("\u00ab", "\u00bb");
                Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(q));
                highlighter.setTextFragmenter(new NullFragmenter());

                String str = highlighter.getBestFragment(this.env.indexAnalyzer, "keywords", text);
                return null != str ? str : text;
            } catch (Throwable x) {
                logger.error("Caught an exception:", x);
            }
        }
        return text;
    }
}
