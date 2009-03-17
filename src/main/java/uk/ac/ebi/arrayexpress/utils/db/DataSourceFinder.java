package uk.ac.ebi.arrayexpress.utils.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.sql.DataSource;
import java.sql.Connection;

public class DataSourceFinder
{
    // logging facility
    private final Log log = LogFactory.getLog(getClass());

    public DataSource findDataSource( String dsNames )
    {
        DataSource result = null;
        if (null != dsNames) {
            String[] dsList = dsNames.trim().split("\\s*,\\s*");
            for ( String dsName : dsList ) {
                log.info("Checking data source [" + dsName + "]");
                result = getDataSource(dsName);
                if (isDataSourceAvailable(result)) {
                    log.info("Will use available data source [" + dsName + "]");
                    break;
                } else {
                    log.warn("Data source [" + dsName + "] is unavailable");
                    result = null;
                }
            }
        }
        return result;
    }

    private DataSource getDataSource( String dsName )
    {
        DataSource ds = null;
        log.info("Looking up data source [" + dsName + "]");
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");

            ds = (DataSource) envContext.lookup("jdbc/" + dsName.toLowerCase());
        } catch ( NameNotFoundException x ) {
            log.error("Data source [" + dsName + "] is not regsitered with application, check your context.xml");
        } catch ( Throwable x ) {
            log.error("Caught an exception:", x);
        }

        return ds;
    }

    private boolean isDataSourceAvailable( DataSource ds )
    {
        boolean result = false;
        Connection conn = null;
        if (null != ds) {
            try {
                conn = ds.getConnection();
                conn.close();
                conn = null;
                result = true;
            } catch ( Throwable x ) {
                log.debug("Caught an exception [" + x.getMessage() + "]");
            } finally {
                if (null != conn) {
                    try {
                        conn.close();
                    } catch ( Throwable x ) {
                        log.error("Caught an exception:", x);
                    }
                }
            }
        }
        return result;
    }
}
