package uk.ac.ebi.microarray.app;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class ApplicationJob implements InterruptableJob, StatefulJob
{
    // logging facitlity
    private final Logger logger = LoggerFactory.getLogger(getClass());
    // worker thread object
    private Thread myThread;

    public void execute( JobExecutionContext jec ) throws JobExecutionException
    {
        myThread = Thread.currentThread();
        try {
            execute();
        } catch ( InterruptedException x ) {
            logger.debug("Job [{}] was interrupted", jec.getJobDetail().getFullName());
        }
        myThread = null;
    }

    public abstract void execute() throws InterruptedException;

    public void interrupt() throws UnableToInterruptJobException
    {
        logger.debug("Attempting to interrupt job");
        if (null != myThread)
            myThread.interrupt();
    }
}