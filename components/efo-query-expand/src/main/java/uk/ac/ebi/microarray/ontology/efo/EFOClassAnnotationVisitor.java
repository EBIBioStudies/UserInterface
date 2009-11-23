package uk.ac.ebi.microarray.ontology.efo;

import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLConstantAnnotation;
import org.semanticweb.owl.model.OWLObjectAnnotation;
import uk.ac.ebi.microarray.ontology.IClassAnnotationVisitor;
import static uk.ac.ebi.microarray.ontology.efo.Utils.*;

import java.util.*;

/**
 * @author Anna Zhukova
 *         Visits annotations of the EFO ontology class,
 *         stores usefull information and then is able to create appropriate node.
 */
public class EFOClassAnnotationVisitor implements IClassAnnotationVisitor<EFONode>
{

    private Map<String, Set<String>> nameToAlternativesMap;

    private String term;
    private boolean isBranchRoot;
    private boolean isOrganizational;
    private Set<String> alternatives = new HashSet<String>();

    public EFOClassAnnotationVisitor()
    {
        this(new HashMap<String, Set<String>>());
    }

    public EFOClassAnnotationVisitor( Map<String, Set<String>> nameToAlternativesMap )
    {
        this.nameToAlternativesMap = nameToAlternativesMap;
    }

    /**
     * Visits the given annotation. If it is label, "branch_class", "organizational_class",
     * "ArrayExpress_label" or "alternative_term" one, stores corresponding information.
     *
     * @param annotation Annotation to visit.
     */
    public void visit( OWLConstantAnnotation annotation )
    {
        if (annotation.isLabel()) {
            OWLConstant c = annotation.getAnnotationValue();
            if (term == null)
                term = c.getLiteral();
        } else if (annotation.getAnnotationURI().toString().contains("branch_class")) {
            isBranchRoot = Boolean.valueOf(annotation.getAnnotationValue().getLiteral());
        } else if (annotation.getAnnotationURI().toString().contains("organizational_class")) {
            isOrganizational = Boolean.valueOf(annotation.getAnnotationValue().getLiteral());
        } else if (annotation.getAnnotationURI().toString().contains("ArrayExpress_label")) {
            term = annotation.getAnnotationValue().getLiteral();
        } else if (annotation.getAnnotationURI().toString().contains("alternative_term")) {
            alternatives.add(annotation.getAnnotationValue().getLiteral());
        }
    }

    /**
     * Ignores.
     *
     * @param annotation Annotation to ignore.
     */
    public void visit( OWLObjectAnnotation annotation )
    {
    }

    /**
     * Returns label of just visited class.
     *
     * @return Label of just visited class.
     */
    public String getTerm()
    {
        return term;
    }

    /**
     * Returns if just visited class is annotated as a banch root.
     *
     * @return If just visited class is annotated as a banch root.
     */
    public boolean isBranchRoot()
    {
        return isBranchRoot;
    }

    /**
     * Returns if just visited class is annotated as an organizational one.
     *
     * @return If just visited class is annotated as an organizational one.
     */
    public boolean isOrganizational()
    {
        return isOrganizational;
    }

    /**
     * Returns set of "alternative_term" annotations for just visited class.
     *
     * @return Set of "alternative_term" annotations for just visited class.
     */
    public Set<String> getAlternatives()
    {
        return alternatives;
    }

    /**
     * Given a class if creates a EFONode.
     *
     * @param id Id of the ontology class given.
     * @return EFONode created.
     */
    public EFONode getOntologyNode( String id )
    {
        EFONode node = new EFONode(id, getTerm(), isBranchRoot());
        if (!isOrganizational()) {
            updateAlternativesMap(getTerm());
        }
        return node;
    }

    /**
     * Given the node corresponding to the ontology class and nodes corresponding to its children
     * and information if this class is organisational updates this ontology node children set
     * and their parents sets (if the given node is not organizational).
     *
     * @param node                 IOntologyNode given.
     * @param children             Child nodes.
     * @param isNodeOrganizational If the given node is organizational.
     */
    public void updateOntologyNode( EFONode node, Collection<EFONode> children, boolean isNodeOrganizational )
    {
        for (EFONode child : children) {
            node.getChildren().add(child);
            if (!isNodeOrganizational) {
                child.getParents().add(node);
            }
        }
    }

    /**
     * Given a term updates alternative map.
     *
     * @param term Given term.
     */
    private void updateAlternativesMap( String term )
    {
        String loweredTerm = normalizeString(term);
        if (!isStopWord(loweredTerm)) {
            Set<String> alternatives = getAlternatives();
            if (!alternatives.isEmpty()) {
                Set<String> existingAlternatives = nameToAlternativesMap.get(loweredTerm);
                if (existingAlternatives == null) {
                    existingAlternatives = new HashSet<String>();
                    nameToAlternativesMap.put(loweredTerm, existingAlternatives);
                }
                for (String alternativeTerm : alternatives) {
                    String loweredAlternativeTerm = totallyNormalizeString(alternativeTerm);
                    if (isStopWord(loweredAlternativeTerm)) {
                        continue;
                    }
                    if (loweredTerm.equals(loweredAlternativeTerm)) {
                        continue;
                    }
                    existingAlternatives.add(loweredAlternativeTerm);
                    Set<String> alternativesToAlternative = nameToAlternativesMap.get(loweredAlternativeTerm);
                    if (alternativesToAlternative == null) {
                        alternativesToAlternative = new HashSet<String>();
                        nameToAlternativesMap.put(loweredAlternativeTerm, alternativesToAlternative);
                    }
                    alternativesToAlternative.add(loweredTerm);
                }
            }
        }
    }

    /**
     * Returns map node name (term) -> set of alternative names.
     *
     * @return Unmodifiable map node name (term) -> set of alternative names.
     */
    public Map<String, Set<String>> getNameToAlternativesMap()
    {
        return Collections.unmodifiableMap(nameToAlternativesMap);
    }
}
