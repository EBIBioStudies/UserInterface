package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.om.NodeInfo;

import java.util.List;


public class IndexInfo
{
    public int documentHashCode;
    public List<NodeInfo> documentIndexedNodes;

    public IndexInfo(int hashCode, List<NodeInfo> indexedNodes)
    {
        this.documentHashCode = hashCode;
        this.documentIndexedNodes = indexedNodes;
    }
}
