package uk.ac.ebi.ae15.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.components.Experiments;
import uk.ac.ebi.ae15.components.JobsController;
import uk.ac.ebi.ae15.utils.db.DataSourceFinder;
import uk.ac.ebi.ae15.utils.db.ExperimentListDatabaseRetriever;

import javax.sql.DataSource;
import java.util.List;

public class ReloadExperimentsJob extends ApplicationJob implements JobListener
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private List<Long> exps;
    private DataSource ds;
    private StringBuffer xmlBuffer;

    private int numThreadsCompleted;
    private int expsPerThread;

    public void execute( Application app ) throws InterruptedException
    {
        if (null != app) {
            Long threads = app.getPreferences().getLong("ae.experiments.reload.threads");
            if (null != threads) {
                int numThreadsForRetrieval = threads.intValue();
                numThreadsCompleted = 0;
                xmlBuffer = new StringBuffer(20000000);

                String dsNames = ((Experiments) app.getComponent("Experiments")).getDataSource();
                log.info("Reload of experiment data from [" + dsNames + "] requested");

                ds = new DataSourceFinder().findDataSource(dsNames);
                if (null != ds) {
                    exps = new ExperimentListDatabaseRetriever(
                            ds,
                            app.getPreferences().getBoolean("ae.experiments.publiconly")
                    ).getExperimentList();
                    Thread.sleep(1);

                    log.info("Got [" + String.valueOf(exps.size()) + "] experiments listed in the database, scheduling retrieval");
                    xmlBuffer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><experiments total=\"").append(exps.size()).append("\">");

                    ((JobsController) app.getComponent("JobsController")).setJobListener(this);
                    // split list into several pieces
                    expsPerThread = (int) Math.floor(exps.size() / numThreadsForRetrieval) + 1;
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
                    log.info("Reload of experiment data completed");
                    xmlBuffer = null;
                } else {
                    log.warn("No data sources available, reload aborted");
                }
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
                log.error("Caught an exception:", x);
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

    private void startXmlRetrieval( int index, List<Long> exps )
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Starting subjob [").append(index).append("], list size [").append(exps.size()).append("]");
        log.debug(sb.toString());

    }

    private synchronized void incrementCompletedThreadsCounter()
    {
        numThreadsCompleted++;
    }
}
