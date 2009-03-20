package uk.ac.ebi.arrayexpress.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;

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
            execute();
        } catch ( InterruptedException x ) {
            log.debug("Job [" + jec.getJobDetail().getFullName() + "] was interrupted");
        }
        myThread = null;
    }

    public abstract void execute() throws InterruptedException;

    public void interrupt() throws UnableToInterruptJobException
    {
        log.debug("Attempting to interrupt job");
        if (null != myThread)
            myThread.interrupt();
    }
}