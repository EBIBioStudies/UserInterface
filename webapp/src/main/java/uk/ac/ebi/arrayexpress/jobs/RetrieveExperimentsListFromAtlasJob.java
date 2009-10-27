package uk.ac.ebi.arrayexpress.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationJob;
import uk.ac.ebi.arrayexpress.components.Experiments;
import uk.ac.ebi.arrayexpress.utils.db.DataSourceFinder;
import uk.ac.ebi.arrayexpress.utils.db.ExperimentListInAtlasDatabaseRetriever;

import javax.sql.DataSource;
import java.util.List;

public class RetrieveExperimentsListFromAtlasJob extends ApplicationJob
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void execute() throws InterruptedException
    {
        List<String> exps;
        DataSource ds;

        Application app = Application.getInstance();
        String dsNames = app.getPreferences().getString("ae.atlasexperiments.datasources");
        logger.info("Reload of list of Atlas experiments from [{}] requested", dsNames);

        ds = new DataSourceFinder().findDataSource(dsNames);
        if (null != ds) {
            exps = new ExperimentListInAtlasDatabaseRetriever(ds).getExperimentList();
            Thread.sleep(1);

            ((Experiments) app.getComponent("Experiments")).setExperimentsInAtlas(exps);
            logger.info("Got [{}] experiments listed in Atlas", exps.size());
        } else {
            logger.warn("No data sources available, reload aborted");
        }
    }
}
