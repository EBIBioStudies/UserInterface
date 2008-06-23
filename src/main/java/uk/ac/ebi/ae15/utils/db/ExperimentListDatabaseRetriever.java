package uk.ac.ebi.ae15.utils.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExperimentListDatabaseRetriever extends SqlStatementExecutor
{
    // logging facility
    private final Log log = LogFactory.getLog(getClass());
    // sql to get a list of experiments from the database
    // (the parameter is either 0 for all experiments and 1 for public only)
    private final static String getExperimentListSql = "select distinct e.id" +
            " from tt_experiment e" +
            "  left outer join tt_extendable ext on ext.id = e.id" +
            "  left outer join pl_visibility v on v.label_id = ext.label_id" +
            "  where v.user_id = 1 or 0 = ?" +
            " order by" +
            "  e.id asc";

    // experiment list
    private List<Long> experimentList;
    // should I list experiments that are public?
    private boolean shouldListOnlyPublic;

    public ExperimentListDatabaseRetriever( DataSource ds, boolean publicOnly )
    {
        super(ds, getExperimentListSql);
        experimentList = new ArrayList<Long>();
        shouldListOnlyPublic = publicOnly;
    }

    public List<Long> getExperimentList()
    {
        if (!execute(false)) {
            log.error("There was a problem retrieving the list of experiments, check log for errors or exceptions");
        }
        return experimentList;
    }

    protected void setParameters( PreparedStatement stmt ) throws SQLException
    {
        stmt.setInt(1, shouldListOnlyPublic ? 1 : 0);
    }

    protected void processResultSet( ResultSet resultSet ) throws SQLException
    {
        while ( resultSet.next() ) {
            experimentList.add(resultSet.getLong(1));
        }
    }
}

