package uk.ac.ebi.ae15.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import uk.ac.ebi.ae15.Application;

public class RescanFilesJob implements InterruptableJob, StatefulJob
{
    // logging facitlity
    private final Log log = LogFactory.getLog(getClass());
    // worker thread object
    private Thread workerThread;

    public void execute(JobExecutionContext jec) throws JobExecutionException
    {
        workerThread = Thread.currentThread();
        try {
            Application app = (Application) jec.getJobDetail().getJobDataMap().get("application");
            if (null != app) {
                app.getFilesRegistry().rescan();
            }
        } catch (InterruptedException x) {
            log.debug("Job [" + jec.getJobDetail().getFullName() + "] was interrupted");
        }
        workerThread = null;
    }

    public void interrupt() throws UnableToInterruptJobException
    {
        log.debug("Attempting to interrupt job");
        if (null != workerThread)
            workerThread.interrupt();
    }
}
