package uk.ac.ebi.arrayexpress.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.search.EFOQueryExpander;
import uk.ac.ebi.microarray.ontology.efo.EFOOntologyHelper;

import java.util.Map;
import java.util.Set;

public class Ontologies extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, Set<String>> efoFullExpansionMap;
    private Map<String, Set<String>> efoSynonymMap;

    public Ontologies()
    {
        super("Ontologies");
    }

    public void initialize()
    {
        try {
            EFOOntologyHelper efoHelper = new EFOOntologyHelper(this.getApplication().getResource("/WEB-INF/classes/efo.owl").openStream());

            efoFullExpansionMap = efoHelper.getFullOntologyExpansionMap();
            efoSynonymMap = efoHelper.getSynonymMap();

            ((SearchEngine)getComponent("SearchEngine")).getController().setQueryExpander(new EFOQueryExpander());

        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
    }

    public void terminate()
    {
    }
}
