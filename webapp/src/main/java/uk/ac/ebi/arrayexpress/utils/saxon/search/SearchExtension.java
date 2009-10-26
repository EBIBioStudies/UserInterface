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

    public static SequenceIterator queryIndex( String indexId, String queryId )
    {
        List<NodeInfo> nodes = Controller.getInstance().queryIndex(indexId, Integer.decode(queryId));
        if (null != nodes) {
            return new NodeListIterator(nodes);
        }

        return null;
    }

    public static String highlightQuery( String indexId, String queryId, String text, String openMark, String closeMark)
    {
        return Controller.getInstance().highlightQuery(indexId, Integer.decode(queryId), text, openMark, closeMark);
    }
}
