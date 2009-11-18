package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeListIterator;
import net.sf.saxon.om.SequenceIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class SearchExtension
{
    // logging machinery
    private static final Logger logger = LoggerFactory.getLogger(SearchExtension.class);

    private static Controller controller;

    public static SequenceIterator queryIndex( String indexId, String queryId )
    {
        List<NodeInfo> nodes = getController().queryIndex(indexId, Integer.decode(queryId));
        if (null != nodes) {
            return new NodeListIterator(nodes);
        }

        return null;
    }

    public static String highlightQuery( String indexId, String queryId, String fieldName, String text, String openMark, String closeMark)
    {
        return getController().highlightQuery(indexId, Integer.decode(queryId), fieldName, text, openMark, closeMark);
    }

    public static void setController( Controller ctrl )
    {
        controller = ctrl;
    }

    public static Controller getController()
    {
        return controller;
    }
}
