package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;

public class Application {

    // logging macinery
    private static final Log log = org.apache.commons.logging.LogFactory.getLog(Application.class);

    // auto-created singleton machinery
    private static Application instance = null;

    synchronized static public Application Instance() {
        if ( null == instance ) {
            instance = new Application();
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

    public Application() {
        Preferences().load();
    }
}
