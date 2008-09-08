package uk.ac.ebi.ae15.utils.users;

import java.util.HashMap;

public class UserList extends HashMap<String, UserRecord>
{
    public UserRecord get( Object key )
    {
        return super.get(((String)key).toLowerCase());
    }

    public boolean containsKey( Object key )
    {
        return super.containsKey(((String)key).toLowerCase());
    }

    public UserRecord put( String key, UserRecord value )
    {
        return super.put(key.toLowerCase(), value);
    }
}
