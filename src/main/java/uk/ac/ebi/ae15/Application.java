package uk.ac.ebi.ae15;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    public Application( ServletContext context )
    {
        servletContext = context;

        preferences = new Preferences(this);
        experiments = new Experiments(this);
        filesDirectory = new DownloadableFilesDirectory();
        xsltHelper = new XsltHelper(this);

        // load application preferences
        preferences.load();
    }

    public void releaseComponents()
    {
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
}
