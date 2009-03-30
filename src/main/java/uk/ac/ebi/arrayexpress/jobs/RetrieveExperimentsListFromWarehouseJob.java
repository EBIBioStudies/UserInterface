package uk.ac.ebi.arrayexpress.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationJob;
import uk.ac.ebi.arrayexpress.components.Experiments;
import uk.ac.ebi.arrayexpress.utils.db.DataSourceFinder;
import uk.ac.ebi.arrayexpress.utils.db.ExperimentListInWarehouseDatabaseRetriever;

import javax.sql.DataSource;
import java.util.List;

public class RetrieveExperimentsListFromWarehouseJob  extends ApplicationJob
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void execute() throws InterruptedException
    {
        List<String> exps;
        DataSource ds;

        Application app = Application.getInstance();
        String dsNames = app.getPreferences().getString("ae.warehouseexperiments.datasources");
        logger.info("Reload of warehouse experiment data from [{}] requested", dsNames);

        ds = new DataSourceFinder().findDataSource(dsNames);
        if (null != ds) {
            exps = new ExperimentListInWarehouseDatabaseRetriever(ds).getExperimentList();
            Thread.sleep(1);

            logger.info("Got [{}] experiments listed in the warehouse", exps.size());
            ((Experiments) app.getComponent("Experiments")).setExperimentsInWarehouse(exps);
            logger.info("Reload of warehouse experiment data completed");
        } else {
            logger.warn("No data sources available, reload aborted");
        }
    }
}
