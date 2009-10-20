package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.om.DocumentInfo;

import java.net.URL;
import java.util.Map;
import java.util.HashMap;



public class Controller
{
    // there should be a single instance of Controller in the runtime environment
    private static Controller self;

    private Configuration config;
    private Map<String, IndexInfo> indicesInfo = new HashMap<String, IndexInfo>();

    private Controller( URL configFile )
    {
        this.config = new Configuration(configFile);
    }

    public void index( String indexId, DocumentInfo document )
    {
        this.indicesInfo.put(indexId, new IndexInfo( document.hashCode(), new Indexer(config).index(indexId, document)));
    }

    public static Controller getController( URL configFile )
    {
        if (null == self) {
            self = new Controller(configFile);
        }

        return self;
    }
}
