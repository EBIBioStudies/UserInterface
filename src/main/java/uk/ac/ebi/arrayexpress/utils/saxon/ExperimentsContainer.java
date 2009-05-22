package uk.ac.ebi.arrayexpress.utils.saxon;

import net.sf.saxon.om.DocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentsContainer
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DocumentInfo experimentsDoc = null;

    public ExperimentsContainer()
    {
    }

    public ExperimentsContainer(DocumentInfo doc)
    {
        setDocument(doc);
    }

    public DocumentInfo getDocument()
    {
        return experimentsDoc;
    }

    public void setDocument(DocumentInfo doc)
    {
        experimentsDoc = doc;
    }
}
