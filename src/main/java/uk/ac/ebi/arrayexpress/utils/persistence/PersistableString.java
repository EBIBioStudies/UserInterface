package uk.ac.ebi.arrayexpress.utils.persistence;

public class PersistableString implements Persistable
{
    private String string;

    public PersistableString()
    {
        string = "";
    }

    public PersistableString( String str )
    {
        string = str;
    }

    public String get()
    {
        return string;
    }

    public void set( String str )
    {
        string = str;
    }

    public String toPersistence()
    {
        return string;
    }

    public void fromPersistence( String str )
    {
        string = str;
    }

    public boolean shouldLoadFromPersistence()
    {
        return (0 == string.length());
    }
}
