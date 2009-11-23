package uk.ac.ebi.microarray.ontology;

import org.semanticweb.owl.model.OWLAnnotationVisitor;

import java.util.Collection;

/**
 * @author Anna Zhukova
 *         Visits annotations of the ontology class, stores usefull information and then is able to create appropriate node.
 */
public interface IClassAnnotationVisitor<N extends IOntologyNode> extends OWLAnnotationVisitor
{
    /**
     * If just loaded node is a fake one and should not be put into ontology map.
     *
     * @return If just loaded node is a fake one.
     */
    boolean isOrganizational();

    /**
     * Given the id of the ontology class constructs IOntologyNode.
     *
     * @param id Id of the ontology class given.
     * @return IOntologyNode constructed.
     */
    N getOntologyNode( String id );

    /**
     * Given the node corresponding to the ontology class and nodes corresponding to its children
     * and information if this class is organisational updates this ontology node in appropriate way.
     *
     * @param node                 IOntologyNode given.
     * @param children             Child nodes.
     * @param isNodeOrganizational If the given node is organizational.
     */
    void updateOntologyNode( N node, Collection<N> children, boolean isNodeOrganizational );
}
