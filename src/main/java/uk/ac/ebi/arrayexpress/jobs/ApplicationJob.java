package uk.ac.ebi.arrayexpress.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import uk.ac.ebi.arrayexpress.app.Application;

abstract public class ApplicationJob implements InterruptableJob, StatefulJob
{
    // logging facitlity
    private final Log log = LogFactory.getLog(getClass());
    // worker thread object
    private Thread myThread;

    public void execute( JobExecutionContext jec ) throws JobExecutionException
    {
        myThread = Thread.currentThread();
        try {
            Application app = (Application) jec.getJobDetail().getJobDataMap().get("application");
            execute(app);
        } catch ( InterruptedException x ) {
            log.debug("Job [" + jec.getJobDetail().getFullName() + "] was interrupted");
        }
        myThread = null;
    }

    public abstract void execute( Application app ) throws InterruptedException;

    public void interrupt() throws UnableToInterruptJobException
    {
        log.debug("Attempting to interrupt job");
        if (null != myThread)
            myThread.interrupt();
    }
}