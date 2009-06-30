package uk.ac.ebi.arrayexpress.utils.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRecord
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Long     id;
    private String   name;
    private String   password;
    private String   email;
    private boolean  isPrivileged;

    public UserRecord( Long _id, String _name, String _password, String _email, boolean _isPrivileged )
    {
        id = _id;
        name = _name;
        password = _password;
        email = _email;
        isPrivileged = _isPrivileged;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getPassword()
    {
        return password;
    }

    public String getEmail()
    {
        return email;
    }

    public boolean isPrivileged()
    {
        return isPrivileged;
    }
}
