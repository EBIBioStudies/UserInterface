package uk.ac.ebi.arrayexpress.utils.search;

import org.apache.lucene.search.Query;

import java.util.List;
import java.util.Set;

public interface IEFOExpansionLookup
{
    public List<Set<String>> getExpansionTerms( Query query );
}
