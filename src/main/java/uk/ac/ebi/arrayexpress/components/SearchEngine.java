package uk.ac.ebi.arrayexpress.components;

import net.sf.saxon.om.NodeInfo;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;

import java.util.ArrayList;
import java.util.List;


public class SearchEngine extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Analyzer analyzer;
    private Directory directory;

    private IndexWriter iwriter;
    private Document document;

    private IndexSearcher isearcher;
    private List<NodeInfo> contextNodes = new ArrayList<NodeInfo>();

    public SearchEngine()
    {
        super("SearchEngine");
    }

    public void initialize()
    {
        analyzer = new WhitespaceAnalyzer();
        directory = new RAMDirectory();
    }

    public void terminate()
    {
        try {
            if (null != isearcher) {
                isearcher.close();
            }

            if (null != directory) {
                directory.close();
            }

        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
    }

    public void createIndex()
    {
        if (null == iwriter) {
            try {
                iwriter = new IndexWriter(directory, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
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

    public void addIndexField(String name, String value, int flags)
    {
        if (null != document) {
            document.add(new Field(name, value, Field.Store.NO, Field.Index.ANALYZED));
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
                    isearcher = new IndexSearcher(directory);
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

    public List<NodeInfo> queryIndex(String queryString)
    {
        List<NodeInfo> results = null;
        try {
            QueryParser parser = new QueryParser("text", analyzer);
            parser.setDefaultOperator(QueryParser.Operator.AND);
            Query query = parser.parse(queryString);
            TopDocs hits = isearcher.search(query, 99999);

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
