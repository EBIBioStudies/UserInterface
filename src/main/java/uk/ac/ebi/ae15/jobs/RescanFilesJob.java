package uk.ac.ebi.ae15.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import uk.ac.ebi.ae15.AEInterfaceApplication;
import uk.ac.ebi.ae15.DownloadableFilesRegistry;

public class RescanFilesJob implements InterruptableJob, StatefulJob
{
    // logging facitlity
    private final Log log = LogFactory.getLog(getClass());
    // worker thread object
    private Thread myThread;

    public void execute(JobExecutionContext jec) throws JobExecutionException
    {
        myThread = Thread.currentThread();
        try {
            AEInterfaceApplication app = (AEInterfaceApplication) jec.getJobDetail().getJobDataMap().get("application");
            if (null != app) {
                ((DownloadableFilesRegistry) app.getComponent("DownloadableFilesRegistry")).rescan();
            }
        } catch (InterruptedException x) {
            log.debug("Job [" + jec.getJobDetail().getFullName() + "] was interrupted");
        }
        myThread = null;
    }

    public void interrupt() throws UnableToInterruptJobException
    {
        log.debug("Attempting to interrupt job");
        if (null != myThread)
            myThread.interrupt();
    }
}
