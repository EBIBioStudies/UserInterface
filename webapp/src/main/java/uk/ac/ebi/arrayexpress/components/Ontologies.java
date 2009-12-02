package uk.ac.ebi.arrayexpress.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.search.EFOExpansionLookupIndex;
import uk.ac.ebi.arrayexpress.utils.search.EFOQueryExpander;
import uk.ac.ebi.microarray.ontology.efo.EFOOntologyHelper;

import java.util.Map;
import java.util.Set;


public class Ontologies extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Ontologies()
    {
        super("Ontologies");
    }

    public void initialize()
    {
        try {
            EFOOntologyHelper efoHelper = new EFOOntologyHelper(this.getApplication().getResource("/WEB-INF/classes/efo.owl").openStream());

            Map<String, Set<String>> efoFullExpansionMap = efoHelper.getFullOntologyExpansionMap();
            Map<String, Set<String>> efoSynonymMap = efoHelper.getSynonymMap();

            EFOExpansionLookupIndex ix = new EFOExpansionLookupIndex(getPreferences().getString("ae.efo.index.location"));
            ix.addMaps(efoSynonymMap, efoFullExpansionMap);

            ((SearchEngine)getComponent("SearchEngine")).getController().setQueryExpander(new EFOQueryExpander(ix));

        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }
    }

    public void terminate()
    {
    }
}
