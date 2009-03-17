package uk.ac.ebi.arrayexpress.utils.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExperimentListInWarehouseDatabaseRetriever extends SqlStatementExecutor
{
    // logging facility
    private final Log log = LogFactory.getLog(getClass());
    // sql to get a list of experiments from the warehouse database
    private final static String getExperimentListSql =
            "select e.experiment_identifier from atlas a, ae1__experiment__main e\n" +
            "where a.experiment_id_key = e.experiment_id_key group by e.experiment_identifier having count(updn) > 0";

    // experiment list
    private List<String> experimentList;

    public ExperimentListInWarehouseDatabaseRetriever( DataSource ds )
    {
        super(ds, getExperimentListSql);
        experimentList = new ArrayList<String>();
    }

    public List<String> getExperimentList()
    {
        if (!execute(false)) {
            log.error("There was a problem retrieving the list of experiments, check log for errors or exceptions");
        }
        return experimentList;
    }

    protected void setParameters( PreparedStatement stmt ) throws SQLException
    {
    }

    protected void processResultSet( ResultSet resultSet ) throws SQLException
    {
        while ( resultSet.next() ) {
            experimentList.add(resultSet.getString(1));
        }
    }
}

