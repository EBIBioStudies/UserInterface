package uk.ac.ebi.arrayexpress.components;

import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableUserList;
import uk.ac.ebi.arrayexpress.utils.persistence.TextFilePersistence;
import uk.ac.ebi.arrayexpress.utils.users.UserList;
import uk.ac.ebi.arrayexpress.utils.users.UserRecord;
import uk.ac.ebi.microarray.arrayexpress.shared.auth.AuthenticationHelper;

import java.io.File;

public class Users extends ApplicationComponent
{
    private TextFilePersistence<PersistableUserList> userList;
    private AuthenticationHelper authHelper;

    public Users()
    {
        super("Users");
    }

    public void initialize()
    {
        userList = new TextFilePersistence<PersistableUserList>(
                new PersistableUserList()
                , new File(getPreferences().getString("ae.users.file.location"))
        );

        authHelper = new AuthenticationHelper();
    }

    public void terminate()
    {
    }

    public void setUserList( UserList _userList )
    {
        userList.setObject(new PersistableUserList(_userList));
    }

    public String hashLogin( String username, String password, String suffix )
    {
        if ( null != username && null != password && null != suffix
                && userList.getObject().containsKey(username) ) {
            UserRecord user = userList.getObject().get(username);
            if ( user.getPassword().equals(password) ) {
                return authHelper.generateHash(username, password, suffix);
            }
        }
        // otherwise
        return "";
    }

    public boolean verifyLogin( String username, String hash, String suffix )
    {
        if ( null != username && null != hash && null != suffix
                && userList.getObject().containsKey(username) ) {
            UserRecord user = userList.getObject().get(username);
            return authHelper.verifyHash(hash, username, user.getPassword(), suffix);
        }
        return false;
    }

    public UserRecord getUserRecord( String username )
    {
        return ( null != username ) ? userList.getObject().get(username) : null;
    }
}
