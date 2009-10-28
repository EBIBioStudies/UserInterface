package uk.ac.ebi.arrayexpress.utils.saxon;

import net.sf.saxon.om.DocumentInfo;

public class DocumentContainer
{
    private DocumentInfo document = null;

    public DocumentContainer()
    {
    }

    public DocumentContainer(DocumentInfo doc)
    {
        setDocument(doc);
    }

    public DocumentInfo getDocument()
    {
        return this.document;
    }

    public void setDocument(DocumentInfo doc)
    {
        this.document = doc;
    }
}
