package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;

public class Application {

    public Application( ServletContext context )
    {
        servletContext = context;

        preferences = new Preferences(this);
        experiments = new Experiments(this);
        filesDirectory = new DownloadableFilesDirectory();
        xsltHelper = new XsltHelper(this);

        // TODO: remove this crap..
        HelperXsltExtension.setApplication(this);
        
        // load application preferences
        preferences.load();
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

    private ServletContext servletContext;

    private Preferences preferences;
    private Experiments experiments;
    private DownloadableFilesDirectory filesDirectory;
    private XsltHelper xsltHelper;

    // logging macinery
    private final Log log = LogFactory.getLog(getClass());
}
