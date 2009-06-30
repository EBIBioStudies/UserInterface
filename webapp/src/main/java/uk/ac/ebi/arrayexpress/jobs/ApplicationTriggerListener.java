package uk.ac.ebi.arrayexpress.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationTriggerListener implements TriggerListener
{
   // logging facitlity
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String runningJob = "";

    public String getName()
    {
        return "trigger-listener";
    }

    public void triggerFired(Trigger trigger, JobExecutionContext context)
    {
    }

    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context)
    {
        if (!context.getJobDetail().getName().equals("retrieve-xml")) {

            while (!registerExclusiveRunningJob(context)) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException x) {
                    logger.debug("Job has been interrupted while waiting", x);
                    return true;
                }
            }
        }
        boolean isShuttingDown = false;

        try {
            isShuttingDown = context.getScheduler().isInStandbyMode();
        } catch (SchedulerException x) {
            logger.error("Caught an exception:", x);
        }
        
        return isShuttingDown;
    }

    public void triggerMisfired(Trigger trigger)
    {
        // nothing here yet
    }

    public void triggerComplete(Trigger trigger, JobExecutionContext context, int triggerInstructionCode)
    {
        deregisterRunningJob(context);
    }

    private synchronized boolean registerExclusiveRunningJob(JobExecutionContext context)
    {
        if (!runningJob.equals("")) {
            return false;   // some job is already running
        } else {
            runningJob = context.getJobDetail().getName();
            return true;    // success
        }
    }

    private synchronized void deregisterRunningJob(JobExecutionContext context)
    {
        if (context.getJobDetail().getName().equals(runningJob)) {
            runningJob = "";
        }
    }

}
