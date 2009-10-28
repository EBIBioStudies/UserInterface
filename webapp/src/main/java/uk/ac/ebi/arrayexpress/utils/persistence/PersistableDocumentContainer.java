package uk.ac.ebi.arrayexpress.utils.persistence;

import net.sf.saxon.om.DocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.components.SaxonEngine;
import uk.ac.ebi.arrayexpress.utils.saxon.DocumentContainer;

// TODO - check XML version on persistence events

public class PersistableDocumentContainer extends DocumentContainer implements Persistable
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PersistableDocumentContainer()
    {
        createDocument();
    }

    public PersistableDocumentContainer( DocumentInfo doc )
    {
        if (null == doc) {
            createDocument();
        } else {
            setDocument(doc);
        }
    }

    public String toPersistence()
    {
        return ((SaxonEngine)Application.getAppComponent("SaxonEngine")).serializeDocument(getDocument());
    }

    public void fromPersistence( String str )
    {
        setDocument(((SaxonEngine)Application.getAppComponent("SaxonEngine")).buildDocument(str));
        
        if (null == getDocument()) {
            createDocument();
        }
    }

    public boolean isEmpty()
    {
        if (null == getDocument())
            return true;

        String total = ((SaxonEngine)Application.getAppComponent("SaxonEngine")).evaluateXPathSingle(getDocument(), "/experiments/@total");

        return (null == total || total.equals("0"));
    }

    private void createDocument()
    {
        setDocument(((SaxonEngine)Application.getAppComponent("SaxonEngine")).buildDocument("<?xml version=\"1.0\"?><experiments total=\"0\"></experiments>"));

        if (null == getDocument()) {
            logger.error("The document WAS NOT created, expect problems down the road");
        }

    }
}
