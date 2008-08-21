package uk.ac.ebi.ae15.utils.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PersistableStringList extends ArrayList<String> implements Persistable
{
    public PersistableStringList()
    {
    }

    public PersistableStringList( List<String> listToCopy )
    {
        super(listToCopy);    
    }

    public String toPersistence()
    {
        StringBuilder sb = new StringBuilder();

        for ( String entry : this ) {
            sb.append(entry).append('\n');
        }

        return sb.toString();
    }

    public void fromPersistence( String str )
    {
        this.clear();
        this.addAll(Arrays.asList(str.split("\n")));
    }

    public boolean shouldLoadFromPersistence()
    {
        return (0 == this.size());
    }}

