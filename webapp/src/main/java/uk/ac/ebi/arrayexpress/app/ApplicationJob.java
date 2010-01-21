package uk.ac.ebi.arrayexpress.app;

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
            doExecute(jec);
        } catch ( InterruptedException x ) {
            logger.debug("Job [{}] was interrupted", jec.getJobDetail().getFullName());
        } catch ( Error x ) {
            logger.error("[SEVERE] Runtime error while executing job [" + jec.getJobDetail().getFullName() + "]:", x);
            Application.getInstance().sendExceptionReport("[SEVERE] Runtime error while executing job [" + jec.getJobDetail().getFullName() + "]", x);
            throw new JobExecutionException(x);
        }
        myThread = null;
    }

    public abstract void doExecute( JobExecutionContext jec ) throws InterruptedException;

    public void interrupt() throws UnableToInterruptJobException
    {
        logger.debug("Attempting to interrupt job");
        if (null != myThread)
            myThread.interrupt();
    }
}