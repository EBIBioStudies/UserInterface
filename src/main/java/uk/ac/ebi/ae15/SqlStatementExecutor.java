package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SqlStatementExecutor
{
    // logging facility
    private final Log log = LogFactory.getLog(getClass());

    // statement
    private PreparedStatement statement;

    public SqlStatementExecutor( String dsName, String sql )
    {
        statement = prepareStatement(dsName, sql);
    }

    protected boolean execute( boolean shouldRetainConnection )
    {
        boolean result = false;
        if ( null != statement ) {
            ResultSet rs = null;
            try {
                setParameters(statement);
                rs = statement.executeQuery();
                processResultSet(rs);
                result = true;
            } catch ( Throwable x ) {
                log.error("Caught an exception:", x);
            } finally {
                if ( null != rs ) {
                    try {
                        rs.close();
                    } catch ( SQLException x ) {
                        log.error("Caught an exception:", x);

                    }
                }

                if ( !shouldRetainConnection ) {
                    closeConnection();
                }
            }
        } else {
            log.error("Statement is null, please check the log for possible exceptions");
        }
        return result;
    }

    // overridable method that would allow user to set additional parameters (if any)
    protected abstract void setParameters( PreparedStatement stmt ) throws SQLException;
    // overridable method that would allow user to parse the result set upon successful execution 
    protected abstract void processResultSet( ResultSet resultSet ) throws SQLException;

    private PreparedStatement prepareStatement( String dsName, String sql )
    {
        PreparedStatement stmt = null;
        DataSource ds = getDataSource(dsName);
        if ( null != ds )
        {
            try {
                Connection conn = ds.getConnection();
                stmt = conn.prepareStatement(sql);
            } catch ( SQLException x ) {
                log.error("Caught an exception:", x);
            }
        }

        return stmt;
    }

    private void closeConnection()
    {
        if ( null != statement ) {
            try {
                statement.getConnection().close();
            } catch ( SQLException x ) {
                log.error("Caught an exception:", x);
            }
            statement = null;
        }
    }

    private DataSource getDataSource( String dataSourceName )
    {
        DataSource ds = null;

        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");

            ds = (DataSource) envContext.lookup("jdbc/" + dataSourceName.toLowerCase());
        } catch (Throwable x) {
            log.error("Caught an exception:", x);
        }

        return ds;
    }
}
