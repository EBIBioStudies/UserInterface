package uk.ac.ebi.arrayexpress.components;

import net.sf.saxon.om.NodeInfo;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class SearchEngine extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Analyzer analyzer;
    private File indexDirectory;

    private IndexWriter iwriter;
    private Document document;

    private IndexReader ireader;
    private List<NodeInfo> contextNodes = new ArrayList<NodeInfo>();

    public SearchEngine()
    {
        super("SearchEngine");
    }

    public void initialize()
    {
        String tmpDir = System.getProperty("java.io.tmpdir");
        indexDirectory = new File(tmpDir, "index");

        analyzer = new WhitespaceAnalyzer();
    }

    public void terminate()
    {
        try {
            if (null != ireader) {
                ireader.close();
            }

            /* if (null != directory) {
                directory.close();
            }
            */
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
    }

    public void createIndex()
    {
        if (null == iwriter) {
            try {
                iwriter = new IndexWriter(indexDirectory, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
            } catch (Throwable x) {
                logger.error("Caught an exception", x);
            }
        } else {
            logger.error("Index writer has already been created!");
        }
    }

    public void newIndexDocument(NodeInfo contextNode)
    {
        if (null == document) {
            document = new Document();
            contextNodes.add(contextNode);
            document.add(new Field("_id", String.valueOf(contextNodes.size() - 1), Field.Store.YES, Field.Index.NO));
        } else {
            logger.error("Index document has already been created!");
        }
    }

    public void addIndexField(String name, String value, boolean shouldAnalyze, boolean shouldStore)
    {
        if (null != document) {
            document.add(new Field(name, value, shouldStore ? Field.Store.YES : Field.Store.NO, shouldAnalyze ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED));
        } else {
            logger.error("Create document first!");
        }
    }

    public void addIndexDocument()
    {
        if (null != iwriter) {
            if (null != document) {
                try {
                    iwriter.addDocument(document);
                    document = null;
                } catch (Throwable x) {
                    logger.error("Caught an exception:", x);
                }
            } else {
                logger.error("Create document first!");
            }
        } else {
            logger.error("Create index writer first!");
        }
    }

    public void commitIndex()
    {
        if (null != iwriter) {
            try {
                iwriter.optimize();
                iwriter.commit();
                iwriter.close();
                iwriter = null;

                try {
                    ireader = IndexReader.open(indexDirectory);
                } catch (Throwable x) {
                    logger.error("Caught an exception:", x);
                }
            } catch (Throwable x) {
                logger.error("Caught an exception:", x);
            }
        } else {
            logger.error("Create index writer first!");
        }
    }

    public List<NodeInfo> queryIndex(String userId, String queryString, String species, String arrayId, String expType)
    {
        List<NodeInfo> results = null;
        try {
            QueryParser parser = new QueryParser("text", analyzer);
            parser.setDefaultOperator(QueryParser.Operator.AND);
            BooleanQuery query = new BooleanQuery();
            if (null != queryString && !queryString.trim().equals("")) {
                query.add(parser.parse(queryString), BooleanClause.Occur.MUST);
            }
            // additional constraint 
            if (null != userId && !userId.equals("0")) {
                query.add(new TermQuery(new Term("user", userId)), BooleanClause.Occur.MUST);
            }

            if (null != species && !species.trim().equals("")) {
                query.add(new TermQuery(new Term("species", species)), BooleanClause.Occur.MUST);
            }

            if (null != arrayId && !arrayId.trim().equals("")) {
                query.add(new TermQuery(new Term("array_id", arrayId)), BooleanClause.Occur.MUST);
            }

            if (null != expType && !expType.trim().equals("")) {
                query.add(new TermQuery(new Term("exp_type", expType)), BooleanClause.Occur.MUST);
            }

            // empty query returns everything
            if (query.clauses().isEmpty()) {
                return contextNodes;
            }

            // to show _all_ available experiments
            IndexSearcher isearcher = new IndexSearcher(ireader);
            TopDocs hits = isearcher.search(query, contextNodes.size());

            results = new ArrayList<NodeInfo>(hits.totalHits);
            for (ScoreDoc d : hits.scoreDocs) {
                results.add(contextNodes.get(Integer.parseInt(isearcher.doc(d.doc).getField("_id").stringValue())));
            }
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
        return results;
    }
}
