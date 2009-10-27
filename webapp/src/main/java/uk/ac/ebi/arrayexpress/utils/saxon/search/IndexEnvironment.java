package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.om.NodeInfo;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexEnvironment
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // source index configuration (will be eventually removed)
    public HierarchicalConfiguration indexConfig;

    // index configuration, parsed
    public String indexId;
    public Directory indexDirectory;
    public PerFieldAnalyzerWrapper indexAnalyzer;

    // index document xpath
    public String indexDocumentPath;

    // field information, parsed
    public class FieldInfo
    {
        public String name;
        public String path;
        public boolean shouldAnalyze;
        public String analyzer;
        public boolean shouldStore;

        public FieldInfo(HierarchicalConfiguration fieldConfig)
        {
            this.name = fieldConfig.getString("[@name]");
            this.path = fieldConfig.getString("[@path]");
            this.shouldAnalyze = fieldConfig.getBoolean("[@analyze]");
            this.analyzer = fieldConfig.getString("[@analyzer]");
            this.shouldStore = fieldConfig.getBoolean("[@store]");
        }
    }

    public Map<String, FieldInfo> fields;

    // document info
    public int documentHashCode;
    public List<NodeInfo> documentNodes;

    public IndexEnvironment(HierarchicalConfiguration indexConfig)
    {
        this.indexConfig = indexConfig;
        populateIndexConfiguration();
    }

    public void putDocumentInfo(int documentHashCode, List<NodeInfo> documentNodes)
    {
        this.documentHashCode = documentHashCode;
        this.documentNodes = documentNodes;
    }

    private void populateIndexConfiguration()
    {
        try {
            this.indexId = this.indexConfig.getString("[@id]");

            String indexBaseLocation = this.indexConfig.getString("[@location]");
            this.indexDirectory = FSDirectory.open(new File(indexBaseLocation, this.indexId));

            String indexAnalyzer = this.indexConfig.getString("[@defaultAnalyzer]");
            Analyzer a = (Analyzer) Class.forName(indexAnalyzer).newInstance();
            this.indexAnalyzer = new PerFieldAnalyzerWrapper(a);

            this.indexDocumentPath = indexConfig.getString("document[@path]");

            List fieldsConfig = indexConfig.configurationsAt("document.field");

            this.fields = new HashMap<String,FieldInfo>();
            for (Object fieldConfig : fieldsConfig) {
                FieldInfo fieldInfo = new FieldInfo((HierarchicalConfiguration) fieldConfig);
                fields.put(fieldInfo.name, fieldInfo);
                if (null != fieldInfo.analyzer) {
                    Analyzer fa = (Analyzer) Class.forName(fieldInfo.analyzer).newInstance();
                    this.indexAnalyzer.addAnalyzer(fieldInfo.name, fa);
                }
            }

        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
    }
}