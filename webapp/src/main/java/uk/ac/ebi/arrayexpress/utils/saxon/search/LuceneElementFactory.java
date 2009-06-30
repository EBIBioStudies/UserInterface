package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.style.ExtensionElementFactory;

public class LuceneElementFactory implements ExtensionElementFactory
{
    public Class getExtensionClass(String localname)
    {
        if (localname.equals("document")) return DocumentElement.class;
        if (localname.equals("create")) return CreateElement.class;
        if (localname.equals("commit")) return CommitElement.class;
        if (localname.equals("field")) return FieldElement.class;

        return null;
    }
}
