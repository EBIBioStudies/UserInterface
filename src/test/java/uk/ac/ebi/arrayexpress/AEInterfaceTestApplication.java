package uk.ac.ebi.arrayexpress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.components.*;

import java.net.MalformedURLException;
import java.net.URL;

public class AEInterfaceTestApplication extends Application
{
    // logging machinery
    private final Log log = LogFactory.getLog(getClass());

    public AEInterfaceTestApplication()
    {
        super("arrayexpress");

        // test-instance only code to emulate functionality missing from tomcat container
        // add a shutdown hook to to a proper termination
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        addComponent(new SaxonEngine());
        addComponent(new SearchEngine());
        addComponent(new Experiments());
        addComponent(new Users());
        addComponent(new DownloadableFilesRegistry());
        addComponent(new JobsController());

        initialize();
    }

    public URL getResource(String path) throws MalformedURLException
    {
        return getClass().getResource(path.replaceFirst("/WEB-INF/classes", ""));
    }

    // this is to receive termination notification and shutdown system properly
    private class ShutdownHook extends Thread
    {
        public void run()
        {
            Application.getInstance().terminate();
        }
    }
}
