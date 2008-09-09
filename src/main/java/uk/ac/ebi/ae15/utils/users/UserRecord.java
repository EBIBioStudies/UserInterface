package uk.ac.ebi.ae15.utils.users;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.ae15.utils.CookieCustomBase64Encoder;

import java.security.MessageDigest;

public class UserRecord
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

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

    public String getPasswordHash()
    {
        String hash = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("sha-512");
            byte[] hashBytes = digest.digest(password.getBytes());
            hash = new String(CookieCustomBase64Encoder.encode(hashBytes));
        } catch ( Throwable x ) {
            log.error("Caught an exception:", x);
        }
        return hash;
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
