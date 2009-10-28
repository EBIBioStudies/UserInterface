package uk.ac.ebi.arrayexpress.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.files.FtpFileEntry;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableDocumentContainer;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableFilesMap;
import uk.ac.ebi.arrayexpress.utils.persistence.TextFilePersistence;

import java.io.File;

public class Files extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String rootFolder;
    private TextFilePersistence<PersistableDocumentContainer> files;

    public Files()
    {
        super("Files");
    }

    public void initialize()
    {
        files = new TextFilePersistence<PersistableDocumentContainer>(
                new PersistableDocumentContainer(),
                new File(
                        System.getProperty("java.io.tmpdir"),
                        getPreferences().getString("ae.files.cache.filename")
                )
        );
    }

    public void terminate()
    {
    }

    public synchronized void setRootFolder( String folder )
    {
        if (null != folder && 0 < folder.length()) {
            if (folder.endsWith(File.separator)) {
                rootFolder = folder;
            } else {
                rootFolder = folder + File.separator;
            }
        } else {
            logger.error("setRootFolder called with null or empty parameter, expect probilems down the road");
        }
    }

    public String getRootFolder()
    {
        if (null == rootFolder) {
            rootFolder = getPreferences().getString("ae.files.root.location");
        }
        return rootFolder;
    }

    // scans the specified rootFolder folder; returns true if no exceptions occured during the action
    public void rescan() throws InterruptedException
    {
        if (null != getRootFolder()) {
            File root = new File(getRootFolder());
            if (!root.exists()) {
                logger.error("Rescan problem: root folder [{}] is inaccessible", getRootFolder());
            } else if (!root.isDirectory()) {
                logger.error("Rescan problem: root folder [{}] is not a directory", getRootFolder());
            } else {
                try {
                    logger.info("Rescan of downloadable files from [{}] requested", getRootFolder());
                    PersistableFilesMap newMap = new PersistableFilesMap();
                    rescanFolder(root, newMap);
                    //setFilesMap(newMap);
                    ((Experiments) getComponent("Experiments")).updateFiles();

                    logger.info("Rescan of downloadable files completed");
                } catch ( InterruptedException x ) {
                    throw x;
                } catch ( Throwable x ) {
                    logger.error("Caught an exception:", x);
                }
            }
        } else {
            logger.error("Rescan problem: root folder has not been set");
        }
    }

    // returns true is file is registered in the registry
    public synchronized boolean doesExist( String accession, String name )
    {
        /*
        if (null != accession && !accession.equals("")) {
            return filesMap.getObject().doesExist(accession, name);
        } else {
            return filesMap.getObject().doesNameExist(name);
        }
        */
        return true;
    }

    // returns absolute file location (if file exists, null otherwise) in local filesystem
    public synchronized String getLocation( String accession, String name )
    {
        String result = "";
        /*
        if (null != accession && !accession.equals("")) {
            FtpFileEntry entry = filesMap.getObject().getEntry(accession, name);
            if (null != entry) {
                result = entry.getLocation();
            }
        } else {
            List<FtpFileEntry> entries = filesMap.getObject().getEntriesByName(name);

            if (null != entries) {
                if (1 == entries.size()) {
                    result = entries.get(0).getLocation();
                } else {
                    logger.error("Multiple entries found for file [{}], cannot offer a definitive download", name);
                }
            }
        }
        */
        return result;
    }

///    public synchronized FtpFilesMap getFilesMap()
///    {
///        return filesMap.getObject();
///    }

    private void rescanFolder( File folder, PersistableFilesMap map ) throws InterruptedException
    {
        if (folder.canRead()) {
            File[] files = folder.listFiles();
            Thread.sleep(1);
            // process files first, then go over sub-folders
            for ( File f : files ) {
                Thread.sleep(1);
                if (f.isFile()) {
                    String name = f.getName();
                    String location = f.getAbsolutePath();

                    if (!f.canRead()) {
                        logger.warn("Rescan found non-readable file [{}]", location);
                    } else if (!name.startsWith(".")) {
                        map.putEntry(new FtpFileEntry(f));
                    }
                }
            }

            // go over sub-folders
            for ( File f : files ) {
                Thread.sleep(1);
                if (f.isDirectory() && !f.getName().startsWith(".")) {
                    rescanFolder(f, map);
                }
            }
        } else {
            logger.warn("Rescan found non-readable folder [{}{}]", folder.getAbsolutePath(), File.separator);
        }
    }

///    private synchronized void setFilesMap( PersistableFilesMap newMap )
///    {
///        filesMap.setObject(newMap);
///    }
}
