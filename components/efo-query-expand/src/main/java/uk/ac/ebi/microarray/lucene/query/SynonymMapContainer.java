package uk.ac.ebi.microarray.lucene.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Anna Zhukova
 * Map String -> Set of its synonyms container
 */
public class SynonymMapContainer {
    /**
     * Empty set.
     */
    public static final Set<String> EMPTY = new HashSet<String>(0);
    /**
     * Map with no strings and no synonyms.
     */
    public static Map<String, Set<String>> EMPTY_WORD_2_SYNONYMS = new HashMap<String, Set<String>>();

    private Map<String, Set<String>> word2Synonyms = new HashMap<String, Set<String>>();

    /**
     * Returns set of synonyms to the given string.
     * @param str String synonyms to that are being looked for .
     * @return Set of synonyms.
     * */
    public Set<String> getSynonyms(String str) {
        Set<String> result = word2Synonyms.get(str);
        return (result == null) ? EMPTY : result;
    }

    /**
     * Returns string to synonyms map.
     * @return Map string -> Set of its synonyms.
     * */
    public Map<String, Set<String>> getWord2Synonyms() {
        return word2Synonyms;
    }

    /**
     * Sets string to synonyms map to the given map if it is not null
     *  or empty map otherwise.
     * @param word2Synonyms Map string -> Set of its synonyms to be set.
     * */
    public void setWord2Synonyms(Map<String, Set<String>> word2Synonyms) {
        this.word2Synonyms = word2Synonyms != null ? word2Synonyms : EMPTY_WORD_2_SYNONYMS;
    }
}

