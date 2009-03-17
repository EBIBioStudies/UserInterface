package uk.ac.ebi.arrayexpress.utils.persistence;

/**
 * Persistable class implements two basic functions
 * <p/>
 * - toPersisence() that creates a String based upon object contents
 * - fromPersistence( String ) that initializes an Object based upon a String parameter
 * - shouldLoadFromPersistence() that returns true if object is just created and an attempt
 * to load from persistence should be made
 */
public interface Persistable
{
    public String toPersistence();

    public void fromPersistence( String str );

    public boolean shouldLoadFromPersistence();

    // our internal representation of EOL
    public final char EOL = '\n';
}
