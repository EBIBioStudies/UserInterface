package uk.ac.ebi.ae15.utils.users;

public class UserRecord
{
    public Long     id;
    public String   name;
    public String   password;
    public String   email;
    public boolean  isPrivileged;

    public UserRecord( Long _id, String _name, String _password, String _email, boolean _isPrivileged )
    {
        id = _id;
        name = _name;
        password = _password;
        email = _email;
        isPrivileged = _isPrivileged;
    }
}
