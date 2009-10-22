package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.xpath.XPathEvaluator;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.Analyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathConstants;

import uk.ac.ebi.arrayexpress.utils.search.ExperimentTextAnalyzer;

public class Indexer
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Configuration config;

    private TransformerFactoryImpl trFactory;

    public Indexer( Configuration config )
    {
        this.config = config;
    }

    private class FieldInfo
    {
        public String name;
        public XPathExpression xpe;
        public boolean shouldAnalyze;
        public boolean shouldStore;

        public FieldInfo(String name, XPathExpression xpe, boolean shouldAnalyze, boolean shouldStore)
        {
            this.name = name;
            this.xpe = xpe;
            this.shouldAnalyze = shouldAnalyze;
            this.shouldStore = shouldStore;
        }
    }

    public List<NodeInfo> index( String indexId, DocumentInfo document )
    {
        // so how this works?
        // 1. read the configuration and determine the location of index
        // 2. read the document xpath and apply it to the document so we iterate over (capturing NodeInfo)
        // 3. read fields xpath and add fields, applying results to the document
        // 4. commit the index and return

        List<NodeInfo> indexedNodes = null;

        HierarchicalConfiguration indexConfig = this.config.getIndexConfig(indexId);
        if ( null != indexConfig ) {
            String documentPath = indexConfig.getString("document[@path]");

            try {
                XPath xp = new XPathEvaluator(document.getConfiguration());
                XPathExpression xpe = xp.compile(documentPath);
                List documentNodes = (List)xpe.evaluate(document, XPathConstants.NODESET);
                indexedNodes = new ArrayList<NodeInfo>(documentNodes.size());
                if (null != documentNodes) {
                    List fieldsConfig = indexConfig.configurationsAt("document.field");

                    // prepare xpath expressions to save some time
                    List<FieldInfo> fields = new ArrayList<FieldInfo>();
                    for (Object fieldConfig : fieldsConfig) {
                        fields.add(
                            new FieldInfo(
                                ((HierarchicalConfiguration)fieldConfig).getString("[@name]")
                                , xp.compile(((HierarchicalConfiguration)fieldConfig).getString("[@path]"))
                                , ((HierarchicalConfiguration)fieldConfig).getBoolean("[@analyze]")
                                , ((HierarchicalConfiguration)fieldConfig).getBoolean("[@store]")
                            )
                        );
                    }

                    String indexBaseLocation = indexConfig.getString("[@location]");
                    String indexAnalyzer = indexConfig.getString("[@analyzer]");
                    Analyzer a = (Analyzer)Class.forName(indexAnalyzer).newInstance();
                    IndexWriter w = createIndex(new File(indexBaseLocation, indexId), a);

                    for (Object node : documentNodes) {
                        Document d = new Document();
                        addIndexField(d, "_node_id", String.valueOf(indexedNodes.size()), false, true);

                        // get all the fields taken care of
                        for (FieldInfo field : fields) {
                            List values = (List)field.xpe.evaluate(node, XPathConstants.NODESET);
                            for (Object v : values) {
                                String fieldValue = "";
                                if (v instanceof String) {
                                    fieldValue = (String)v;
                                } else if (v instanceof NodeInfo) {
                                    fieldValue = ((NodeInfo)v).getStringValue();
                                } else {
                                    fieldValue = v.toString();
                                    logger.warn("Not sure if I handle the value of [{}] correctly, relying on Object.toString()", v.getClass().getName());
                                }
                                addIndexField(d, field.name, fieldValue, field.shouldAnalyze, field.shouldStore);
                            }
                        }
                        addIndexDocument(w, d);
                        // append node to the list
                        indexedNodes.add((NodeInfo)node);
                    }
                    commitIndex(w);
                }
            } catch (Throwable x) {
                logger.error("Caught an exception:", x);
            }

        }
        return indexedNodes;
    }


    private IndexWriter createIndex(File indexDirectory, Analyzer analyzer)
    {
        IndexWriter iwriter = null;
        try {
            iwriter = new IndexWriter(indexDirectory, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }

        return iwriter;
    }

    private void addIndexField(Document document, String name, String value, boolean shouldAnalyze, boolean shouldStore)
    {
        document.add(new Field(name, value, shouldStore ? Field.Store.YES : Field.Store.NO, shouldAnalyze ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED));
    }

    private void addIndexDocument(IndexWriter iwriter, Document document)
    {
        try {
            iwriter.addDocument(document);
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
    }

    private void commitIndex(IndexWriter iwriter)
    {
        try {
            iwriter.optimize();
            iwriter.commit();
            iwriter.close();
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
    }
}
