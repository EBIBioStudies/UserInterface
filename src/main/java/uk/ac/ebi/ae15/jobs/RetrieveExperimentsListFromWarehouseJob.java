package uk.ac.ebi.ae15.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.components.Experiments;
import uk.ac.ebi.ae15.utils.db.DataSourceFinder;
import uk.ac.ebi.ae15.utils.db.ExperimentListInWarehouseDatabaseRetriever;

import javax.sql.DataSource;
import java.util.List;

public class RetrieveExperimentsListFromWarehouseJob  extends ApplicationJob
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    public void execute( Application app ) throws InterruptedException
    {
        List<String> exps;
        DataSource ds;

        if (null != app) {
            String dsNames = app.getPreferences().getString("ae.warehouseexperiments.datasources");
            log.info("Reload of warehouse experiment data from [" + dsNames + "] requested");

            ds = new DataSourceFinder().findDataSource(dsNames);
            if (null != ds) {
                exps = new ExperimentListInWarehouseDatabaseRetriever(ds).getExperimentList();
                Thread.sleep(1);

                log.info("Got [" + String.valueOf(exps.size()) + "] experiments listed in the warehouse");
                ((Experiments) app.getComponent("Experiments")).setExperimentsInWarehouse(exps);
                log.info("Reload of warehouse experiment data completed");
            } else {
                log.warn("No data sources available, reload aborted");
            }

        }
    }
}
