package uk.ac.ebi.ae15;

/**
 * StringPersistable class implements two basic functions
 *  - toPersisence() that creates a String based upon object contents
 *  - fromPersistence( String ) that initializes an Object based upon a String parameter
 *
 */
public interface StringPersistable {
    public String toPersistence();
    public void fromPersisence( String str );

    public boolean isEmpty();

    // our internal representation of EOL
    public final char EOL = '\n';
}
