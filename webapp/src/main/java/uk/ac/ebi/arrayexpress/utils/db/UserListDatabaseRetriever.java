package uk.ac.ebi.arrayexpress.utils.db;

import uk.ac.ebi.arrayexpress.utils.users.UserList;
import uk.ac.ebi.arrayexpress.utils.users.UserRecord;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class UserListDatabaseRetriever extends SqlStatementExecutor
{
    // logging facility
    private final Logger logger = LoggerFactory.getLogger(getClass());
    // sql to get a list of experiments from the database
    // (the parameter is either 0 for all experiments and 1 for public only)
    private final static String getUserListSql = "select distinct id, name, password, email, priviledge" +
            " from" +
            "  pl_user" +
            " order by" +
            "  id asc";

    // user list
    private UserList userList;

    public UserListDatabaseRetriever( DataSource ds )
    {
        super(ds, getUserListSql);
        userList = new UserList();
    }

    public UserList getUserList()
    {
        if (!execute(false)) {
            logger.error("There was a problem retrieving the list of experiments, check log for errors or exceptions");
        }
        return userList;
    }

    protected void setParameters( PreparedStatement stmt ) throws SQLException
    {
        // nothing to do here
    }

    protected void processResultSet( ResultSet resultSet ) throws SQLException
    {
        while ( resultSet.next() ) {
            UserRecord userRecord = new UserRecord(resultSet.getLong(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getBoolean(5));

            userList.put(userRecord.getName(), userRecord);
        }
    }
}

