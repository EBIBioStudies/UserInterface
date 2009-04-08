package uk.ac.ebi.arrayexpress.utils.saxon;

import net.sf.saxon.om.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ExperimentsContainer
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DocumentInfo experimentsDoc = null;
    private Map<Integer, NodeInfo> idsMap = null;

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
        idsMap = buildIdsMap(doc);
    }

    public NodeInfo getExperimentById(Integer id)
    {
        return idsMap.get(id);
    }

    private Map<Integer, NodeInfo> buildIdsMap(DocumentInfo doc)
    {
        Map<Integer, NodeInfo> idsMap = new HashMap<Integer, NodeInfo>();

        // TODO - this is a shortcut - we expect specific document structure
        NodeInfo experimentsNode = (NodeInfo)doc.iterateAxis(Axis.CHILD).next();
        if (null != experimentsNode) {
            // TODO - this is a shortcut - we expect specific document structure
            AxisIterator experimentItor = experimentsNode.iterateAxis(Axis.CHILD);
            while (experimentItor.moveNext()) {
                NodeInfo experimentNode = (NodeInfo)experimentItor.current();
                if (null != experimentNode) {
                    // TODO - this is a shortcut - we expect first child of experiment is <id>intid</id>
                    NodeInfo experimentIdNode = (NodeInfo)experimentNode.iterateAxis(Axis.CHILD).next();
                    if (null != experimentIdNode) {
                        Integer idValue = Integer.decode(experimentIdNode.getStringValue());
                        if (null != idValue) {
                            idsMap.put(idValue, experimentNode);
                        } else {
                            logger.error("Unable to convert value [{}] of node [{}] to integer", experimentIdNode.getStringValue(), Navigator.getPath(experimentIdNode));
                        }
                    } else {
                        logger.error("Unable to find any children for node [{}]", Navigator.getPath(experimentNode));
                    }
                }
            }
        }
        return idsMap;
    }
}
