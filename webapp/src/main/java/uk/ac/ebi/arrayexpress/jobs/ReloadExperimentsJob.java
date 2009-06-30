package uk.ac.ebi.arrayexpress.jobs;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationJob;
import uk.ac.ebi.arrayexpress.components.Experiments;
import uk.ac.ebi.arrayexpress.components.JobsController;
import uk.ac.ebi.arrayexpress.components.Users;
import uk.ac.ebi.arrayexpress.utils.db.DataSourceFinder;
import uk.ac.ebi.arrayexpress.utils.db.ExperimentListDatabaseRetriever;
import uk.ac.ebi.arrayexpress.utils.db.UserListDatabaseRetriever;
import uk.ac.ebi.arrayexpress.utils.users.UserList;

import javax.sql.DataSource;
import java.util.List;

public class ReloadExperimentsJob extends ApplicationJob implements JobListener
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<Long> exps;
    private DataSource ds;
    private StringBuffer xmlBuffer;

    private int numThreadsCompleted;
    private int expsPerThread;

    public void execute() throws InterruptedException
    {
        Application app = Application.getInstance();
        Long threads = app.getPreferences().getLong("ae.experiments.reload.threads");
        if (null != threads) {
            int numThreadsForRetrieval = threads.intValue();
            numThreadsCompleted = 0;
            xmlBuffer = new StringBuffer(20000000);

            String dsNames = ((Experiments) app.getComponent("Experiments")).getDataSource();
            logger.info("Reload of experiment data from [{}] requested", dsNames);

            ds = new DataSourceFinder().findDataSource(dsNames);
            if (null != ds) {
                UserList userList = new UserListDatabaseRetriever(ds).getUserList();
                ((Users)app.getComponent("Users")).setUserList(userList);
                logger.info("Reloaded the user list from the database");

                exps = new ExperimentListDatabaseRetriever(ds).getExperimentList();
                Thread.sleep(1);

                logger.info("Got [{}] experiments listed in the database, scheduling retrieval", exps.size());
                xmlBuffer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><experiments total=\"").append(exps.size()).append("\">");

                ((JobsController) app.getComponent("JobsController")).setJobListener(this);

                if (0 < exps.size()) {
                    if (exps.size() <= numThreadsForRetrieval) {
                        numThreadsForRetrieval = 1;
                    }
                    // split list into several pieces
                    expsPerThread = (int) Math.ceil(((double)exps.size()) / ((double)numThreadsForRetrieval));
                    for ( int i = 0; i < numThreadsForRetrieval; ++i ) {
                        ((JobsController) app.getComponent("JobsController")).executeJob("retrieve-xml", i);
                        Thread.sleep(1);
                    }

                    while ( numThreadsCompleted < numThreadsForRetrieval ) {
                        Thread.sleep(1000);
                    }

                    ((JobsController) app.getComponent("JobsController")).setJobListener(null);
                    xmlBuffer.append("</experiments>");
                    ((Experiments) app.getComponent("Experiments")).reload(xmlBuffer.toString().replaceAll("[^\\p{Print}]", " "));
                    logger.info("Reload of experiment data completed");
                    xmlBuffer = null;
                } else {
                    logger.warn("No experiments found, reload aborted");
                }
            } else {
                logger.warn("No data sources available, reload aborted");
            }
        }
    }

    // jobListener support
    public String getName()
    {
        return "job-listener";
    }

    public void jobToBeExecuted( JobExecutionContext jec )
    {
        if (jec.getJobDetail().getName().equals("retrieve-xml")) {
            JobDataMap jdm = jec.getMergedJobDataMap();
            int index = jdm.getInt("index");
            jdm.put("xmlBuffer", xmlBuffer);
            jdm.put("ds", ds);
            jdm.put("exps", exps.subList(index * expsPerThread, Math.min(((index + 1) * expsPerThread), exps.size())));
        }
    }

    public void jobExecutionVetoed( JobExecutionContext jec )
    {
        if (jec.getJobDetail().getName().equals("retrieve-xml")) {
            try {
                interrupt();
            } catch ( Throwable x ) {
                logger.error("Caught an exception:", x);
            }
        }
    }

    public void jobWasExecuted( JobExecutionContext jec, JobExecutionException jobException )
    {
        if (jec.getJobDetail().getName().equals("retrieve-xml")) {
            JobDataMap jdm = jec.getMergedJobDataMap();
            jdm.remove("xmlObject");
            jdm.remove("ds");
            jdm.remove("exps");

            incrementCompletedThreadsCounter();
        }
    }

    private synchronized void incrementCompletedThreadsCounter()
    {
        numThreadsCompleted++;
    }
}
