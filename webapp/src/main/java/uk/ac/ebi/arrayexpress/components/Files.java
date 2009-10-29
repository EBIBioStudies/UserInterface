package uk.ac.ebi.arrayexpress.components;

import net.sf.saxon.om.DocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableDocumentContainer;
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

    public synchronized DocumentInfo getFiles()
    {
        return this.files.getObject().getDocument();
    }

    private synchronized void setFiles( DocumentInfo doc )
    {
        if (null != doc) {
            this.files.setObject(new PersistableDocumentContainer(doc));
        } else {
            this.logger.error("Files NOT updated, NULL document passed");
        }
    }

    public void reload( String xmlString )
    {
        DocumentInfo doc = loadFilesFromString(xmlString);
        if (null != doc) {
            setFiles(doc);
        }
    }

    private DocumentInfo loadFilesFromString( String xmlString )
    {
        DocumentInfo doc = ((SaxonEngine)getComponent("SaxonEngine")).transform(xmlString, "preprocess-files-xml.xsl", null);
        if (null == doc) {
            this.logger.error("Transformation [preprocess-files-xml.xsl] returned an error, returning null");
            return null;
        }
        return doc;
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
}
