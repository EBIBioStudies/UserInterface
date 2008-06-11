package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public class PersistableFilesMap extends HashMap<String,String> implements StringPersistable
{
    public String toPersistence()
    {
        StringBuilder sb = new StringBuilder();

        for ( Map.Entry<String, String> entry : this.entrySet() )
            {
            sb.append( entry.getKey() ).append('\t').append( entry.getValue() ).append('\n');
            }

        return sb.toString();
    }

    public void fromPersisence( String str )
    {
        this.clear();

        int beginIndex = 0;
        int eolIndex = str.indexOf( EOL, beginIndex );
        while ( -1 != eolIndex && eolIndex < str.length() ) {
            String line = str.substring( beginIndex, eolIndex );
            int tabIndex = line.indexOf('\t');
            if ( -1 != tabIndex ) {
                this.put( line.substring( 0, tabIndex ), line.substring( tabIndex + 1 ) );
            } else {
                log.warn("No TAB found while parsing persistence string, line from [" + beginIndex + "] to [" + eolIndex + "]");
            }
            beginIndex = eolIndex + 1;
            eolIndex = str.indexOf( EOL, beginIndex );
        }
    }

    public boolean shouldLoadFromPersistence()
    {
        return ( 0 == this.size() );
    }

    // logging macinery
    private final Log log = LogFactory.getLog(getClass());

}