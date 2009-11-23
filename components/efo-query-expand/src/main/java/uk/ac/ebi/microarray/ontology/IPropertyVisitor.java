package uk.ac.ebi.microarray.ontology;

/**
 * @author Anna Zhukova
 * Strategy of nodes relationship induced by the property processing.
 */
public interface IPropertyVisitor<N extends IOntologyNode> {
    /**
     * What property we are intrested in.
     * @return Property.
     */
    String getPropertyName();

    /**
     * If we want to look for property relationships of this node.
     * @param node Node.
     * @return If we are interested in this node property relationships.
     */
    boolean isInterestedInNode(N node);

    /**
     * Process relationshid forsed by the property we are interested in between given nodes.
     * @param node First node.
     * @param friend Second node.
     */
    void inRelationship(N node, N friend);
}
