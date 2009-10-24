package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // there should be a single instance of Controller in the runtime environment
    private static Controller self;

    private Configuration config;

    public class IndexEnvironment
    {
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

            public FieldInfo( HierarchicalConfiguration fieldConfig )
            {
                this.name = fieldConfig.getString("[@name]");
                this.path = fieldConfig.getString("[@path]");
                this.shouldAnalyze = fieldConfig.getBoolean("[@analyze]");
                this.analyzer = fieldConfig.getString("[@analyzer]");
                this.shouldStore = fieldConfig.getBoolean("[@store]");
            }
        }
        public List<FieldInfo> fields;

        // document info
        public int documentHashCode;
        public List<NodeInfo> documentNodes;

        public IndexEnvironment( HierarchicalConfiguration indexConfig )
        {
            this.indexConfig = indexConfig;
            populateIndexConfiguration();
        }

        public void putDocumentInfo( int documentHashCode, List<NodeInfo> documentNodes )
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
                Analyzer a = (Analyzer)Class.forName(indexAnalyzer).newInstance();
                this.indexAnalyzer = new PerFieldAnalyzerWrapper(a);

                this.indexDocumentPath = indexConfig.getString("document[@path]");

                List fieldsConfig = indexConfig.configurationsAt("document.field");

                this.fields = new ArrayList<FieldInfo>();
                for (Object fieldConfig : fieldsConfig) {
                    FieldInfo fieldInfo = new FieldInfo((HierarchicalConfiguration)fieldConfig);
                    fields.add(fieldInfo);
                    if (null != fieldInfo.analyzer) {
                        Analyzer fa = (Analyzer)Class.forName(fieldInfo.analyzer).newInstance();
                        this.indexAnalyzer.addAnalyzer(fieldInfo.name, fa);
                    }
                }
                
            } catch (Throwable x) {
                logger.error("Caught an exception:", x);
            }
        }
    }

    private Map<String, IndexEnvironment> environment = new HashMap<String, IndexEnvironment>();

    private Controller( URL configFile )
    {
        this.config = new Configuration(configFile);
    }

    public IndexEnvironment getEnvironment( String indexId )
    {
        if (!this.environment.containsKey(indexId)) {
            this.environment.put(indexId, new IndexEnvironment(config.getIndexConfig(indexId)));
        }

        return this.environment.get(indexId);
    }

    public void index( String indexId, DocumentInfo document )
    {
        logger.info("Started indexing for index id [{}]", indexId);
        getEnvironment(indexId).putDocumentInfo(
                document.hashCode()
                , new Indexer(getEnvironment(indexId)).index(document)
        );
        logger.info("Indexing for index id [{}] completed", indexId);
    }

    public List<String> getTerms( String indexId, String fieldName )
    {
        return new Querier(getEnvironment(indexId)).getTerms(fieldName);
    }

    public List<NodeInfo> queryIndex( String indexId, String queryString )
    {
        return new Querier(getEnvironment(indexId)).queryIndex(queryString);
    }

    public String highlightQuery( String indexId, String queryString, String text )
    {
        return new Querier(getEnvironment(indexId)).highlightQuery(queryString, text);
    }

    public static Controller getController( URL configFile )
    {
        if (null == self) {
            self = new Controller(configFile);
        }

        return self;
    }
}
