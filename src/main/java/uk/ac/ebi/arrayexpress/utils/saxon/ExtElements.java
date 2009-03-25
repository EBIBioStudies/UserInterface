package uk.ac.ebi.arrayexpress.utils.saxon;

import net.sf.saxon.style.ExtensionElementFactory;

public class ExtElements implements ExtensionElementFactory
{
    public Class getExtensionClass(String localname)  {
        if (localname.equals("log")) return LoggerExtElement.class;
        return null;
    }
}
