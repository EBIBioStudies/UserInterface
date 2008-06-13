package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.servlet.ServletContext;

public class Application
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private ServletContext servletContext;

    private Preferences preferences;
    private Experiments experiments;
    private DownloadableFilesDirectory filesDirectory;
    private XsltHelper xsltHelper;
    private Scheduler quartzScheduler;

    public Application( ServletContext context )
    {
        servletContext = context;

        preferences = new Preferences(this);
        experiments = new Experiments(this);
        filesDirectory = new DownloadableFilesDirectory();
        xsltHelper = new XsltHelper(this);

        // load application preferences
        preferences.load();

        try {
            startScheduler();
        } catch ( Throwable x ) {
            log.error("Caught an exception:", x);
        }
    }

    public void releaseComponents()
    {
        try {
            quartzScheduler.interrupt("job","group");
            quartzScheduler.shutdown(true);
        } catch ( Throwable x ) {
            log.error("Caught an exception:", x);
        }

        quartzScheduler = null;
        preferences = null;
        experiments = null;
        filesDirectory = null;
        xsltHelper = null;
        servletContext = null;
    }

    public Preferences getPreferences()
    {
        return preferences;
    }

    public Experiments getExperiments()
    {
        return experiments;
    }


    public DownloadableFilesDirectory getFilesDirectory()
    {
        return filesDirectory;
    }

    public XsltHelper getXsltHelper()
    {
        return xsltHelper;
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }

    private void startScheduler() throws SchedulerException
    {
        // Retrieve a scheduler from schedule factory
        quartzScheduler =new StdSchedulerFactory().getScheduler();

        // current time
        long ctime = System.currentTimeMillis();

        // Initiate JobDetail with job name, job group, and executable job class
        JobDetail jobDetail =
        	new JobDetail("job", "group", ReloadExperimentsJob.class);
        // Initiate CronTrigger with its name and group name
        CronTrigger cronTrigger = new CronTrigger("cronTrigger", "triggerGroup2");
        try {
            // setup CronExpression
            CronExpression cexp = new CronExpression("0/20 * * * * ?");
            // Assign the CronExpression to CronTrigger
            cronTrigger.setCronExpression(cexp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // schedule a job with JobDetail and Trigger
        quartzScheduler.scheduleJob(jobDetail, cronTrigger);

        // start the scheduler
        quartzScheduler.start();
    }

}
