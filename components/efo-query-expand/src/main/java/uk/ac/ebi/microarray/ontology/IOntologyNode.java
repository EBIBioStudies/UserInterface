package uk.ac.ebi.microarray.ontology;

import java.util.Collection;

/**
 * @author Anna Zhukova
 * Representation of ontology class.
 */
public interface IOntologyNode {
    /**
     * Returns children.
     * @return Child collection.
     */
    <RealType extends IOntologyNode> Collection<RealType> getChildren();
}
