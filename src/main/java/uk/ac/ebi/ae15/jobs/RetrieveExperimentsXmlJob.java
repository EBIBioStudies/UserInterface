package uk.ac.ebi.ae15.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import uk.ac.ebi.ae15.utils.db.ExperimentXmlDatabaseRetriever;

import javax.sql.DataSource;
import java.util.List;

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
            JobDataMap jdm = jec.getMergedJobDataMap();
            DataSource ds = (DataSource) jdm.get("ds");
            List exps = (List) jdm.get("exps");
            StringBuffer xmlBuffer = (StringBuffer) jdm.get("xmlBuffer");
            xmlBuffer.append(
                    new ExperimentXmlDatabaseRetriever(
                            ds,
                            exps).getExperimentXml()
            );
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
