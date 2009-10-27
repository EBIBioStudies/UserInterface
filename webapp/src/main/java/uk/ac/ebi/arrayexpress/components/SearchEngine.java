package uk.ac.ebi.arrayexpress.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.saxon.search.Controller;


public class SearchEngine extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Controller controller;

    public SearchEngine()
    {
        super("SearchEngine");
    }

    @Override
    public void initialize()
    {
        try {
            controller = Controller.initController(getApplication().getResource("/WEB-INF/classes/aeindex.xml"));
        } catch (Throwable x) {
            logger.error("Caught an exception:", x);
        }

    }

    @Override
    public void terminate()
    {
    }

    public Controller getController()
    {
        return controller;
    }
}
