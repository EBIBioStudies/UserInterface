package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public class TextFilePersistence< Object extends StringPersistable > {

    protected TextFilePersistence()
    {
    }

    protected TextFilePersistence( File file )
    {
        persistenceFile = file;
    }

    public TextFilePersistence( Object obj, File file )
    {
        // TODO: check object and file
        object = obj;
        persistenceFile = file;
    }

    public Object getObject()
    {
        if ( null != object ) {
            if ( object.shouldLoadFromPersistence() ) {
                loadObject();
            }
        }
        return object;
    }

    public void setObject( Object obj )
    {
        object = obj;
        save(object.toPersistence());
    }

    private void loadObject()
    {
        object.fromPersisence(load());
    }

    private String load()
    {
        StringBuilder result = new StringBuilder();
        try {
            if ( persistenceFile.exists() ) {
                BufferedReader r = new BufferedReader( new InputStreamReader( new FileInputStream(persistenceFile) ) );
                boolean isEndOfStream = false;
                while ( r.ready() ) {
                    String str = r.readLine();
                    // null means stream has reached the end
                    if ( null != str ) {
                        result.append(str).append(Object.EOL);
                    } else {
                        break;
                    }
                }
            }
        } catch ( Throwable x ) {
            log.error( "Caught an exception:", x );
        }
        return result.toString();
    }

    private void save( String objectString )
    {
        try {
            BufferedWriter w = new BufferedWriter( new FileWriter(persistenceFile) );
            w.write(objectString);
            w.close();
        } catch ( Throwable x ) {
            log.error( "Caught an exception:", x );
        }
    }

    // logging macinery
    private final Log log = LogFactory.getLog(getClass());


    // persistence file handle
    private File persistenceFile;

    // internal object holder
    private Object object;
}
