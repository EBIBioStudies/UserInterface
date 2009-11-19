package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;

public final class SearchExtension
{

    public static SequenceIterator queryIndex( XPathContext context, String indexId, String queryId )
    {
        return ((NodeInfo)context.getContextItem()).iterateAxis(Axis.CHILD);
    }

    public static String highlightQuery( String indexId, String queryId, String fieldName, String text, String openMark, String closeMark)
    {
        return text;
    }
}
