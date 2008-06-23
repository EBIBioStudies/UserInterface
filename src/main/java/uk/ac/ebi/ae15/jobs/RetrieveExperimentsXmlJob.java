package uk.ac.ebi.ae15.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

public class RetrieveExperimentsXmlJob implements InterruptableJob
{
    // logging facitlity
    private final Log log = LogFactory.getLog(getClass());
    // worker thread object
    private Thread myThread;

    public void execute( JobExecutionContext jec ) throws JobExecutionException
    {
        myThread = Thread.currentThread();
        try {
            Thread.sleep(1000);
            //DataSource ds = (DataSource) jec.getJobDetail().getJobDataMap().get("data-source");
            //List exps = (List) jec.getJobDetail().getJobDataMap().get("exps");
            //StringBuffer xmlBuffer = (StringBuffer) jec.getJobDetail().getJobDataMap().get("xml-buffer");
            //xmlBuffer.append(
            //        new ExperimentXmlDatabaseRetriever(
            //                ds,
            //                exps).getExperimentXml()
            //);
        } catch ( InterruptedException x ) {
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
