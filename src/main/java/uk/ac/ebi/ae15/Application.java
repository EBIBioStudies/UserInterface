package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;

public class Application {

    // logging macinery
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(Application.class);

    // filesysystem separator
    private final static String slash = System.getProperty("file.separator");

    // auto-created singleton machinery
    private static Application instance = null;

    synchronized static public Application Instance( String contextRoot ) {
        if ( null == instance ) {
            instance = new Application(contextRoot);
        }
        return instance;
    }

    // auto-created singleton for related objects (Preferences, Experiments, etc)
    private static Preferences preferences = null;

    synchronized static public Preferences Preferences() {
        if ( null == preferences ) {
            preferences = new Preferences();
        }
        return preferences;
    }

    private static Experiments experiments = null;

    synchronized static public Experiments Experiments() {
        if ( null == experiments ) {
            experiments = new Experiments();
        }
        return experiments;
    }

    private static DownloadableFilesDirectory filesDirectory = null;

    synchronized static public DownloadableFilesDirectory FilesDirectory() {
        if ( null == filesDirectory ) {
            filesDirectory = new DownloadableFilesDirectory();
        }
        return filesDirectory;
    }

    public Application( String contextRoot ) {

        // load application preferences
        Preferences().load();

        if ( null != contextRoot ) {
            if ( !contextRoot.endsWith(slash) ) {
                contextRoot = contextRoot + slash;
            }
            Preferences().setProperty( "ae.webapp.root", contextRoot );
        } else {
            log.error("Unable to determine webapp root location, expect problems down the road.");
        }
    }
}
