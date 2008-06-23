package uk.ac.ebi.ae15;

import uk.ac.ebi.ae15.app.Application;
import uk.ac.ebi.ae15.jobs.JobsController;

public class AEInterfaceApplication extends Application
{
    public AEInterfaceApplication()
    {
        super("arrayexpress");
        addComponent(new Experiments(this));
        addComponent(new DownloadableFilesRegistry(this));
        addComponent(new XsltHelper(this));
        addComponent(new JobsController(this));
    }
/*
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
*/
}
