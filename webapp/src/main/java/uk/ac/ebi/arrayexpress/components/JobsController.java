package uk.ac.ebi.arrayexpress.components;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.jobs.ReloadExperimentsJob;
import uk.ac.ebi.arrayexpress.jobs.RescanFilesJob;
import uk.ac.ebi.arrayexpress.jobs.RetrieveExperimentsListFromAtlasJob;
import uk.ac.ebi.arrayexpress.jobs.RetrieveExperimentsXmlJob;

import java.util.List;


public class JobsController extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // jobs group
    private final String AE_JOBS_GROUP = "ae-jobs";

    // quartz scheduler
    private Scheduler scheduler;

    public JobsController()
    {
        super("JobsController");
    }

    public void initialize()
    {
        //addTriggerListener(new ApplicationTriggerListener());
        addJob("rescan-files", RescanFilesJob.class);
        addJob("reload-xml", ReloadExperimentsJob.class);
        addJob("retrieve-xml", RetrieveExperimentsXmlJob.class);
        addJob("reload-atlas-info", RetrieveExperimentsListFromAtlasJob.class);
        scheduleJob("rescan-files", "ae.files.rescan");
        scheduleJob("reload-xml", "ae.experiments.reload");
        scheduleJob("reload-atlas-info", "ae.atlasexperiments.reload");
        startScheduler();
    }

    private void startScheduler()
    {
        try {
            getScheduler().start();
        } catch ( SchedulerException x ) {
            logger.error("Caught an exception:", x);
        }
    }

    public void terminate()
    {
        terminateJobs();
    }

    public void executeJob( String name )
    {
        try {
            getScheduler().triggerJob(name, AE_JOBS_GROUP);
        } catch ( Throwable x ) {
            logger.error("Caught an exception:", x);
        }
    }

    public void executeJobWithParam( String name, String param )
    {
        try {
            JobDataMap map = new JobDataMap();
            map.put("param", param);
            getScheduler().triggerJob(name, AE_JOBS_GROUP, map);
        } catch ( Throwable x ) {
            logger.error("Caught an exception:", x);
        }
    }


    public void setJobListener( JobListener jl )
    {
        try {
            if (null != jl) {
                getScheduler().addGlobalJobListener(jl);
            } else {
                getScheduler().removeGlobalJobListener("job-listener");
            }
        } catch ( Throwable x ) {
            logger.error("Caught an exception:", x);
        }
    }

    private Scheduler getScheduler()
    {
        if (null == scheduler) {
            try {
                // Retrieve a scheduler from schedule factory
                scheduler = new StdSchedulerFactory().getScheduler();
            } catch ( Throwable x ) {
                logger.error("Caught an exception:", x);
            }
        }
        return scheduler;
    }

    private void addTriggerListener( TriggerListener tl )
    {
        try {
            getScheduler().addGlobalTriggerListener(tl);
        } catch (Throwable x ) {
            logger.error("Caught an exception:", x);
        }
    }
    
    private void addJob( String name, Class c )
    {
        JobDetail j = new JobDetail(name, AE_JOBS_GROUP, c, true /* volatilily */, true /*durability */, false /* recover */);
        try {
            getScheduler().addJob(j, false);
        } catch ( Throwable x ) {
            logger.error("Caught an exception:", x);
        }
    }

    private void scheduleJob( String name, String preferencePrefix )
    {
        String schedule = getPreferences().getString(preferencePrefix + ".schedule");
        Long interval = getPreferences().getLong(preferencePrefix + ".interval");
        Boolean atStart = getPreferences().getBoolean(preferencePrefix + ".atstart");

        if (null != schedule && 0 < schedule.length()) {
            CronTrigger cronTrigger = new CronTrigger(name + "_schedule_trigger", null);
            try {
                // setup CronExpression
                CronExpression cexp = new CronExpression(schedule);
                // Assign the CronExpression to CronTrigger
                cronTrigger.setCronExpression(cexp);
                cronTrigger.setJobName(name);
                cronTrigger.setJobGroup(AE_JOBS_GROUP);
                // schedule a job with JobDetail and Trigger
                getScheduler().scheduleJob(cronTrigger);
            } catch ( Throwable x ) {
                logger.error("Caught an exception:", x);
            }
        }

        boolean hasScheduledInterval = false;

        if (null != interval) {
            SimpleTrigger intervalTrigger = new SimpleTrigger(name + "_interval_trigger", AE_JOBS_GROUP, SimpleTrigger.REPEAT_INDEFINITELY, interval);

            intervalTrigger.setJobName(name);
            intervalTrigger.setJobGroup(AE_JOBS_GROUP);

            try {
                getScheduler().scheduleJob(intervalTrigger);
                hasScheduledInterval = true;
            } catch ( Throwable x ) {
                logger.error("Caught an exception:", x);
            }
        }

        if ((null != atStart && atStart) && !hasScheduledInterval) {
            SimpleTrigger intervalTrigger = new SimpleTrigger(name + "_at_start_trigger", AE_JOBS_GROUP);

            intervalTrigger.setJobName(name);
            intervalTrigger.setJobGroup(AE_JOBS_GROUP);

            try {
                getScheduler().scheduleJob(intervalTrigger);
            } catch ( Throwable x ) {
                logger.error("Caught an exception:", x);
            }
        }
    }

    private void terminateJobs()
    {
        try {
            // stop all jobs from being triggered
            getScheduler().pauseAll();

            List runningJobs = getScheduler().getCurrentlyExecutingJobs();
            for ( Object jec : runningJobs ) {
                JobDetail j = ((JobExecutionContext) jec).getJobDetail();
                getScheduler().interrupt(j.getName(), j.getGroup());
            }

            getScheduler().shutdown(true);

        } catch ( Throwable x ) {
            logger.error("Caught an exception:", x);
        }
    }
}
