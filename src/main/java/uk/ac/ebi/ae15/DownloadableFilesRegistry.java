package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.app.ApplicationComponent;
import uk.ac.ebi.ae15.utils.persistence.PersistableFilesMap;
import uk.ac.ebi.ae15.utils.persistence.TextFilePersistence;

import java.io.File;
import java.util.Map;

public class DownloadableFilesRegistry extends ApplicationComponent
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());
    // root folder location (in local file system terms)
    private String rootFolder;
    // filename->location map
    private TextFilePersistence<PersistableFilesMap> filesMap;

    public DownloadableFilesRegistry(Application app)
    {
        super(app, "DownloadableFilesRegistry");
        filesMap = new TextFilePersistence<PersistableFilesMap>(
                new PersistableFilesMap(),
                new File(
                        System.getProperty("java.io.tmpdir"),
                        getPreferences().get("ae.files.cache.filename")
                )
        );
    }

    public void initialize()
    {
    setRootFolder(getPreferences().get("ae.files.root.location"));
    }

    public void terminate()
    {

    }

    public synchronized void setRootFolder(String folder)
    {
        if (null != folder && 0 < folder.length()) {
            if (folder.endsWith(File.separator)) {
                rootFolder = folder;
            } else {
                rootFolder = folder + File.separator;
            }
        } else {
            log.error("setRootFolder called with null or empty parameter, expect probilems down the road");
        }
    }

    // scans the specified root folder; returns true if no exceptions occured during the action
    public void rescan() throws InterruptedException
    {
        if (null != rootFolder) {
            File root = new File(rootFolder);
            if (!root.exists()) {
                log.error("Rescan problem: root folder [" + rootFolder + "] is inaccessible");
            } else if (!root.isDirectory()) {
                log.error("Rescan problem: root folder [" + rootFolder + "] is not a directory");
            } else {
                try {
                    log.info("Rescan of downloadable files from [" + rootFolder + "] requested");
                    PersistableFilesMap newMap = new PersistableFilesMap();
                    rescanFolder(root, newMap);
                    setFilesMap(newMap);
                    log.info("Rescan of downloadable files completed");
                } catch (InterruptedException x) {
                    throw x;
                } catch (Throwable x) {
                    log.error("Caught an exception:", x);
                }
            }
        } else {
            log.error("Rescan problem: root folder has not been set");
        }
    }

    // returns true is file is registered in the registry
    public synchronized boolean doesExist(String fileName)
    {
        return filesMap.getObject().containsKey(fileName);
    }

    // returns absolute file location (if file exists, null otherwise) in local filesystem
    public synchronized String getLocation(String fileName)
    {
        return filesMap.getObject().get(fileName);
    }

    // returns relative file location (to the root folder) if file exists, null otherwise)
    public synchronized String getRelativeLocation(String fileName)
    {
        String location = filesMap.getObject().get(fileName);
        if (null != location) {
            int ix = location.indexOf(rootFolder);
            if (-1 != ix) {
                location = location.substring(ix);
            }
        }
        return location;
    }

    private void rescanFolder(File folder, Map<String, String> map) throws InterruptedException
    {
        if (folder.canRead()) {
            File[] files = folder.listFiles();
            Thread.sleep(1);
            // process files first, then go over sub-folders
            for (File f : files) {
                Thread.sleep(1);
                if (f.isFile()) {
                    String name = f.getName();
                    String location = f.getAbsolutePath();

                    if (!f.canRead()) {
                        log.warn("Rescan found non-readable file [" + location + "]");
                    } else if (map.containsKey(name)) {
                        log.warn("Rescan found a duplicate file [" + location + "], registry entry is [" + name + "," + map.get(name) + "]");
                    } else if (!name.startsWith(".")) {
                        map.put(name, location);
                    }
                }
            }

            // go over sub-folders
            for (File f : files) {
                Thread.sleep(1);
                if (f.isDirectory() && !f.getName().startsWith(".")) {
                    rescanFolder(f, map);
                }
            }
        } else {
            log.warn("Rescan found non-readable folder [" + folder.getAbsolutePath() + File.separator + "]");
        }
    }

    private synchronized void setFilesMap( PersistableFilesMap newMap)
    {
        filesMap.setObject(newMap);
    }
}
