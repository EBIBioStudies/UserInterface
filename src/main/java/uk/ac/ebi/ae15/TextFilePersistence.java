package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public class TextFilePersistence<Object extends PersistableInString>
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    // persistence file handle
    private File persistenceFile;

    // internal object holder
    private Object object;

    public TextFilePersistence( Object obj, File file )
    {
        // TODO: check object and file
        object = obj;
        persistenceFile = file;
    }

    public Object getObject()
    {
        if (null != object) {
            if (object.shouldLoadFromPersistence()) {
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
        object.fromPersistence(load());
    }

    private String load()
    {
        log.debug("Retrieving persistable object [" + object.getClass().toString() + "] from [" + persistenceFile.getName() + "]");

        StringBuilder result = new StringBuilder();
        try {
            if (persistenceFile.exists()) {
                BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(persistenceFile)));
                while ( r.ready() ) {
                    String str = r.readLine();
                    // null means stream has reached the end
                    if (null != str) {
                        result.append(str).append(Object.EOL);
                    } else {
                        break;
                    }
                }
                log.debug("Object successfully retrieved");
            } else {
                log.warn("Persistence file [" + persistenceFile.getAbsolutePath() + "] not found");
            }
        } catch ( Throwable x ) {
            log.error("Caught an exception:", x);
        }
        return result.toString();
    }

    private void save( String objectString )
    {
        log.debug("Saving persistable object [" + object.getClass().toString() + "] to [" + persistenceFile.getName() + "]");
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(persistenceFile));
            w.write(objectString);
            w.close();
            log.debug("Object successfully saved");

        } catch ( Throwable x ) {
            log.error("Caught an exception:", x);
        }
    }
}
