package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.jobs.RescanFilesJob;

import javax.servlet.ServletContext;

public class AEInterfaceApplication extends Application
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    private Preferences preferences;
    private Experiments experiments;
    private DownloadableFilesRegistry filesRegistry;
    private XsltHelper xsltHelper;
    private Scheduler quartzScheduler;

    public AEInterfaceApplication(ServletContext context)
    {
        super(context);

        preferences = new Preferences(this);
        preferences.load();

        experiments = new Experiments(this);

        filesRegistry = new DownloadableFilesRegistry(this);
        filesRegistry.setRootFolder(getPreferences().get("ae.files.root.location"));

        xsltHelper = new XsltHelper(this);


        try {
            startScheduler();
        } catch (Throwable x) {
            log.error("Caught an exception:", x);
        }
    }

    public void initializeComponents()
    {

    }

    public void terminateComponents()
    {
        try {
            quartzScheduler.interrupt("job", "group");
            quartzScheduler.shutdown(true);
        } catch (Throwable x) {
            log.error("Caught an exception:", x);
        }

        quartzScheduler = null;
        preferences = null;
        experiments = null;
        filesRegistry = null;
        xsltHelper = null;
    }

    public Preferences getPreferences()
    {
        return preferences;
    }

    public Experiments getExperiments()
    {
        return experiments;
    }


    public DownloadableFilesRegistry getFilesRegistry()
    {
        return filesRegistry;
    }

    public XsltHelper getXsltHelper()
    {
        return xsltHelper;
    }

    private void startScheduler() throws SchedulerException
    {
        // Retrieve a scheduler from schedule factory
        quartzScheduler = new StdSchedulerFactory().getScheduler();

        // Initiate JobDetail with job name, job group, and executable job class
        JobDetail jobDetail = new JobDetail("job", "group", RescanFilesJob.class);

        jobDetail.getJobDataMap().put("application", this);
        // Initiate CronTrigger with its name and group name
        CronTrigger cronTrigger = new CronTrigger("cronTrigger", "triggerGroup");
        try {
            // setup CronExpression
            CronExpression cexp = new CronExpression(getPreferences().get("ae.files.rescan.schedule"));
            // Assign the CronExpression to CronTrigger
            cronTrigger.setCronExpression(cexp);

            // schedule a job with JobDetail and Trigger
            quartzScheduler.scheduleJob(jobDetail, cronTrigger);

            if (getPreferences().get("ae.files.rescan.atstart").toLowerCase().equals("true")) {
                SimpleTrigger atStartTrigger = new SimpleTrigger("atStartTrigger", "triggerGroup");

                atStartTrigger.setJobName(jobDetail.getName());
                atStartTrigger.setJobGroup(jobDetail.getGroup());

                quartzScheduler.scheduleJob(atStartTrigger);
            }
            // start the scheduler
            quartzScheduler.start();
        } catch (Throwable x) {
            log.error("Caught an exception:", x);
        }
    }


}
