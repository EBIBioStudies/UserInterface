package uk.ac.ebi.arrayexpress.utils.persistence;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.*;

public class TextFilePersistence<Object extends Persistable>
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

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
        logger.debug("Retrieving persistable object [{}] from [{}]", object.getClass().toString(), persistenceFile.getName());

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
                logger.debug("Object successfully retrieved");
            } else {
                logger.warn("Persistence file [{}] not found", persistenceFile.getAbsolutePath());
            }
        } catch ( Throwable x ) {
            logger.error("Caught an exception:", x);
        }
        return result.toString();
    }

    private void save( String objectString )
    {
        logger.debug("Saving persistable object [{}] to [{}]", object.getClass().toString(), persistenceFile.getName());
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(persistenceFile));
            w.write(objectString);
            w.close();
            logger.debug("Object successfully saved");

        } catch ( Throwable x ) {
            logger.error("Caught an exception:", x);
        }
    }
}
