package uk.ac.ebi.arrayexpress.utils.persistence;

import net.sf.saxon.s9api.XdmNode;
import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.components.SaxonEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: CHECK VERSION
public class PersistableDocumentContainer implements Persistable
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // document storage
    private XdmNode document;

    public PersistableDocumentContainer()
    {
        createDocument();
    }

    public PersistableDocumentContainer( XdmNode doc )
    {
        if (null == doc) {
            createDocument();
        } else {
            document = doc;
        }
    }

    public XdmNode getDocument()
    {
        return document;
    }

    public String toPersistence()
    {
        return ((SaxonEngine)Application.getAppComponent("SaxonEngine")).serializeDocument(document);
    }

    public void fromPersistence( String str )
    {
        document = ((SaxonEngine)Application.getAppComponent("SaxonEngine")).buildDocument(str);
        
        if (null == document) {
            createDocument();
        }
    }

    public boolean shouldLoadFromPersistence()
    {
        if (null == document)
            return true;

        String total = ((SaxonEngine)Application.getAppComponent("SaxonEngine")).evaluateXPathSingle(document, "/experiments/@total");

        return (null == total || total.equals("0"));
    }

    private void createDocument()
    {
        document = ((SaxonEngine)Application.getAppComponent("SaxonEngine")).buildDocument("<?xml version=\"1.0\"?><experiments total=\"0\"></experiments>");

        if (null == document) {
            logger.error("The document WAS NOT created, expect problems down the road");
        }

    }
}
