package uk.ac.ebi.arrayexpress.utils.saxon.search;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

public class Configuration
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, HierarchicalConfiguration> indicesConfig = new HashMap<String, HierarchicalConfiguration>();

    public Configuration( URL configResource )
    {
        try {
            // set list delimiter to bogus value to disable list parsing in configuration values
            XMLConfiguration.setDefaultListDelimiter('\uffff');
            XMLConfiguration xmlConfig = new XMLConfiguration(configResource);
            List indexList = xmlConfig.configurationsAt("index");

            for ( Iterator it = indexList.iterator(); it.hasNext();)
            {
                HierarchicalConfiguration indexConfig = (HierarchicalConfiguration) it.next();
                String indexId = indexConfig.getString("[@id]");
                this.indicesConfig.put(indexId, indexConfig);
            }
        } catch (ConfigurationException x) {
            logger.error("There was an exception thrown:", x);
        }
    }

    public HierarchicalConfiguration getIndexConfig( String indexId )
    {
        if ( indicesConfig.containsKey(indexId)) {
            return indicesConfig.get(indexId);
        }
        
        return null;
    }
}
