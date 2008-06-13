package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

public class ReloadExperimentsJob implements InterruptableJob
{
    private final Log log = LogFactory.getLog(getClass());
    private Thread currentThread;

    public void execute( JobExecutionContext jec ) throws JobExecutionException
    {
        currentThread = Thread.currentThread();
        log.info("Yo! I am being called!");
        try {
            Thread.sleep(1000*60);
        } catch ( Exception x ) {
            log.error("Caught an exception:", x);
        }
        log.info("Yo! was called!");
        currentThread = null;
    }

    public void interrupt() throws UnableToInterruptJobException
    {
        log.info("Yo! I am being interrupted!");
        if ( null != currentThread )
            currentThread.interrupt();
    }
}