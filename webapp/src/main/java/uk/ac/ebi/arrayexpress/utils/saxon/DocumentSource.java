package uk.ac.ebi.arrayexpress.utils.saxon;

import net.sf.saxon.om.DocumentInfo;

public interface DocumentSource
{
    public String getDocumentURI();
    public DocumentInfo getDocument();
}
