package uk.ac.ebi.ae15.utils.files;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class EntriesCollectionMap extends HashMap<String, Collection<Integer>>
{
    public void putEntry( String key, Integer value )
    {
        if (!containsKey(key)) {
            put(key, new HashSet<Integer>());
        }
        get(key).add(value);
    }
}
