package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;

import javax.servlet.ServletContext;

public class Application {

    // logging macinery
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(Application.class);

    // auto-created singleton machinery
    private static Application instance = null;

    public Application( ServletContext context )
    {
        if ( null != instance ) {
            log.error( "Wow, somebody is really trying to create a second instance of the application here. See stack trace below", new Throwable() );
        } else {
            instance = this;
            servletContext = context;

            // load application preferences
            Preferences().load();
        }
    }

    static public Application Instance()
    {
        return instance;
    }

    // auto-created singleton for related objects (Preferences, Experiments, etc)
    private static Preferences preferences = null;

    synchronized static public Preferences Preferences()
    {
        if ( null == preferences ) {
            preferences = new Preferences();
        }
        return preferences;
    }

    private static Experiments experiments = null;

    synchronized static public Experiments Experiments()
    {
        if ( null == experiments ) {
            experiments = new Experiments();
        }
        return experiments;
    }

    private static DownloadableFilesDirectory filesDirectory = null;

    synchronized static public DownloadableFilesDirectory FilesDirectory()
    {
        if ( null == filesDirectory ) {
            filesDirectory = new DownloadableFilesDirectory();
        }
        return filesDirectory;
    }

    public ServletContext ServletContext()
    {
        return servletContext;
    }

    private ServletContext servletContext = null;
}
