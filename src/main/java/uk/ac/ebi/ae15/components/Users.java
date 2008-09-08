package uk.ac.ebi.ae15.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.misc.BASE64Encoder;
import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.app.ApplicationComponent;
import uk.ac.ebi.ae15.utils.persistence.PersistableUserList;
import uk.ac.ebi.ae15.utils.persistence.TextFilePersistence;
import uk.ac.ebi.ae15.utils.users.UserList;
import uk.ac.ebi.ae15.utils.users.UserRecord;

import java.io.File;
import java.security.MessageDigest;

public class Users extends ApplicationComponent
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private TextFilePersistence<PersistableUserList> userList;

    public Users( Application app )
    {
        super(app, "Users");
    }

    public void initialize()
    {
        userList = new TextFilePersistence<PersistableUserList>(
                new PersistableUserList()
                , new File(
                    System.getProperty("java.io.tmpdir")
                    , getPreferences().getString("ae.users.cache.filename")
                )
        );
    }

    public void terminate()
    {
    }

    public void setUserList( UserList _userList )
    {
        userList.setObject(new PersistableUserList(_userList));
    }

    public String hashLogin( String username, String password )
    {
        if ( userList.getObject().containsKey(username) && userList.getObject().get(username).password.equals(password)) {
            return hashPassword(password);
        }
        // otherwise
        return null;
    }

    public boolean verifyLogin( String username, String hash )
    {
        return (userList.getObject().containsKey(username) && verifyHash(userList.getObject().get(username).password, hash));
    }

    public UserRecord getUserRecord( String username )
    {
        return userList.getObject().get(username);    
    }

    private boolean verifyHash( String password, String hash )
    {
        return hashPassword(password).equals(hash);
    }

    private String hashPassword( String password )
    {
        String hash = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("sha-512");
            byte[] hashBytes = digest.digest(password.getBytes());
            hash = new BASE64Encoder().encode(hashBytes);
        } catch ( Throwable x ) {
            log.error("Caught an exception:", x);
        }
        return hash;
    }
}
